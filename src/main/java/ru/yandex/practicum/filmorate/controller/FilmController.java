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
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping("/films")
    public List<FilmDto> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/films/{id}")
    public Optional<FilmDto> findFilm(@PathVariable Long id) {
        return filmService.findById(id);
    }

    @PostMapping("/films")
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        return filmService.create(FilmMapper.mapToFilm(filmDto));
    }

    @PutMapping("/films")
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        return filmService.update(FilmMapper.mapToFilm(filmDto));
    }

    @PutMapping("/films/{id}/like/{user-id}")
    public void addLike(@PathVariable Long id, @PathVariable("user-id") Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{user-id}")
    public void deleteLike(@PathVariable Long id, @PathVariable("user-id") Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/films/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
}
