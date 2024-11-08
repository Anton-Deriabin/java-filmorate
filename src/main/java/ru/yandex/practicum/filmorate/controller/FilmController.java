package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final String messageNameDuplicate = "Это имя уже используется";
    @Getter
    protected final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Коллекция фильмов отправлена по запросу");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        for (Film value : films.values()) {
            if (film.getName().equals(value.getName())) {
                log.error("Попытка занять уже используемое имя при добавлении");
                throw new DuplicatedDataException(messageNameDuplicate);
            }
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата выхода раньше 28.12.1895 при добавлении");
            throw new ValidationException("Дата выхода не может быть раньше " +
                    "даты выхода первого в истории фильма");
        }
        film.setId(getNextId());
        log.debug("Фильму \"{}\" назначен id = {}", film.getName(), film.getId());
        films.put(film.getId(), film);
        log.info("Фильм с id = {}  - добавлен", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Id фильма для обновления не указан");
            throw new ValidationException("Id должен быть указан");

        }
        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата выхода к обновлению раньше 28.12.1895");
            throw new ValidationException("Дата выхода не может быть раньше " +
                    "даты выхода первого в истории фильма");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.trace("Создали переменную старого фильма для сравнения с новой");
            if (newFilm.getName() != null) {
                for (Film value : films.values()) {
                    if (newFilm.getName().equals(value.getName())) {
                        log.error("Попытка занять уже используемое имя при обновлении");
                        throw new DuplicatedDataException(messageNameDuplicate);
                    }
                }
            }
            oldFilm.setName(newFilm.getName());
            log.debug("Фильму с id = {} установлено имя - {}", newFilm.getId(), newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            log.debug("Фильму с id = {} установлено описание - {}", newFilm.getId(), newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            log.debug("Фильму с id = {} установлена дата выхода - {}", newFilm.getId(), newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.debug("Фильму с id = {} установлена длительность - {}", newFilm.getId(), newFilm.getDuration());
            log.info("Фильм \"{}\" с id = {}  - обновлен", newFilm.getName(), newFilm.getId());
            return oldFilm;
        }
        log.error("Попытка получить фильм с несуществующим id = {}", newFilm.getId());
        throw new NotFoundException(String.format("Фильм с id = %d  - не найден", newFilm.getId()));
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        log.debug("Cоздали новый id = {} ", currentMaxId);
        return ++currentMaxId;
    }
}