package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaRatingMapper;
import ru.yandex.practicum.filmorate.storage.MpaRatingRepository;

import java.util.List;

@Service
public class MpaRatingService {
    private final MpaRatingRepository mpaRatingRepository;

    public MpaRatingService(MpaRatingRepository mpaRatingRepository) {
        this.mpaRatingRepository = mpaRatingRepository;
    }

    public List<MpaRatingDto> findAll() {
        return mpaRatingRepository.findAll().stream()
                .map(MpaRatingMapper::mapToMpaRatingDto)
                .toList();
    }

    public MpaRatingDto findById(Long id) {
        return mpaRatingRepository.findById(id)
                .map(MpaRatingMapper::mapToMpaRatingDto)
                .orElseThrow(() -> new NotFoundException(String.format("Рейтинг с id=%d не найден", id)));
    }
}
