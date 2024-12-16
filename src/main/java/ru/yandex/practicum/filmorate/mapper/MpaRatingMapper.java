package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MpaRatingMapper {
    public static MpaRatingDto mapToMpaRatingDto(MpaRating mpaRating) {
        MpaRatingDto mpaRatingDto = new MpaRatingDto();
        mpaRatingDto.setId(mpaRating.getId());
        mpaRatingDto.setName(mpaRating.getName());
        return mpaRatingDto;
    }
}
