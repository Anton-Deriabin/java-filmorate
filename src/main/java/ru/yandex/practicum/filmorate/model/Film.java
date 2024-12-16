package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "name" })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;

    @NotNull(message = "Имя фильма не может быть null")
    @NotBlank(message = "Имя фильма не может быть пустым")
    String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть пустой")
    @Positive(message = "Продолжительность должна быть положительным числом")
    Integer duration;

    @NotNull(message = "MPA рейтинг не может быть пустым")
    MpaRating mpaRating;

    Set<Genre> genres = new HashSet<>();

    private Set<Like> likes = new HashSet<>();
}

