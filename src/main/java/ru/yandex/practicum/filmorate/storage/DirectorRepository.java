package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@Repository
public class DirectorRepository extends BaseRepository<Director> {
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_QUERY =
            "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY =
            "DELETE FROM directors WHERE id = ?";
    private static final String FIND_DIRECTORS_FOR_FILMS_QUERY =
            "SELECT fd.film_id, d.* " +
                    "FROM film_directors fd " +
                    "JOIN directors d ON fd.director_id = d.id " +
                    "WHERE fd.film_id IN (%s) " +
                    "ORDER BY fd.film_id, d.id";

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper, Director.class);
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Director> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Director create(Director director) {
        long id = insert(INSERT_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        if (findById(director.getId()).isEmpty()) {
            throw new NotFoundException(String.format("Режиссер с id=%d не найден", director.getId()));
        }
        update(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    public void delete(Long id) {
        if (findById(id).isEmpty()) {
            throw new NotFoundException(String.format("Режиссер с id=%d не найден", id));
        }
        delete(DELETE_QUERY, id);
    }

    public Map<Long, Set<Director>> findDirectorsForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String inClause = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        String query = String.format(FIND_DIRECTORS_FOR_FILMS_QUERY, inClause);

        Map<Long, Set<Director>> directorsByFilm = new HashMap<>();
        jdbc.query(query, rs -> {
            Long filmId = rs.getLong("film_id");
            Director director = new Director(
                    rs.getLong("id"),
                    rs.getString("name")
            );
            directorsByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(director);
        });
        return directorsByFilm;
    }
}