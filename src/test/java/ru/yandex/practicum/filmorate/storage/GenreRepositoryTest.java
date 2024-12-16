package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreRepository.class, GenreRowMapper.class})
class GenreRepositoryTest {
    private final GenreRepository genreRepository;

    @Test
    void findAll_ReturnsListOfGenres() {
        List<Genre> genres = genreRepository.findAll();
        assertThat(genres).hasSize(6); // Убедитесь, что все жанры из data.sql загружены.
    }

    @Test
    void findById_ExistingId_ReturnsGenre() {
        Optional<Genre> genre = genreRepository.findById(1L);
        assertThat(genre).isPresent()
                .hasValueSatisfying(g -> assertThat(g.getName()).isEqualTo("Комедия"));
    }

    @Test
    void findById_NonExistingId_ReturnsEmptyOptional() {
        Optional<Genre> genre = genreRepository.findById(999L);
        assertThat(genre).isEmpty();
    }
}


