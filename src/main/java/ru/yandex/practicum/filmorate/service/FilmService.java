package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.DirectorRepository;
import ru.yandex.practicum.filmorate.storage.EventRepository;
import ru.yandex.practicum.filmorate.storage.FilmRepository;
import ru.yandex.practicum.filmorate.storage.LikeRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;
    private final FilmEnrichmentService filmEnrichmentService;
    private final DirectorRepository directorRepository;
    private final EventRepository eventRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(21);

    @Cacheable("films")
    public List<FilmDto> findAll() {
        List<Film> films = filmRepository.findAll();
        return enrichAndMapFilms(films);
    }

    @Cacheable(value = "filmsByIds", key = "#filmIds")
    public List<FilmDto> findAllWithIds(Set<Long> filmIds) {
        List<Film> films = filmRepository.findAllWithIds(filmIds);
        return enrichAndMapFilms(films);
    }

    @Cacheable(value = "filmById", key = "#id")
    public Optional<FilmDto> findById(Long id) {
        Optional<Film> filmOptional = filmRepository.findById(id);
        if (filmOptional.isPresent()) {
            Film film = filmOptional.get();
            filmEnrichmentService.enrichFilm(film);
            return Optional.of(FilmMapper.mapToFilmDto(film));
        } else {
            throw new NotFoundException(String.format("Фильм с id=%d не найден", id));
        }
    }

    public FilmDto create(Film film) {
        checkReleaseDate(film);
        Film createdFilm = filmRepository.create(film);
        filmEnrichmentService.enrichFilm(createdFilm);
        return FilmMapper.mapToFilmDto(createdFilm);
    }

    public FilmDto update(Film film) {
        checkReleaseDate(film);
        Film updatedFilm = filmRepository.update(film);
        filmEnrichmentService.enrichFilm(updatedFilm);
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public void delete(Long id) {
        filmRepository.delete(id);
    }

    @Cacheable(value = "commonFilms", key = "#userId + '_' + #friendId")
    public List<FilmDto> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = filmRepository.getCommonFilms(userId, friendId);
        return enrichAndMapFilms(films);
    }

    public void addLike(Long filmId, Long userId, Double mark) {
        eventRepository.addEvent(userId, filmId, EventType.LIKE, Operation.ADD);
        likeRepository.addLike(filmId, userId, mark);
    }

    public void deleteLike(Long filmId, Long userId) {
        eventRepository.addEvent(userId, filmId, EventType.LIKE, Operation.REMOVE);
        likeRepository.deleteLike(filmId, userId);
    }

    @Cacheable(value = "popularFilms", key = "#count + '_' + #genreId + '_' + #year")
    public List<FilmDto> getPopularFilms(int count, Integer genreId, Integer year) {
        List<Film> films = filmRepository.findAllWithFilters(genreId, year);
        List<FilmDto> enrichedFilms = enrichAndMapFilms(films);
        return enrichedFilms.stream()
                .sorted(Comparator.comparingDouble(FilmDto::getRate).reversed())
                .limit(count)
                .toList();
    }

    private void checkReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата выхода не может быть раньше " +
                    "28.12.1895 - даты выхода первого в истории фильма");
        }
    }

    @Cacheable(value = "filmsByDirector", key = "#directorId + '_' + #sortBy")
    public List<FilmDto> getFilmsByDirector(Long directorId, String sortBy) {
        List<Film> films = filmRepository.findFilmsByDirector(directorId);
        directorRepository.existById(directorId);
        List<FilmDto> enrichedFilms = enrichAndMapFilms(films);
        if ("year".equals(sortBy)) {
            enrichedFilms.sort(Comparator.comparing(FilmDto::getReleaseDate));
        } else {
            enrichedFilms.sort(Comparator.comparingDouble(FilmDto::getRate).reversed());
        }
        return enrichedFilms;
    }

    @Cacheable(value = "searchFilms", key = "#query + '_' + #by")
    public List<FilmDto> search(String query, String by) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<String> searchBy = Arrays.asList(by.toLowerCase().split(","));

        if (!searchBy.contains("director") && !searchBy.contains("title")) {
            throw new ValidationException("Параметр 'by' должен содержать 'director' и/или 'title'");
        }

        List<Film> films = filmRepository.search(query, searchBy);
        List<FilmDto> enrichedFilms = enrichAndMapFilms(films);
        return enrichedFilms.stream()
                .sorted(Comparator.comparingDouble(FilmDto::getRate).reversed())
                .toList();
    }

    private List<FilmDto> enrichAndMapFilms(List<Film> films) {
        int totalFilms = films.size();
        int batchSize = (totalFilms + 3) / 4; // Разделим на 4 части, округляя вверх

        List<Callable<List<Film>>> tasks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int startIdx = i * batchSize;
            int endIdx = Math.min((i + 1) * batchSize, totalFilms);
            List<Film> subList = films.subList(startIdx, endIdx);
            tasks.add(() -> {
                filmEnrichmentService.enrichFilms(subList);
                return subList;
            });
        }

        try {
            List<Future<List<Film>>> futures = executorService.invokeAll(tasks);
            List<Film> enrichedFilms = new ArrayList<>();
            for (Future<List<Film>> future : futures) {
                enrichedFilms.addAll(future.get());
            }
            return enrichedFilms.stream()
                    .map(FilmMapper::mapToFilmDto)
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Ошибка при выполнении многопоточной операции", e);
        }
    }
}