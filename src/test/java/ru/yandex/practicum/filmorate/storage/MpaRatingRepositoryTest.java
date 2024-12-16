package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRatingRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaRatingRepository.class, MpaRatingRowMapper.class})
class MpaRatingRepositoryTest {
    private final MpaRatingRepository mpaRatingRepository;

    @Test
    void findAll_ReturnsListOfRatings() {
        List<MpaRating> ratings = mpaRatingRepository.findAll();
        assertThat(ratings).hasSize(5); // Убедитесь, что все рейтинги из data.sql загружены.
    }

    @Test
    void findById_ExistingId_ReturnsRating() {
        Optional<MpaRating> rating = mpaRatingRepository.findById(1L);
        assertThat(rating).isPresent()
                .hasValueSatisfying(r -> assertThat(r.getName()).isEqualTo("G"));
    }

    @Test
    void findById_NonExistingId_ReturnsEmptyOptional() {
        Optional<MpaRating> rating = mpaRatingRepository.findById(999L);
        assertThat(rating).isEmpty();
    }
}


