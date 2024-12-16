package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MpaRatingController {
    private final MpaRatingService mpaRatingService;

    @GetMapping("/mpa")
    public List<MpaRatingDto> findAll() {
        return mpaRatingService.findAll();
    }

    @GetMapping("/mpa/{id}")
    public MpaRatingDto findFilm(@PathVariable Long id) {
        return mpaRatingService.findById(id);
    }
}
