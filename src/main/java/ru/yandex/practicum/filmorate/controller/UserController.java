package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final String usersIdPath = "/{id}";
    private final String friendsIdPath = "/{id}/friends/{friend-id}";
    private final String friendsPath = "/{id}/friends";
    private final String commonFriendsPath = "/{id}/friends/common/{other-id}";
    private final String recommendationsPath = "/{id}/recommendations";
    private final String getFeed = "/{id}/feed";
    private final UserService userService;

    @GetMapping()
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @GetMapping(usersIdPath)
    public UserDto findUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping()
    public UserDto create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping()
    public UserDto update(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    @DeleteMapping(usersIdPath)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @PutMapping(friendsIdPath)
    public void addFriend(@PathVariable Long id, @PathVariable("friend-id") Long friendId) {
        userService.addFriend(friendId, id);
    }

    @DeleteMapping(friendsIdPath)
    public void deleteFriend(@PathVariable Long id, @PathVariable("friend-id") Long friendId) {
        userService.deleteFriend(friendId, id);
    }

    @GetMapping(friendsPath)
    public List<UserDto> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping(commonFriendsPath)
    public List<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable("other-id") Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping(recommendationsPath)
    public List<FilmDto> getRecommendationsForUser(@PathVariable Long id) {
        return userService.getRecommendationsForUser(id);
    }

    @GetMapping(getFeed)
    public List<Event> getEventFeed(@PathVariable Long id) {
        return userService.getEventFeed(id);
    }
}