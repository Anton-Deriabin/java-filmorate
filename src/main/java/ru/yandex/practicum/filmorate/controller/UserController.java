package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final String messageEmailDuplicate = "Этот email уже используется";
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Коллекция пользователей отправлена по запросу");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        for (User value : users.values()) {
            if (user.getEmail().equals(value.getEmail())) {
                log.error("Попытка занять уже используемый email при добавлении");
                throw new DuplicatedDataException(messageEmailDuplicate);
            }
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Вместо имени использован логин при добавлении");
        }
        user.setId(getNextId());
        log.debug("Пользователю \"{}\" назначен id = {}", user.getName(), user.getId());
        users.put(user.getId(), user);
        log.info("Пользователь с id = {}  - добавлен", user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.error("Id пользователя для обновления не указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.debug("Вместо имени использован логин при обновлении");
            newUser.setName(newUser.getLogin());
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            log.trace("Создали переменную старого пользователя для сравнения с новым");
            if (newUser.getEmail() != null) {
                for (User value : users.values()) {
                    if (newUser.getEmail().equals(value.getEmail())) {
                        log.error("Попытка занять уже используемый email при обновлении");
                        throw new DuplicatedDataException(messageEmailDuplicate);
                    }
                }
            }
            oldUser.setEmail(newUser.getEmail());
            log.debug("Пользователю с id = {} установлен email - {}", newUser.getId(), newUser.getEmail());
            oldUser.setName(newUser.getName());
            log.debug("Пользователю с id = {} установлено имя - {}", newUser.getId(), newUser.getName());
            oldUser.setLogin(newUser.getLogin());
            log.debug("Пользователю с id = {} установлен логин - {}", newUser.getId(), newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());
            log.debug("Пользователю с id = {} установлена дата рождения - {}", newUser.getId(), newUser.getBirthday());
            log.info("Пользователь \"{}\" с id = {}  - обновлен", newUser.getName(), newUser.getId());
            return oldUser;
        }
        log.error("Попытка обновить пользователя с несуществующим id = {}", newUser.getId());
        throw new NotFoundException(String.format("Пользователь с id = %d  - не найден", newUser.getId()));
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        log.debug("Cоздали новый id = {} ", currentMaxId);
        return ++currentMaxId;
    }
}
