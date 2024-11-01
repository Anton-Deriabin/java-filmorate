package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = { "email" })
public class User {
    private Long id;
    private String email;
    private String name;
    private String login;
    private LocalDate birthday;
}
