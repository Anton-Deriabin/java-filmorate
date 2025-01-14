package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmRepository;
import ru.yandex.practicum.filmorate.storage.FriendshipRepository;
import ru.yandex.practicum.filmorate.storage.LikeRepository;
import ru.yandex.practicum.filmorate.storage.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final LikeRepository likeRepository;
    private final FilmService filmService;

    public UserService(UserRepository userRepository,
                       FriendshipRepository friendshipRepository,
                       LikeRepository likeRepository,
                       FilmService filmService) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.likeRepository = likeRepository;
        this.filmService = filmService;
    }

    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%d не найден", id)));
    }

    public UserDto create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        checkName(user);
        return UserMapper.mapToUserDto(userRepository.create(user));
    }

    public UserDto update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        checkUserExists(newUser.getId());
        checkName(newUser);
        return UserMapper.mapToUserDto(userRepository.update(newUser));
    }

    public void delete(Long id) {
        userRepository.delete(id);
    }

    public List<UserDto> getFriends(Long receiver) {
        checkUserExists(receiver);

        return userRepository.getFriends(receiver)
                .stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public void addFriend(Long sender, Long receiver) {
        friendshipRepository.addFriend(sender, receiver);
    }

    public void deleteFriend(Long sender, Long receiver) {
        friendshipRepository.deleteFriend(sender, receiver);
    }

    public List<UserDto> getCommonFriends(Long userId, Long friendId) {
        return userRepository.getCommonFriends(userId, friendId)
                .stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<FilmDto> getRecommendationsForUser(Long userId) {
        checkUserExists(userId);

        List<Long> filmsLikedByUser = likeRepository.getFilmIdsLikedByUser(userId);
        List<Long> usersWithSameTaste = likeRepository.getUserIdsWhoLikedFilms(filmsLikedByUser, userId);
        Map<Long, Set<Long>> userToLikedFilmsMap = likeRepository.getUsersWithLikedFilms(usersWithSameTaste);

        long nearestUserIdByTaste = userToLikedFilmsMap.keySet().stream()
                // для каждого ИД юзера вычислить число общих фильмов с исследуемым юзером
                .map((Long id) -> {
                    List<Long> commonFilmsWithLikes = new ArrayList<>(filmsLikedByUser);
                    commonFilmsWithLikes.retainAll(userToLikedFilmsMap.get(id));
                    return Map.of(
                            "userId", id,
                            "commonFilms", (long) commonFilmsWithLikes.size()
                    );
                })
                // отбор наибольшего совпадения
                .max(Comparator.comparingLong(m -> m.get("commonFilms")))
                .orElse(Map.of("userId", -1L))
                .get("userId");

        if (nearestUserIdByTaste < 0) {
            return List.of();
        }

        Set<Long> recommendations = userToLikedFilmsMap.get(nearestUserIdByTaste);
        // удалить из recommendations все фильмы, которые уже лайкал исследуемый юзер
        filmsLikedByUser.forEach(recommendations::remove);
        return filmService.findAllWithIds(recommendations);
    }

    private void checkUserExists(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", userId));
        }
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
