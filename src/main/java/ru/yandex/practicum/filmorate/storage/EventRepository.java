package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

@Repository
public class EventRepository extends BaseRepository<Event> {
    private static final String EXIST_USER = "SELECT COUNT(*) FROM users WHERE id = ?";
    private static final String EVENTS_FOR_USER_QUERY = """
                SELECT
                  e.id AS id,
                  e.timestamp,
                  e.user_id AS userId,
                  et.name AS eventType,
                  op.name AS operation,
                  e.entity_id AS entityId
                  FROM
                  event e
                JOIN event_type et ON e.entity_type_id = et.id
                JOIN operation op ON e.operation_id = op.id
                WHERE
                  e.user_id = ?
                ORDER BY
                  e.timestamp;
            """;
    private static final String INSERT_EVENT_QUERY =
            "INSERT INTO event (user_id, timestamp, operation_id, entity_id, entity_type_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

    public EventRepository(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper, Event.class);
    }

    public List<Event> findFeedForUser(Long id) {
        return findMany(EVENTS_FOR_USER_QUERY, id);
    }

    public void addEvent(Long userId, Long secondId, EventType eventType, Operation operation) {
        Integer count = jdbc.queryForObject(EXIST_USER, new Object[]{userId}, Integer.class);
        if (count == null || count <= 0) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        Long timeStamp = System.currentTimeMillis();
        Long operationId = getOperationId(operation);
        Long entityTypeId = getEventTypeId(eventType);

        insert(INSERT_EVENT_QUERY, userId, timeStamp, operationId, secondId, entityTypeId);
    }

    private Long getOperationId(Operation operation) {
        return switch (operation) {
            case REMOVE -> 1L;
            case ADD -> 2L;
            case UPDATE -> 3L;
            default -> throw new IllegalArgumentException("Неизвестная операция: " + operation);
        };
    }

    private Long getEventTypeId(EventType eventType) {
        return switch (eventType) {
            case LIKE -> 1L;
            case REVIEW -> 2L;
            case FRIEND -> 3L;
            default -> throw new IllegalArgumentException("Неизвестное событие: " + eventType);
        };
    }
}
