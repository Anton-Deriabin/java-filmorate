package ru.yandex.practicum.filmorate.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventRepository;
import ru.yandex.practicum.filmorate.storage.ReviewLikeRepository;
import ru.yandex.practicum.filmorate.storage.ReviewRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final EventRepository eventRepository;

    public ReviewService(ReviewRepository reviewRepository, ReviewLikeRepository reviewLikeRepository, EventRepository eventRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.eventRepository = eventRepository;
    }

    @Cacheable("reviews")
    public List<ReviewDto> findAll(Long filmId, Integer count) {
        List<Review> reviews = reviewRepository.findAll();
        if (filmId != null) {
            reviews = reviews.stream()
                    .filter(review -> review.getFilmId().equals(filmId))
                    .toList();
        }
        int limit = (count != null && count > 0) ? count : 10;

        return reviews.stream()
                .map(review -> {
                    int useful = reviewRepository.calculateUseful(review.getId());
                    return ReviewMapper.mapToReviewDto(review, (long) useful);
                })
                .sorted(Comparator.comparing(ReviewDto::getUseful).reversed())
                .limit(limit)
                .toList();
    }

    @Cacheable(value = "reviews", key = "#id")
    public ReviewDto findById(long id) {
        return reviewRepository.findById(id)
                .map(review -> {
                    int useful = reviewRepository.calculateUseful(review.getId());
                    return ReviewMapper.mapToReviewDto(review, (long) useful);
                })
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%d не найден", id)));
    }


    public ReviewDto create(Review review) {
        Review createdReview = reviewRepository.create(review);
        int useful = reviewRepository.calculateUseful(createdReview.getId());
        eventRepository.addEvent(review.getUserId(), review.getId(), EventType.REVIEW, Operation.ADD);
        return ReviewMapper.mapToReviewDto(createdReview, (long) useful);
    }

    public ReviewDto update(Review review) {
        Review updatedReview = reviewRepository.update(review);
        int useful = reviewRepository.calculateUseful(updatedReview.getId());
        eventRepository.addEvent(updatedReview.getUserId(), updatedReview.getId(), EventType.REVIEW, Operation.UPDATE);
        return ReviewMapper.mapToReviewDto(updatedReview, (long) useful);
    }

    public void delete(Long id) {
        ReviewDto reviewDto = findById(id);
        eventRepository.addEvent(reviewDto.getUserId(), reviewDto.getReviewId(), EventType.REVIEW, Operation.REMOVE);
        reviewRepository.delete(id);
    }

    public void addVote(Long reviewId, Long userId, int vote) {
        if (vote != 1 && vote != -1) {
            throw new IllegalArgumentException("Некорректное значение vote: " + vote);
        }
        Integer existingVote;
        try {
            existingVote = reviewLikeRepository.getVote(reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            existingVote = null;
        }
        if (existingVote != null) {
            if (existingVote == vote) {
                throw new DuplicatedDataException(String.format(
                        "Отзыву с id = %d уже поставлен такой же vote пользователем с id = %d", reviewId, userId));
            } else {
                reviewLikeRepository.deleteVote(reviewId, userId);
            }
        }
        reviewLikeRepository.addVote(reviewId, userId, vote);
    }

    public void deleteVote(Long reviewId, Long userId) {
        reviewLikeRepository.deleteVote(reviewId, userId);
    }
}
