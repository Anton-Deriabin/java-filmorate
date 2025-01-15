package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Event event = new Event();
        event.setId(resultSet.getLong("id"));
        event.setEventType(EventType.valueOf(resultSet.getString("eventType")));
        event.setOperation(Operation.valueOf(resultSet.getString("operation")));
        event.setUserId(resultSet.getLong("userId"));
        event.setEntityId(resultSet.getLong("entityId"));
        event.setTimestamp(resultSet.getLong("timestamp"));
        return event;
    }
}
