package ru.yandex.practicum.filmorate.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewLikeRepository;
import ru.yandex.practicum.filmorate.storage.ReviewRepository;

import java.util.Comparator;
import java.util.List;

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

    public ReviewDto findById(long id) {
        return reviewRepository.findById(id)
                .map(ReviewMapper::mapToReviewDto)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id=%d не найден", id)));
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
        if (vote != 1 && vote != -1) {
            throw new IllegalArgumentException("Некорректное значение vote: " + vote);
        }

        Integer existingVote;
        try {
            existingVote = reviewLikeRepository.getVote(reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            existingVote = null; // Если голос отсутствует
        }

        if (existingVote != null) {
            if (existingVote == vote) {
                throw new DuplicatedDataException(String.format(
                        "Отзыву с id = %d уже поставлен такой же голос пользователем с id = %d", reviewId, userId));
            } else {
                // Удаляем старый голос и корректируем значение useful
                reviewLikeRepository.deleteVote(reviewId, userId);
                int delta = (existingVote == 1) ? -1 : 1; // Убираем предыдущий голос
                reviewRepository.updateUseful(reviewId, delta);
            }
        }

        // Добавляем новый голос и обновляем useful
        reviewLikeRepository.addVote(reviewId, userId, vote);
        int delta = (vote == 1) ? 1 : -1; // Увеличиваем или уменьшаем useful
        reviewRepository.updateUseful(reviewId, delta);
    }

    public void deleteVote(Long reviewId, Long userId) {
        Integer vote = reviewLikeRepository.getVote(reviewId, userId);
        reviewLikeRepository.deleteVote(reviewId, userId);
        int delta = (vote == 1) ? -1 : 1;
        reviewRepository.updateUseful(reviewId, delta);
    }
}
