package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Id пользователя не может быть null")
    @NotBlank(message = "Id пользователя не может быть пустым")
    Long userId;

    @NotNull(message = "Id события не может быть null")
    @NotBlank(message = "Id события не может быть пустым")
    Long entityId;

    @NotNull(message = "Время события не может быть null")
    @NotBlank(message = "Время события не может быть пустым")
    @Positive(message = "Время должно быть положительным числом")
    Long timestamp;

    @NotNull(message = "Тип события не может быть null")
    @NotBlank(message = "Тип события не может быть пустым")
    EventType eventType;

    @NotNull(message = "Тип операции события не может быть null")
    @NotBlank(message = "Тип операции события не может быть пустым")
    Operation operation;

}
