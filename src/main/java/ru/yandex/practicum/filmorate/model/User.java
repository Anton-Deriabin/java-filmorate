package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String login;
    @NotNull
    private LocalDate birthday;
}
