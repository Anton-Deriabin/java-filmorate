package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = { "email" })
public class User {
    private Long id;
    @NotNull
    @NotBlank
    @Email
    private String email;
    private String name;
    @NotNull
    @NotBlank
    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должен содержать пробелы")
    private String login;
    @NotNull
    @Past
    private LocalDate birthday;
}
