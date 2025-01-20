package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final String directorIdPath = "/{id}";
    private final DirectorService directorService;

    @GetMapping
    public List<DirectorDto> findAll() {
        return directorService.findAll();
    }

    @GetMapping(directorIdPath)
    public DirectorDto findById(@PathVariable Long id) {
        return directorService.findById(id);
    }

    @PostMapping
    public DirectorDto create(@Valid @RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public DirectorDto update(@Valid @RequestBody Director director) {
        return directorService.update(director);
    }

    @DeleteMapping(directorIdPath)
    public void delete(@PathVariable Long id) {
        directorService.delete(id);
    }
}