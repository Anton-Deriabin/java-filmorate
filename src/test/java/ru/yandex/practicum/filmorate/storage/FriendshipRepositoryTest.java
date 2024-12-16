package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendshipRepository.class, FriendshipRowMapper.class, UserRepository.class, UserRowMapper.class})
class FriendshipRepositoryTest {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setName("User One");
        user1.setLogin("userone");
        user1.setBirthday(LocalDate.of(2000, 1, 1));
        user1 = userRepository.create(user1);
        userId1 = user1.getId();

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
        user2.setLogin("usertwo");
        user2.setBirthday(LocalDate.of(1995, 5, 15));
        user2 = userRepository.create(user2);
        userId2 = user2.getId();
    }

    @Test
    void addFriend_ValidFriendship_AddsFriendship() {
        friendshipRepository.addFriend(userId1, userId2);
        var friends = userRepository.getFriends(userId2);
        assertThat(friends).anyMatch(friend -> friend.getId().equals(userId1));
    }

    @Test
    void deleteFriend_ValidFriendship_DeletesFriendship() {
        friendshipRepository.addFriend(userId1, userId2);
        friendshipRepository.deleteFriend(userId1, userId2);
        var friends = userRepository.getFriends(userId2);
        assertThat(friends).noneMatch(friend -> friend.getId().equals(userId1));
    }
}
