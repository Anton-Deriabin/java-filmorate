package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final String FILMS_ID_PATH = "/{id}";
    private final String LIKE_PATH = "/{id}/like/{user-id}";
    private final String POPULAR_PATH = "/popular";
    private final String COMMON_FILMS_PATH = "/common";
    private final String DIRECTOR_PATH = "/director/{directorId}";
    private final String SEARCH_PATH = "/search";

    private final FilmService filmService;

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.findAll();
    }

    @GetMapping(FILMS_ID_PATH)
    public Optional<FilmDto> findFilm(@PathVariable Long id) {
        return filmService.findById(id);
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        return filmService.create(FilmMapper.mapToFilm(filmDto));
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        return filmService.update(FilmMapper.mapToFilm(filmDto));
    }

    @DeleteMapping(FILMS_ID_PATH)
    public void delete(@PathVariable Long id) {
        filmService.delete(id);
    }

    @PutMapping(LIKE_PATH)
    public void addLike(@PathVariable Long id, @PathVariable("user-id") Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping(LIKE_PATH)
    public void deleteLike(@PathVariable Long id, @PathVariable("user-id") Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping(POPULAR_PATH)
    public List<FilmDto> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year
    ) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping(DIRECTOR_PATH)
    public List<FilmDto> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam(defaultValue = "likes") String sortBy
    ) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping(COMMON_FILMS_PATH)
    public List<FilmDto> getCommonFilms(
            @RequestParam("userId") Long userId,
            @RequestParam("friendId") Long friendId
    ) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping(SEARCH_PATH)
    public List<FilmDto> search(
            @RequestParam String query,
            @RequestParam String by
    ) {
        return filmService.search(query, by);
    }
}