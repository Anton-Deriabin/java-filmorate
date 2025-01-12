package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewLikeRepository;
import ru.yandex.practicum.filmorate.storage.ReviewRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    public ReviewService(ReviewRepository reviewRepository, ReviewLikeRepository reviewLikeRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
    }

    public List<ReviewDto> findAll(Long filmId, Integer count) {
        List<Review> reviews = reviewRepository.findAll();
        if (filmId != null) {
            reviews = reviews.stream()
                    .filter(review -> review.getFilmId().equals(filmId))
                    .toList();
        }
        int limit = (count != null && count > 0) ? count : 10;
        return reviews.stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(limit)
                .map(ReviewMapper::mapToReviewDto)
                .toList();
    }

    public Optional<ReviewDto> findById(long id) {
        Optional<Review> reviewOptional = reviewRepository.findById(id);
        if (reviewOptional.isPresent()) {
            Review review = reviewOptional.get();
            return Optional.of(ReviewMapper.mapToReviewDto(review));
        }
        return Optional.empty();
    }

    public ReviewDto create(Review review) {
        return ReviewMapper.mapToReviewDto(reviewRepository.create(review));
    }

    public ReviewDto update(Review review) {
        return ReviewMapper.mapToReviewDto(reviewRepository.update(review));
    }

    public void delete(Long id) {
        reviewRepository.delete(id);
    }

    public void addVote(Long reviewId, Long userId, int vote) {
        reviewLikeRepository.addVote(reviewId, userId, vote);
    }

    public void deleteVote(Long reviewId, Long userId) {
        reviewLikeRepository.deleteVote(reviewId, userId);
    }
}
