package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
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

    public List<FilmDto> findAll() {
        List<Film> films = filmRepository.findAll();
        filmEnrichmentService.enrichFilms(films);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

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

    public void addLike(Long filmId, Long userId) {
        likeRepository.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        likeRepository.deleteLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        List<Film> films = filmRepository.findAll();
        filmEnrichmentService.enrichFilms(films);
        return films.stream()
                .sorted((a, b) -> b.getLikes().size() - a.getLikes().size())
                .limit(count)
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDto> getFilmsByDirector(Long directorId, String sortBy) {
        List<Film> films = filmRepository.findFilmsByDirector(directorId);
        filmEnrichmentService.enrichFilms(films);

        if ("year".equals(sortBy)) {
            films.sort(Comparator.comparing(Film::getReleaseDate));
        } else {
            films.sort((a, b) -> b.getLikes().size() - a.getLikes().size());
        }

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    private void checkReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата выхода не может быть раньше " +
                    "28.12.1895 - даты выхода первого в истории фильма");
        }
    }
}