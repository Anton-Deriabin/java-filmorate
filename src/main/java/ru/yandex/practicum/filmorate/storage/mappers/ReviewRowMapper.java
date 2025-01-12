package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Review review = new Review();
        review.setId(resultSet.getLong("id"));
        review.setContent(resultSet.getString("content"));
        review.setIsPositive(resultSet.getBoolean("isPositive"));
        review.setFilmId(resultSet.getLong("filmId"));
        review.setUserId(resultSet.getLong("userId"));
        review.setUseful(resultSet.getLong("useful"));
        return review;
    }
}
