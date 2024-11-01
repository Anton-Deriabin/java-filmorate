package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = { "name" })
public class Film {
    private Long id;
    @NotNull
    @NotBlank
    private String name;
    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;
    private LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;
}
