package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = { "name" })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;
    @NotNull
    @NotBlank
    String name;
    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    String description;
    LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность должна быть положительным числом")
    Integer duration;
}
