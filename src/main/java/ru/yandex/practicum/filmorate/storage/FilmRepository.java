package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private final String notFound = "Фильм с таким id - не найден";
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
    private static final String DELETE_DIRECTORS_BY_FILM_ID =
            "DELETE FROM film_directors " +
                    "WHERE film_id = ?";
    private static final String INSERT_IN_FILM_DIRECTORS_QUERY =
            "INSERT INTO film_directors(film_id, director_id) " +
                    "VALUES (?, ?)";
    private static final String FIND_FILMS_BY_DIRECTOR_QUERY =
            "SELECT f.*, r.name AS rating_name " +
                    "FROM films f " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN ratings r ON f.rating_id = r.id " +
                    "WHERE fd.director_id = ?";

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
        saveDirectors(film);
        return findById(filmId).orElseThrow(() -> new NotFoundException(notFound));
    }

    public Film update(Film film) {
        checkId(film);
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
        jdbc.update(DELETE_DIRECTORS_BY_FILM_ID, film.getId());
        jdbc.update(DELETE_GENRES_BY_FILM_ID, film.getId());
        saveGenres(film);
        saveDirectors(film);
        return findById(film.getId()).orElseThrow(() -> new NotFoundException(notFound));
    }

    public List<Film> findFilmsByDirector(Long directorId) {
        return findMany(FIND_FILMS_BY_DIRECTOR_QUERY, directorId);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbc.update(INSERT_IN_FILM_GENRES_QUERY, film.getId(), genre.getId());
        }
    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }
        for (Director director : film.getDirectors()) {
            jdbc.update(INSERT_IN_FILM_DIRECTORS_QUERY, film.getId(), director.getId());
        }
    }

    private void checkRating(Film film) {
        Integer count = jdbc.queryForObject(CHECK_RATING_QUERY, Integer.class, film.getMpaRating().getId());
        if (count == 0) {
            throw new ValidationException(
                    String.format("Рейтинг с таким id: %d - отсутствует", film.getMpaRating().getId())
            );
        }
    }

    private void checkGenre(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            Integer count = jdbc.queryForObject(CHECK_GENRE_QUERY, Integer.class, genre.getId());
            if (count == 0) {
                throw new ValidationException(
                        String.format("Жанр с таким id: %d - отсутствует", genre.getId())
                );
            }
        }
    }

    void checkId(Film film) {
        Integer count = jdbc.queryForObject(CHECK_ID_QUERY, Integer.class, film.getId());
        if (count == 0) {
            throw new NotFoundException(
                    String.format("Фильм с таким id: %d - отсутствует", film.getId())
            );
        }
    }
}