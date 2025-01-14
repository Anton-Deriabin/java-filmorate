package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.DirectorRepository;
import ru.yandex.practicum.filmorate.storage.GenreRepository;
import ru.yandex.practicum.filmorate.storage.LikeRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmEnrichmentService {
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;
    private final LikeRepository likeRepository;

    public void enrichFilm(Film film) {
        if (film == null) return;
        enrichFilms(List.of(film));
    }

    public void enrichFilms(List<Film> films) {
        if (films == null || films.isEmpty()) return;

        Map<Long, Set<Genre>> genresByFilm = genreRepository.findGenresForFilms(
                films.stream().map(Film::getId).toList()
        );
        Map<Long, Set<Director>> directorsByFilm = directorRepository.findDirectorsForFilms(
                films.stream().map(Film::getId).toList()
        );

        films.forEach(film -> {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setDirectors(directorsByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
            List<Like> likes = likeRepository.findLikesByFilmId(film.getId());
            film.setLikes(new HashSet<>(likes));
        });
    }
}