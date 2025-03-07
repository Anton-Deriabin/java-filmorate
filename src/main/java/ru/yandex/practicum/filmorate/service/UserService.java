package ru.yandex.practicum.filmorate.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventRepository;
import ru.yandex.practicum.filmorate.storage.FriendshipRepository;
import ru.yandex.practicum.filmorate.storage.LikeRepository;
import ru.yandex.practicum.filmorate.storage.UserRepository;

import java.util.*;
import java.util.concurrent.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final LikeRepository likeRepository;
    private final FilmService filmService;
    private final EventRepository eventRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(21);

    public UserService(UserRepository userRepository,
                       FriendshipRepository friendshipRepository,
                       LikeRepository likeRepository,
                       FilmService filmService,
                       EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.likeRepository = likeRepository;
        this.filmService = filmService;
        this.eventRepository = eventRepository;
    }

    @Cacheable("users")
    public List<UserDto> findAll() {
        List<User> users = userRepository.findAll();
        return enrichAndMapUsers(users);
    }

    @Cacheable(value = "users", key = "#id")
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

    @Cacheable(value = "friends", key = "#receiver")
    public List<UserDto> getFriends(Long receiver) {
        checkUserExists(receiver);

        return userRepository.getFriends(receiver)
                .stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public void addFriend(Long sender, Long receiver) {
        friendshipRepository.addFriend(sender, receiver);
        eventRepository.addEvent(receiver, sender, EventType.FRIEND, Operation.ADD);
    }

    public void deleteFriend(Long sender, Long receiver) {
        eventRepository.addEvent(receiver, sender, EventType.FRIEND, Operation.REMOVE);
        friendshipRepository.deleteFriend(sender, receiver);
    }

    @Cacheable(value = "commonFriends", key = "#userId + '_' + #friendId")
    public List<UserDto> getCommonFriends(Long userId, Long friendId) {
        return userRepository.getCommonFriends(userId, friendId)
                .stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    @Cacheable(value = "recommendations", key = "#userId")
    public List<FilmDto> getRecommendationsForUser(Long userId) {
        checkUserExists(userId);
        List<Long> filmsLikedByUser = likeRepository.getFilmIdsLikedByUser(userId);
        if (filmsLikedByUser.isEmpty()) {
            return List.of();
        }
        List<Long> usersWithSameTaste = likeRepository.getUserIdsWhoLikedFilms(filmsLikedByUser, userId);
        if (usersWithSameTaste.isEmpty()) {
            return List.of();
        }
        Map<Long, Set<Long>> userToLikedFilmsMap = likeRepository.getUsersWithLikedFilms(usersWithSameTaste);
        long nearestUserIdByTaste = userToLikedFilmsMap.keySet().stream()
                .map((Long id) -> {
                    List<Long> commonFilmsWithLikes = new ArrayList<>(filmsLikedByUser);
                    commonFilmsWithLikes.retainAll(userToLikedFilmsMap.get(id));
                    return Map.of(
                            "userId", id,
                            "commonFilms", (long) commonFilmsWithLikes.size()
                    );
                })
                .max(Comparator.comparingLong(m -> m.get("commonFilms")))
                .orElse(Map.of("userId", -1L))
                .get("userId");

        if (nearestUserIdByTaste < 0) {
            return List.of();
        }
        Set<Long> recommendations = userToLikedFilmsMap.get(nearestUserIdByTaste);
        filmsLikedByUser.forEach(recommendations::remove);
        return filmService.findAllWithIds(recommendations);
    }

    @Cacheable(value = "events", key = "#id")
    public List<Event> getEventFeed(Long id) {
        checkUserExists(id);
        return eventRepository.findFeedForUser(id);
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

    private List<UserDto> enrichAndMapUsers(List<User> users) {
        int totalUsers = users.size();
        int batchSize = (totalUsers + 3) / 4; // Разделим на 4 части, округляя вверх

        List<Callable<List<User>>> tasks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int startIdx = i * batchSize;
            int endIdx = Math.min((i + 1) * batchSize, totalUsers);
            List<User> subList = users.subList(startIdx, endIdx);
            tasks.add(() -> {
                // Здесь можно добавить обогащение пользователей, если необходимо
                return subList;
            });
        }

        try {
            List<Future<List<User>>> futures = executorService.invokeAll(tasks);
            List<User> enrichedUsers = new ArrayList<>();
            for (Future<List<User>> future : futures) {
                enrichedUsers.addAll(future.get());
            }
            return enrichedUsers.stream()
                    .map(UserMapper::mapToUserDto)
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Ошибка при выполнении многопоточной операции", e);
        }
    }
}