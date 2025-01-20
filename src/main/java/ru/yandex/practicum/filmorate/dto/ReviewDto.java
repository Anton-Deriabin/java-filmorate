package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewDto {
    Long reviewId;

    @NotBlank(message = "Отзыв не может быть пустым")
    String content;

    @NotNull(message = "Статус не может быть null")
    Boolean isPositive;

    @NotNull
    Long filmId;

    @NotNull
    Long userId;

    Long useful;
}
