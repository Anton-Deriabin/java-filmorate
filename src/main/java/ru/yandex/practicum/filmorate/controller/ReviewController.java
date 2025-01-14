package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewDto> findReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false, defaultValue = "10") Integer count) {
        return reviewService.findAll(filmId, count);
    }

    @GetMapping("/{id}")
    public ReviewDto findReview(@PathVariable Long id) {
        return reviewService.findById(id);
    }

    @PostMapping
    public ReviewDto create(@Valid @RequestBody ReviewDto reviewDto) {
        return reviewService.create(ReviewMapper.mapToReview(reviewDto));
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody ReviewDto reviewDto) {
        return reviewService.update(ReviewMapper.mapToReview(reviewDto));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addVote(id, userId, 1);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addVote(id, userId, -1);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteVote(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteVote(id, userId);
    }
}
