package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto implements Serializable {
    static final long serialVersionUID = 1L;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;
    String email;
    String name;
    String login;
    LocalDate birthday;
}
