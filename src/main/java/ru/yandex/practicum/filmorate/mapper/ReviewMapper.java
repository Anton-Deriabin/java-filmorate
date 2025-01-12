package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {
    public static ReviewDto mapToReviewDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getId());
        dto.setContent(review.getContent());
        dto.setIsPositive(review.getIsPositive());
        dto.setFilmId(review.getFilmId());
        dto.setUserId(review.getUserId());
        dto.setUseful(review.getUseful());
        return dto;
    }

    public static Review mapToReview(ReviewDto dto) {
        Review review = new Review();
        review.setId(dto.getReviewId());
        review.setContent(dto.getContent());
        review.setIsPositive(dto.getIsPositive());
        review.setFilmId(dto.getFilmId());
        review.setUserId(dto.getUserId());
        review.setUseful(dto.getUseful());
        return review;
    }
}
