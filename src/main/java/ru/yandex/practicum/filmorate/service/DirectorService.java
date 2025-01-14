package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public List<DirectorDto> findAll() {
        return directorRepository.findAll()
                .stream()
                .map(DirectorMapper::mapToDirectorDto)
                .toList();
    }

    public DirectorDto findById(Long id) {
        return directorRepository.findById(id)
                .map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> new NotFoundException(String.format("Режиссер с id=%d не найден", id)));
    }

    public DirectorDto create(Director director) {
        return DirectorMapper.mapToDirectorDto(directorRepository.create(director));
    }

    public DirectorDto update(Director director) {
        return DirectorMapper.mapToDirectorDto(directorRepository.update(director));
    }

    public void delete(Long id) {
        directorRepository.delete(id);
    }
}