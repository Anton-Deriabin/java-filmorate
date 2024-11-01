package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final String messageNameDuplicate = "Это имя уже используется";
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Имя должно быть указано");
        }
        for (Film value : films.values()) {
            if (film.getName().equals(value.getName())) {
                throw new DuplicatedDataException(messageNameDuplicate);
            }
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1985, 12, 28))) {
            throw new ValidationException("Дата выхода не может быть раньше " +
                    "даты выхода первого в истории фильма");
        }
        if (film.getDuration() == null || film.getDuration().toMinutes() <= 0) {
            throw new ValidationException("Длительность фильма должна быть больше нуля");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            throw new ValidationException("Имя должно быть указано");
        }
        for (Film value : films.values()) {
            if (newFilm.getName().equals(value.getName())) {
                throw new DuplicatedDataException(messageNameDuplicate);
            }
        }
        if (newFilm.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1985, 12, 28))) {
            throw new ValidationException("Дата выхода не может быть раньше " +
                    "даты выхода первого в истории фильма");
        }
        if (newFilm.getDuration() == null || newFilm.getDuration().toMinutes() <= 0) {
            throw new ValidationException("Длительность фильма должна быть больше нуля");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null) {
                for (Film value : films.values()) {
                    if (newFilm.getName().equals(value.getName())) {
                        throw new DuplicatedDataException(messageNameDuplicate);
                    }
                }
            }
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            return oldFilm;
        }
        throw new NotFoundException(String.format("Фильм с id = %d  - не найден", newFilm.getId()));
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }
}