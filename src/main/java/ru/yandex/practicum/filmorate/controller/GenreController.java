package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GenreController {
    private final String genresPath = "/genres";
    private final String genresIdPath = "/genres/{id}";
    private final GenreService genreService;

    @GetMapping(genresPath)
    public List<GenreDto> findAll() {
        return genreService.findAll();
    }

    @GetMapping(genresIdPath)
    public GenreDto findFilm(@PathVariable Long id) {
        return genreService.findById(id);
    }
}
