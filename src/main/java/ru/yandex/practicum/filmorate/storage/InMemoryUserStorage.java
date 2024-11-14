package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final String messageEmailDuplicate = "Этот email уже используется";
    @Getter
    protected final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Коллекция пользователей отправлена по запросу");
        return users.values();
    }

    @PostMapping
    public User create(User user) {
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
    public User update(User newUser) {
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

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public void updateFriends(User user) {
        if (users.containsKey(user.getId())) {
            User existingUser = users.get(user.getId());
            existingUser.setFriends(user.getFriends());
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public void updateLikes(User user) {
        if (users.containsKey(user.getId())) {
            User existingUser = users.get(user.getId());
            existingUser.setLikedFilms(user.getLikedFilms());  // Обновляем только список лайков
            log.debug("Пользователь с id = {} обновлён с новым списком лайков", user.getId());
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }
}
