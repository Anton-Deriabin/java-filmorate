package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    Long eventId;

    @NotBlank(message = "Id пользователя не может быть пустым")
    Long userId;

    @NotBlank(message = "Id события не может быть пустым")
    Long entityId;

    @NotBlank(message = "Время события не может быть пустым")
    @Positive(message = "Время должно быть положительным числом")
    Long timestamp;

    @NotBlank(message = "Тип события не может быть пустым")
    EventType eventType;

    @NotBlank(message = "Тип операции события не может быть пустым")
    Operation operation;

}
