package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;

@Repository
public class FriendshipRepository extends BaseRepository<Friendship> {
    private final String notFound = "Один или оба пользователя не существуют";
    private static final String INSERT_QUERY =
            "INSERT INTO friends(sender, receiver) " +
            "VALUES (?, ?)";
    private static final String CHECK_REQUEST_QUERY =
            "SELECT COUNT(*) " +
            "FROM friends " +
            "WHERE sender = ? AND receiver = ?";
    private static final String DELETE_QUERY =
            "DELETE FROM friends " +
            "WHERE sender = ? AND receiver = ?";
    private static final String CHECK_USER_QUERY =
            "SELECT COUNT(*) " +
            "FROM users WHERE id = ?";

    public FriendshipRepository(JdbcTemplate jdbc, RowMapper<Friendship> mapper) {
        super(jdbc, mapper, Friendship.class);
    }

    public void addFriend(Long sender, Long receiver) {
        if (!userExists(sender) || !userExists(receiver)) {
            throw new NotFoundException(notFound);
        }
        Integer count = jdbc.queryForObject(CHECK_REQUEST_QUERY, Integer.class, sender, receiver);
        if (count != null && count > 0) {
            throw new DuplicatedDataException("Запрос на добавление в друзья уже направлен");
        }
        Friendship friendship = new Friendship(null, sender, receiver);
        insert(INSERT_QUERY, friendship.getSender(), friendship.getReceiver());
    }

    public void deleteFriend(Long sender, Long receiver) {
        if (!userExists(sender) || !userExists(receiver)) {
            throw new NotFoundException(notFound);
        }
        delete(DELETE_QUERY, sender, receiver);
    }

    private boolean userExists(Long userId) {
        Integer count = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, userId);
        return count != null && count > 0;
    }
}