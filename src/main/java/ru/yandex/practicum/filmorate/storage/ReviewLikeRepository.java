package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.ReviewLike;

@Repository
public class ReviewLikeRepository extends BaseRepository<ReviewLike> {
    private static final String INSERT_QUERY =
            "INSERT INTO review_likes(review_id, user_id, vote) " +
            "VALUES (?, ?, ?)";
    private static final String CHECK_REQUEST_QUERY =
            "SELECT COUNT(*) " +
            "FROM review_likes " +
            "WHERE review_id = ? AND user_id = ?";
    private static final String DELETE_QUERY =
            "DELETE FROM review_likes " +
            "WHERE review_id = ? AND user_id = ?";
    private static final String GET_VOTE_QUERY =
            "SELECT vote " +
            "FROM review_likes " +
            "WHERE review_id = ? AND user_id = ?";

    public ReviewLikeRepository(JdbcTemplate jdbc, RowMapper<ReviewLike> mapper) {
        super(jdbc, mapper, ReviewLike.class);
    }

    public Integer getVote(Long reviewId, Long userId) {
        return jdbc.queryForObject(GET_VOTE_QUERY, Integer.class, reviewId, userId);
    }

    public void addVote(Long reviewId, Long userId, int vote) {
        insert(INSERT_QUERY, reviewId, userId, vote);
    }

    public void deleteVote(Long reviewId, Long userId) {
        Integer count = jdbc.queryForObject(CHECK_REQUEST_QUERY, Integer.class, reviewId, userId);
        if (count == 0) {
            throw new DuplicatedDataException(String.format("Отзыву с id = %d, еще не поставлен поставлен лайк " +
                    "пользователем с id = %d", reviewId, userId));
        }
        delete(DELETE_QUERY, reviewId, userId);
    }
}
