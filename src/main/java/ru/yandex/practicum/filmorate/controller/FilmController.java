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
    private final String filmsIdPath = "/{id}";
    private final String likePath = "/{id}/like/{user-id}";
    private final String popularPath = "/popular";
    private final FilmService filmService;

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.findAll();
    }

    @GetMapping(filmsIdPath)
    public Optional<FilmDto> findFilm(@PathVariable Long id) {
        return filmService.findById(id);
    }

    @PostMapping()
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        return filmService.create(FilmMapper.mapToFilm(filmDto));
    }

    @PutMapping()
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        return filmService.update(FilmMapper.mapToFilm(filmDto));
    }

    @DeleteMapping(filmsIdPath)
    public void delete(@PathVariable Long id) {
        filmService.delete(id);
    }

    @PutMapping(likePath)
    public void addLike(@PathVariable Long id, @PathVariable("user-id") Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping(likePath)
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

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam(defaultValue = "likes") String sortBy
    ) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }


}
