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

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;
    private final FilmEnrichmentService filmEnrichmentService;
    private final DirectorRepository directorRepository;
    private final EventRepository eventRepository;

    @Cacheable("films")
    public List<FilmDto> findAll() {
        List<Film> films = filmRepository.findAll();
        filmEnrichmentService.enrichFilms(films);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    @Cacheable(value = "filmsByIds", key = "#filmIds")
    public List<FilmDto> findAllWithIds(Set<Long> filmIds) {
        List<Film> films = filmRepository.findAllWithIds(filmIds);
        filmEnrichmentService.enrichFilms(films);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
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
        filmEnrichmentService.enrichFilms(films);
        return films.stream()
                .sorted(Comparator.comparingDouble(Film::getRate).reversed())
                .map(FilmMapper::mapToFilmDto)
                .toList();
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
        filmEnrichmentService.enrichFilms(films);
        return films.stream()
                .sorted(Comparator.comparingDouble(Film::getRate).reversed())
                .limit(count)
                .map(FilmMapper::mapToFilmDto)
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
        filmEnrichmentService.enrichFilms(films);
        directorRepository.existById(directorId);
        if ("year".equals(sortBy)) {
            films.sort(Comparator.comparing(Film::getReleaseDate));
        } else {
            films.sort(Comparator.comparingDouble(Film::getRate).reversed());
        }

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
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
        filmEnrichmentService.enrichFilms(films);

        return films.stream()
                .sorted(Comparator.comparingDouble(Film::getRate).reversed())
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }
}