package ru.yandex.practicum.filmorate.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MpaRatingDto implements Serializable {
    static final long serialVersionUID = 1L;
    Long id;
    String name;
}
