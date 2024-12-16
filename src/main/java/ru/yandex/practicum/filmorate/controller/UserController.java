package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @GetMapping("/users/{id}")
    public UserDto findUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping("/users")
    public UserDto create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping("/users")
    public UserDto update(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    @PutMapping("/users/{id}/friends/{friend-id}")
    public void addFriend(@PathVariable Long id, @PathVariable("friend-id") Long friendId) {
        userService.addFriend(friendId, id);
    }

    @DeleteMapping("/users/{id}/friends/{friend-id}")
    public void deleteFriend(@PathVariable Long id, @PathVariable("friend-id") Long friendId) {
        userService.deleteFriend(friendId, id);
    }

    @GetMapping("/users/{id}/friends")
    public List<UserDto> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/users/{id}/friends/common/{other-id}")
    public List<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable("other-id") Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}