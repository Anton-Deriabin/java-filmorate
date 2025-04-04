package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        LikeRepository.class,
        LikeRowMapper.class,
        FilmRepository.class,
        FilmRowMapper.class,
        UserRepository.class,
        UserRowMapper.class,
        GenreRepository.class,
        GenreRowMapper.class,
        DirectorRepository.class,
        DirectorRowMapper.class
})
class LikeRepositoryTest {
    private final LikeRepository likeRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    private Long filmId;
    private Long userId;
    private Long mark;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user = userRepository.create(user);
        userId = user.getId();

        Like like = new Like();
        like.setMark(10L);
        mark = like.getMark();

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Film Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120);
        film.setMpaRating(new ru.yandex.practicum.filmorate.model.MpaRating(1L, "G"));
        film.setGenres(new HashSet<>());
        film.setDirectors(new HashSet<>());
        film.setLikes(new HashSet<>());
        film = filmRepository.create(film);
        filmId = film.getId();
    }

    @Test
    void addLike_ValidLike_AddsLike() {
        likeRepository.addLike(filmId, userId, (double) mark);
        List<Like> likes = likeRepository.findLikesByFilmId(filmId);
        assertThat(likes).anyMatch(like -> like.getUserId().equals(userId));
    }

    @Test
    void deleteLike_ValidLike_DeletesLike() {
        likeRepository.addLike(filmId, userId, (double) mark);
        likeRepository.deleteLike(filmId, userId);
        List<Like> likes = likeRepository.findLikesByFilmId(filmId);
        assertThat(likes).noneMatch(like -> like.getUserId().equals(userId));
    }

    @Test
    void findLikesByFilmId_ReturnsListOfLikes() {
        likeRepository.addLike(filmId, userId, (double) mark);
        List<Like> likes = likeRepository.findLikesByFilmId(filmId);
        assertThat(likes).hasSize(1);
    }
}