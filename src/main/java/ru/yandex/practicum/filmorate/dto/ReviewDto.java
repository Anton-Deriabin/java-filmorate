package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDto {
    private Long id;

    @NotNull(message = "Отзыв не может быть null")
    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;

    @NotNull(message = "Статус не может быть null")
    private Boolean isPositive;

    @NotNull
    private Long filmId;

    @NotNull
    private Long userId;

    @NotNull
    private Long useful;
}
