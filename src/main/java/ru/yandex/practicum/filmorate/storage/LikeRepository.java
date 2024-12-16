package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.List;

@Repository
public class LikeRepository extends BaseRepository<Like> {
    private static final String INSERT_QUERY =
            "INSERT INTO likes(film_id, user_id) " +
            "VALUES (?, ?)";
    private static final String CHECK_REQUEST_QUERY =
            "SELECT COUNT(*) " +
            "FROM likes " +
            "WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_QUERY =
            "DELETE FROM likes " +
            "WHERE film_id = ? AND user_id = ?";
    private static final String FIND_ALL_BY_FILM_ID_QUERY =
            "SELECT * " +
            "FROM likes " +
            "WHERE film_id = ?";

    public LikeRepository(JdbcTemplate jdbc, RowMapper<Like> mapper) {
        super(jdbc, mapper, Like.class);
    }

    public void addLike(Long filmId, Long userId) {
        Integer count = jdbc.queryForObject(CHECK_REQUEST_QUERY, Integer.class, filmId, userId);
        if (count != 0) {
            throw new DuplicatedDataException("Фильму с id = " + filmId + ", уже поставлен лайк пользователем " +
                    "с id = " + userId);
        }
        insert(INSERT_QUERY, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Integer count = jdbc.queryForObject(CHECK_REQUEST_QUERY, Integer.class, filmId, userId);
        if (count == 0) {
            throw new DuplicatedDataException("Фильму с id = " + filmId + ", еще не поставлен лайк пользователем " +
                    "с id = " + userId);
        }
        delete(DELETE_QUERY, filmId, userId);
    }

    public List<Like> findLikesByFilmId(Long filmId) {
        return findMany(FIND_ALL_BY_FILM_ID_QUERY, filmId);
    }
}
