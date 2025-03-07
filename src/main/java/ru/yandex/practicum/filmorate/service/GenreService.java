package ru.yandex.practicum.filmorate.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.GenreRepository;

import java.util.List;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Cacheable("genres")
    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream()
                .map(GenreMapper::mapToGenreDto)
                .toList();
    }

    @Cacheable(value = "genres", key = "#id")
    public GenreDto findById(Long id) {
        return genreRepository.findById(id)
                .map(GenreMapper::mapToGenreDto)
                .orElseThrow(() -> new NotFoundException(String.format("Жанр с id=%d не найден", id)));
    }
}
