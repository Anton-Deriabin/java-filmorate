package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserRepository.class, UserRowMapper.class})
class UserRepositoryTest {
    private final UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.findAll().forEach(user -> userRepository.delete(String.valueOf(user.getId())));
    }

    @Test
    void create_ValidUser_SavesUser() {
        User user = new User(null, "test@example.com", "Test User", "testuser", LocalDate.of(2000, 1, 1));
        User createdUser = userRepository.create(user);
        assertThat(createdUser.getId()).isNotNull();
        Optional<User> retrievedUser = userRepository.findById(createdUser.getId());
        assertThat(retrievedUser).isPresent()
                .hasValueSatisfying(u -> assertThat(u.getEmail()).isEqualTo("test@example.com"));
    }

    @Test
    void update_ValidUser_UpdatesUser() {
        User user = new User(null, "test@example.com", "Test User", "testuser", LocalDate.of(2000, 1, 1));
        User createdUser = userRepository.create(user);
        createdUser.setEmail("updated@example.com");
        createdUser.setName("Updated User");
        User updatedUser = userRepository.update(createdUser);
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(userRepository.findById(updatedUser.getId()))
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getName()).isEqualTo("Updated User"));
    }

    @Test
    void findAll_ReturnsListOfUsers() {
        User user1 = new User(null, "user1@example.com", "User One", "userone", LocalDate.of(1990, 1, 1));
        User user2 = new User(null, "user2@example.com", "User Two", "usertwo", LocalDate.of(1995, 5, 5));
        userRepository.create(user1);
        userRepository.create(user2);
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
    }
}


