package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    private final String notFound = "Отзыв с таким id - не найден";
    private static final String FIND_ALL_QUERY =
            "SELECT * " +
            "FROM reviews " +
            "WHERE film_id = ?";
    private static final String FIND_BY_ID_QUERY =
            "SELECT *, " +
            "FROM reviews " +
            "WHERE id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO reviews(content, is_positive, film_id, user_id) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE reviews " +
            "SET content = ?, is_positive = ? " +
            "WHERE id = ?";
    private static final String CHECK_ID_QUERY =
            "SELECT COUNT(*) " +
            "FROM reviews " +
            "WHERE id = ?";
    private static final String DELETE_QUERY =
            "DELETE FROM reviews " +
            "WHERE id = ?";

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper, Review.class);
    }

    public List<Review> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Review> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Review create(Review review) {
        long reviewId = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getFilmId(),
                review.getUserId()
        );
        review.setId(reviewId);
        return  findById(reviewId).orElseThrow(() -> new NotFoundException(notFound));
    }

    public Review update(Review review) {
        checkId(review);
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive()
        );
        return findById(review.getId()).orElseThrow(() -> new NotFoundException(notFound));
    }

    public void delete(Long id) {
        findById(id).orElseThrow(() -> new NotFoundException(notFound));
        delete(
                DELETE_QUERY,
                id
        );
    }

    void checkId(Review review) {
        Integer count = jdbc.queryForObject(CHECK_ID_QUERY, Integer.class, review.getId());
        if (count == 0) {
            throw new NotFoundException(
                    String.format("Отзыв с таким id: %d - отсутствует", review.getId())
            );
        }
    }
}
