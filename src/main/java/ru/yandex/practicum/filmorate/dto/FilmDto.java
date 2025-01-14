package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Like;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class FilmDto {
    private Long id;

    @NotNull(message = "Имя фильма не может быть null")
    @NotBlank(message = "Имя фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть пустой")
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    @NotNull(message = "MPA рейтинг не может быть пустым")
    private MpaRatingDto mpa;

    private Set<GenreDto> genres;

    private Set<Like> likes = new HashSet<>();

    private Set<DirectorDto> directors;
}



