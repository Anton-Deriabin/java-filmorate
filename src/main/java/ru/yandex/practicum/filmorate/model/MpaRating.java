package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(of = { "id" })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MpaRating {
    Long id;

    @NotNull
    String name;
}

