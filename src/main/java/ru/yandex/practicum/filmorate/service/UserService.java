package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final String message = "Пользователь не найден";
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public User addFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(message));
        User userFriend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException(message));
        if (user.getFriends().contains(friendId)) {
            log.error("Попытка добавить в друзья пользователя уже являющегося другом");
            throw new ValidationException("Пользователь уже является другом");
        }
        user.getFriends().add(friendId);
        log.debug("В список друзей пользователя с id = {} добавлен пользователь с id = {}",
                user.getId(),
                userFriend.getId());
        userFriend.getFriends().add(userId);
        log.debug("В список друзей пользователя с id = {} добавлен пользователь с id = {}",
                userFriend.getId(),
                user.getId());
        userStorage.updateFriends(user);
        log.debug("Пользователь с id = {} с увеличенным количеством друзей обновлен в коллекции пользователей",
                user.getName());
        userStorage.updateFriends(userFriend);
        log.debug("Пользователь с id = {} с увеличенным количеством друзей обновлен в коллекции пользователей",
                userFriend.getName());
        log.info("Пользователь с id = {} добавил в друзья пользователя с id = {}", user.getId(), userFriend.getId());
        return user;
    }

    public User deleteFriend(Long userId, Long notFriendAnymoreId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(message));
        User userNotFriendAnymore = userStorage.findById(notFriendAnymoreId)
                .orElseThrow(() -> new NotFoundException(message));
        user.getFriends().remove(notFriendAnymoreId);
        log.debug("Из списка друзей пользователя с id = {} удален пользователь с id = {}",
                user.getId(),
                userNotFriendAnymore.getId());
        userNotFriendAnymore.getFriends().remove(userId);
        log.debug("Из списка друзей пользователя с id = {} удален пользователь с id = {}",
                userNotFriendAnymore.getId(),
                user.getId());
        userStorage.updateFriends(user);
        log.debug("Пользователь с id = {} с уменьшенным количеством друзей обновлен в коллекции пользователей",
                user.getName());
        userStorage.updateFriends(userNotFriendAnymore);
        log.debug("Пользователь с id = {} с уменьшенным количеством друзей обновлен в коллекции пользователей",
                userNotFriendAnymore.getId());
        log.info("Пользователь с id = {} удалил из друзей пользователя с id = {}",
                user.getId(),
                userNotFriendAnymore.getId());
        return user;
    }

    public Collection<User> getFriends(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(message));
        log.info("Получен список друзей пользователя с id = {}", user.getId());
        return user.getFriends().stream()
                .map(friendId -> userStorage.findById(friendId)
                        .orElseThrow(() -> new NotFoundException(message)))
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(message));
        User userFriend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException(message));
        Set<Long> currentUserFriends = user.getFriends();
        Set<Long> friendFriends = userFriend.getFriends();
        log.info("Получен список общих друзей пользователя с id = {} и пользователя с id = {}",
                user.getId(),
                userFriend.getId());
        return currentUserFriends.stream()
                .filter(friendFriends::contains)
                .map(id -> userStorage.findById(id)
                        .orElseThrow(() -> new NotFoundException(message)))
                .collect(Collectors.toList());
    }
}
