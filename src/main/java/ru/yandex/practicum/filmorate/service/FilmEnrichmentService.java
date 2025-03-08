package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DirectorRepository;
import ru.yandex.practicum.filmorate.storage.GenreRepository;
import ru.yandex.practicum.filmorate.storage.LikeRepository;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmEnrichmentService {
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;
    private final LikeRepository likeRepository;
    private final ExecutorService executorService;

    public void enrichFilm(Film film) {
        if (film == null) return;
        enrichFilms(List.of(film));
    }

    public void enrichFilms(List<Film> films) {
        if (films == null || films.isEmpty()) return;

        List<Long> filmIds = films.stream().map(Film::getId).toList();

        Callable<Map<Long, Set<Object>>> genresTask = () -> {
            Map<Long, Set<Genre>> genres = genreRepository.findGenresForFilms(filmIds);
            return genres.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
        };

        Callable<Map<Long, Set<Object>>> directorsTask = () -> {
            Map<Long, Set<Director>> directors = directorRepository.findDirectorsForFilms(filmIds);
            return directors.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
        };

        try {
            List<Future<Map<Long, Set<Object>>>> futures = executorService.invokeAll(List.of(genresTask, directorsTask));
            Map<Long, Set<Object>> genresByFilm = futures.get(0).get();
            Map<Long, Set<Object>> directorsByFilm = futures.get(1).get();

            films.forEach(film -> {
                Set<Genre> genres = genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()).stream()
                        .map(genre -> (Genre) genre)
                        .collect(Collectors.toSet());
                Set<Director> directors = directorsByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()).stream()
                        .map(director -> (Director) director)
                        .collect(Collectors.toSet());

                film.setGenres(genres);
                film.setDirectors(directors);
                film.setRate(likeRepository.getAverageRate(film.getId()));
            });
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Ошибка при многопоточном обогащении фильмов", e);
        }
    }
}