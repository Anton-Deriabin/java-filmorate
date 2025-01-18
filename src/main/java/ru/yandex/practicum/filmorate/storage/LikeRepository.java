package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Like;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String FIND_ALL_FILMS_LIKED_BY_USER =
            "SELECT film_id " +
                    "FROM likes " +
                    "WHERE user_id = ?";
    private static final String FIND_USERS_WHO_LIKED_FILMS =
            "SELECT user_id " +
                    "FROM likes " +
                    "WHERE user_id <> ? AND film_id IN (:FILM_IDS)";
    private static final String FIND_ALL_LIKED_FILMS_FOR_ALL_USERS =
            "SELECT user_id, film_id " +
                    "FROM likes " +
                    "WHERE user_id IN (:USER_IDS)";

    public LikeRepository(JdbcTemplate jdbc, RowMapper<Like> mapper, EventRepository eventRepository) {
        super(jdbc, mapper, Like.class);
    }

    public void addLike(Long filmId, Long userId) {
        Integer count = jdbc.queryForObject(CHECK_REQUEST_QUERY, Integer.class, filmId, userId);
        if (count != 0) {
            return;
        }
        insert(INSERT_QUERY, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Integer count = jdbc.queryForObject(CHECK_REQUEST_QUERY, Integer.class, filmId, userId);
        if (count == 0) {
            throw new NotFoundException(String.format("Фильму с id = %d, еще не поставлен поставлен лайк " +
                    "пользователем с id = %d", filmId, userId));
        }
        delete(DELETE_QUERY, filmId, userId);
    }

    public List<Like> findLikesByFilmId(Long filmId) {
        return findMany(FIND_ALL_BY_FILM_ID_QUERY, filmId);
    }

    public List<Long> getFilmIdsLikedByUser(long userId) {
        return jdbc.queryForList(FIND_ALL_FILMS_LIKED_BY_USER, Long.class, userId);
    }

    public List<Long> getUserIdsWhoLikedFilms(List<Long> filmIds, long ignoreUserId) {
        if (filmIds.isEmpty()) {
            return List.of();
        }

        String query = FIND_USERS_WHO_LIKED_FILMS.replace(
                ":FILM_IDS",
                filmIds.stream().map(String::valueOf).collect(Collectors.joining(", "))
        );
        return jdbc.queryForList(query, Long.class, ignoreUserId);
    }

    public Map<Long, Set<Long>> getUsersWithLikedFilms(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        String userIdsCommaSeparated = userIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        String query = FIND_ALL_LIKED_FILMS_FOR_ALL_USERS.replace(":USER_IDS", userIdsCommaSeparated);
        Map<Long, Set<Long>> usersWithLikedFilms = new HashMap<>();

        jdbc.query(query, (ResultSet rs) -> {
            long userId = rs.getInt("user_id");
            long filmId = rs.getInt("film_id");
            usersWithLikedFilms.putIfAbsent(userId, new HashSet<>());
            usersWithLikedFilms.get(userId).add(filmId);
        });

        return usersWithLikedFilms;
    }
}
