package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRatingRepository extends BaseRepository<MpaRating> {
    private static final String FIND_ALL_QUERY =
            "SELECT * " +
            "FROM ratings";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * " +
            "FROM ratings " +
            "WHERE id = ?";

    public MpaRatingRepository(JdbcTemplate jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper, MpaRating.class);
    }

    public List<MpaRating> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<MpaRating> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}
