package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final String filmsIdPath = "/{id}";
    private final String putLikePath = "/{id}/like/{user-id}/{mark}";
    private final String deleteLikePath = "/{id}/like/{user-id}";
    private final String popularPath = "/popular";
    private final String commonFilmsPath = "/common";
    private final String directorPath = "/director/{director-id}";
    private final String searchPath = "/search";
    private final FilmService filmService;

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.findAll();
    }

    @GetMapping(filmsIdPath)
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

    @DeleteMapping(filmsIdPath)
    public void delete(@PathVariable Long id) {
        filmService.delete(id);
    }

    @PutMapping(putLikePath)
    public void addLike(@PathVariable Long id, @PathVariable("user-id") Long userId, @PathVariable Double mark) {
        filmService.addLike(id, userId, mark);
    }

    @DeleteMapping(deleteLikePath)
    public void deleteLike(@PathVariable Long id, @PathVariable("user-id") Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping(popularPath)
    public List<FilmDto> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year
    ) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping(directorPath)
    public List<FilmDto> getFilmsByDirector(
            @PathVariable("director-id") Long directorId,
            @RequestParam(defaultValue = "likes") String sortBy
    ) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping(commonFilmsPath)
    public List<FilmDto> getCommonFilms(@RequestParam Long userId,
                                        @RequestParam Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping(searchPath)
    public List<FilmDto> search(@RequestParam String query,
                                @RequestParam String by) {
        if (!isValidSearchBy(by)) {
            throw new ValidationException("Parameter 'by' must contain 'director' and/or 'title'");
        }
        return filmService.search(query, by);
    }

    private boolean isValidSearchBy(String by) {
        Set<String> validParams = Set.of("director", "title");
        Set<String> providedParams = Arrays.stream(by.toLowerCase().split(","))
                .collect(Collectors.toSet());
        return !providedParams.isEmpty() && validParams.containsAll(providedParams);
    }
}