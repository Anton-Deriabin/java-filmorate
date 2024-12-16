package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String FIND_ALL_QUERY =
            "SELECT f.*, r.name AS rating_name " +
            "FROM films f " +
            "LEFT JOIN ratings r ON f.rating_id = r.id ";
    private static final String FIND_BY_ID_QUERY =
            "SELECT f.*, r.name AS rating_name " +
            "FROM films f " +
            "LEFT JOIN ratings r ON f.rating_id = r.id " +
            "WHERE f.id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO films(name, description, release_date, duration, rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE films " +
            "SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
            "WHERE id = ?";
    private static final String DELETE_GENRES_BY_FILM_ID =
            "DELETE FROM film_genres " +
            "WHERE film_id = ?";
    private static final String INSERT_IN_FILM_GENRES_QUERY =
            "INSERT INTO film_genres(film_id, genre_id) " +
            "VALUES (?, ?)";
    private static final String CHECK_RATING_QUERY =
            "SELECT COUNT(*) " +
            "FROM ratings " +
            "WHERE id = ?";
    private static final String CHECK_GENRE_QUERY =
            "SELECT COUNT(*) " +
            "FROM genres " +
            "WHERE id = ?";
    private static final String CHECK_ID_QUERY =
            "SELECT COUNT(*) " +
            "FROM films " +
            "WHERE id = ?";

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper, Film.class);
    }

    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Film> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Film create(Film film) {
        checkReleaseDate(film);
        checkRating(film);
        checkGenre(film);
        long filmId = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating().getId()
        );
        film.setId(filmId);
        saveGenres(film);
        return findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден после создания"));
    }

    public Film update(Film film) {
        checkId(film);
        checkReleaseDate(film);
        checkRating(film);
        checkGenre(film);
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating().getId(),
                film.getId()
        );
        jdbc.update(DELETE_GENRES_BY_FILM_ID, film.getId());
        saveGenres(film);
        return findById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм не найден после обновления"));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbc.update(INSERT_IN_FILM_GENRES_QUERY, film.getId(), genre.getId());
        }
    }

    private void checkReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата выхода не может быть раньше " +
                    "28.12.1895 - даты выхода первого в истории фильма");
        }
    }

    private void checkRating(Film film) {
        Integer count = jdbc.queryForObject(CHECK_RATING_QUERY, Integer.class, film.getMpaRating().getId());
        if (count == 0) {
            throw new ValidationException("Рейтинг с таким id: " + film.getMpaRating().getId() + " - отсутствует");
        }
    }

    private void checkGenre(Film film) {
        for (Genre genre : film.getGenres()) {
            Integer count = jdbc.queryForObject(CHECK_GENRE_QUERY, Integer.class, genre.getId());
            if (count == 0) {
                throw new ValidationException("Жанр с таким id: " + genre.getId() + " - отсутствует");
            }
        }
    }

    void checkId(Film film) {
        Integer count = jdbc.queryForObject(CHECK_ID_QUERY, Integer.class, film.getId());
        if (count == 0) {
            throw new NotFoundException("Фильм с таким id: " + film.getId() + " - отсутствует");
        }
    }
}
