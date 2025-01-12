package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "content" })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    Long id;

    @NotNull(message = "Отзыв не может быть null")
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
