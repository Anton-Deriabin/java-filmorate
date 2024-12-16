package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "id" })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Like {
    Long id;

    @NotNull
    Long filmId;

    @NotNull
    Long userId;
}

