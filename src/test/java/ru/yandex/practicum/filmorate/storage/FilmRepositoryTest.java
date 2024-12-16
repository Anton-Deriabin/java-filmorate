package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmRepository.class, FilmRowMapper.class})
class FilmRepositoryTest {
    private final FilmRepository filmRepository;

    @BeforeEach
    void setUp() {
        // Удаляем все существующие фильмы
        filmRepository.findAll().forEach(film -> filmRepository.delete(String.valueOf(film.getId())));
    }

    @Test
    void create_ValidFilm_SavesFilm() {
        Film film = new Film(null, "New Film", "New Description", LocalDate.of(2023, 3, 1), 150, new MpaRating(1L, "G"), Set.of(), Set.of());
        Film savedFilm = filmRepository.create(film);

        assertThat(savedFilm.getId()).isNotNull();
        assertThat(filmRepository.findById(savedFilm.getId())).isPresent();
    }

    @Test
    void findAll_ReturnsListOfFilms() {
        Film film1 = new Film(null, "Film One", "Description One", LocalDate.of(2023, 1, 1), 120, new MpaRating(1L, "G"), Set.of(), Set.of());
        Film film2 = new Film(null, "Film Two", "Description Two", LocalDate.of(2023, 2, 1), 90, new MpaRating(2L, "PG"), Set.of(), Set.of());

        filmRepository.create(film1);
        filmRepository.create(film2);

        List<Film> films = filmRepository.findAll();
        assertThat(films).hasSize(2);
    }
}


