package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final String messageUser = "Пользователь не найден";
    private final String messageFilm = "Фильм не найден";
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film addLike(Long filmId, Long userId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException(messageUser));
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException(messageFilm));
        if (user.getLikedFilms().contains(filmId)) {
            log.error("Попытка поставить фильму лайк пользователем повторно");
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }
        user.getLikedFilms().add(filmId);
        log.debug("В список понравившихся фильмов пользователя с id = {} добавлен фильм \"{}\"",
                film.getId(),
                film.getName());
        film.incrementLikes();
        log.debug("Счетчик лайков у фильма \"{}\" увеличен на 1", film.getName());
        filmStorage.update(film);
        log.debug("Фильм \"{}\" с увеличенным количеством лайков обновлен в коллекции фильмов", film.getName());
        userStorage.update(user);
        log.debug("Пользователь с id = {} с увеличенным количеством понравившихся фильмов " +
                        "обновлен в коллекции пользователей", user.getId());
        log.info("Фильму \"{}\" поставлен лайк пользователем с id = {}", film.getName(), user.getId());
        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException(messageUser));
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException(messageFilm));
        if (user.getLikedFilms().contains(filmId)) {
            user.getLikedFilms().remove(filmId);
            log.debug("Из списка понравившихся фильмов пользователя с id = {} удален фильм \"{}\"",
                    film.getId(),
                    film.getName());
            film.decrementLikes();
            log.debug("Счетчик лайков у фильма \"{}\" уменьшен на 1", film.getName());
            filmStorage.update(film);
            log.debug("Фильм \"{}\" с уменьшенным количеством лайков обновлен в коллекции фильмов", film.getName());
            userStorage.update(user);
            log.debug("Пользователь с id = {} с уменьшенным количеством понравившихся фильмов " +
                    "обновлен в коллекции пользователей", user.getId());
            log.info("Пользователем с id = {} был удален лайк фильму \"{}\"", user.getName(), film.getName());
        } else {
            log.error("Попытка удалить лайк фильму, который не был поставлен");
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }
        return film;
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> popularFilms = filmStorage
                .findAll()
                .stream()
                .sorted((film1, film2) -> Long.compare(film2.getLike(), film1.getLike()))
                .limit(count)
                .collect(Collectors.toList());
        log.debug("Получена коллекция фильмов начиная с 1 и до {} - включительно и записана в переменную", count);
        if (popularFilms.isEmpty()) {
            log.error("Попытка получить пустой список популярных фильмов");
            throw new NotFoundException("Список популярных фильмов пуст");
        }
        log.info("Пользователем был получен список популярных фильмов  начиная с 1 и до {} - включительно", count);
        return popularFilms;
    }
}
