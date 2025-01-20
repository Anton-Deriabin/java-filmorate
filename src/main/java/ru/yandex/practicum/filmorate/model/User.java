package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "email" })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    Long id;

    @NotBlank(message = "email не может быть пустым")
    @Email(message = "email должен соответствоввать паттерну email")
    String email;

    String name;

    @NotBlank(message = "логин не может быть пустым")
    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должен содержать пробелы")
    String login;

    @NotNull(message = "день рождения не может быть null")
    @Past
    LocalDate birthday;
}

