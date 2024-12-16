package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.FilmRepository;
import ru.yandex.practicum.filmorate.storage.GenreRepository;
import ru.yandex.practicum.filmorate.storage.LikeRepository;

import java.util.*;

@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;
    private final GenreRepository genreRepository;

    public FilmService(FilmRepository filmRepository, LikeRepository likeRepository, GenreRepository genreRepository) {
        this.filmRepository = filmRepository;
        this.likeRepository = likeRepository;
        this.genreRepository = genreRepository;
    }

    public List<FilmDto> findAll() {
        List<Film> films = filmRepository.findAll();
        Map<Long, Set<Genre>> genresByFilm = genreRepository.findGenresForFilms(
                films.stream().map(Film::getId).toList()
        );
        films.forEach(film -> film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>())));
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public Optional<FilmDto> findById(Long id) {
        Optional<Film> filmOptional = filmRepository.findById(id);
        if (filmOptional.isPresent()) {
            Film film = filmOptional.get();
            Map<Long, Set<Genre>> genresByFilm = genreRepository.findGenresForFilms(List.of(film.getId()));
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
            return Optional.of(FilmMapper.mapToFilmDto(film));
        }
        return Optional.empty();
    }

    public FilmDto create(Film film) {
        Film createdFilm = filmRepository.create(film);
        Map<Long, Set<Genre>> genresByFilm = genreRepository.findGenresForFilms(List.of(createdFilm.getId()));
        createdFilm.setGenres(genresByFilm.getOrDefault(createdFilm.getId(), new LinkedHashSet<>()));
        return FilmMapper.mapToFilmDto(createdFilm);
    }

    public FilmDto update(Film film) {
        Film updatedFilm = filmRepository.update(film);
        Map<Long, Set<Genre>> genresByFilm = genreRepository.findGenresForFilms(List.of(updatedFilm.getId()));
        updatedFilm.setGenres(genresByFilm.getOrDefault(updatedFilm.getId(), new LinkedHashSet<>()));
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public void addLike(Long filmId, Long userId) {
        likeRepository.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        likeRepository.deleteLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        List<Film> films = filmRepository.findAll();
        Map<Long, Set<Genre>> genresByFilm = genreRepository.findGenresForFilms(
                films.stream().map(Film::getId).toList()
        );
        films.forEach(film -> film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>())));
        films.forEach(film -> {
            List<Like> likes = likeRepository.findLikesByFilmId(film.getId());
            film.setLikes(new HashSet<>(likes));
        });
        return films.stream()
                .sorted((a, b) -> b.getLikes().size() - a.getLikes().size())
                .limit(count)
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }
}


