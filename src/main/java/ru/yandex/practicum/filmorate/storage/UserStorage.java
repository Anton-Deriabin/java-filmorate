package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    Optional<User> findById(Long id);

    void updateFriends(User user);

    void updateLikes(User user);
}
