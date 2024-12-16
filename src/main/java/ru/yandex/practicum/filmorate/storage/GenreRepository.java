package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String FIND_ALL_QUERY =
            "SELECT * " +
            "FROM genres";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * " +
            "FROM genres " +
            "WHERE id = ?";
    private static final String FIND_BY_FILM_IDS_QUERY =
            "SELECT fg.id, fg.film_id, g.id AS genre_id, g.name AS genre_name " +
            "FROM film_genres fg " +
            "JOIN genres g ON fg.genre_id = g.id " +
            "WHERE fg.film_id IN (:filmIds) " +
            "ORDER BY fg.film_id, g.id ";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper, Genre.class);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Map<Long, Set<Genre>> findGenresForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String inClause = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        String query = FIND_BY_FILM_IDS_QUERY.replace(":filmIds", inClause);
        Map<Long, Set<Genre>> genresByFilm = new HashMap<>();
        jdbc.query(query, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getLong("genre_id"), rs.getString("genre_name"));
            genresByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        });
        return genresByFilm;
    }
}
