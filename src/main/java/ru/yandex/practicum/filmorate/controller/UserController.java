package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final String messageEmailDuplicate = "Этот email уже используется";
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email должен быть указан и должен содержать символ \"@\"");
        }
        for (User value : users.values()) {
            if (user.getEmail().equals(value.getEmail())) {
                throw new DuplicatedDataException(messageEmailDuplicate);
            }
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин должен быть указан и не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть позднее текущей даты");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!newUser.getEmail().contains("@")) {
            throw new ValidationException("Email должен быть указан и должен содержать символ \"@\"");
        }
        for (User value : users.values()) {
            if (newUser.getEmail().equals(value.getEmail())) {
                throw new DuplicatedDataException(messageEmailDuplicate);
            }
        }
        if (newUser.getLogin().contains(" ")) {
            throw new ValidationException("Логин должен быть указан и не должен содержать пробелы");
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        if (newUser.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть позднее текущей даты");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null) {
                for (User value : users.values()) {
                    if (newUser.getEmail().equals(value.getEmail())) {
                        throw new DuplicatedDataException(messageEmailDuplicate);
                    }
                }
            }
            oldUser.setEmail(newUser.getEmail());
            oldUser.setName(newUser.getName());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());
            return oldUser;
        }
        throw new NotFoundException(String.format("Пользователь с id = %d  - не найден", newUser.getId()));
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }
}
