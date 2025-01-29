package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "content" })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    Long id;
    String content;
    Boolean isPositive;
    Long filmId;
    Long userId;
    Long useful;
}
