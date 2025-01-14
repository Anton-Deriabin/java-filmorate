package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewLikeRowMapper implements RowMapper<ReviewLike> {
    @Override
    public ReviewLike mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setId(rs.getLong("id"));
        reviewLike.setReviewId(rs.getLong("review_id"));
        reviewLike.setUserId(rs.getLong("user_id"));
        reviewLike.setVote(rs.getInt("vote"));
        return reviewLike;
    }
}
