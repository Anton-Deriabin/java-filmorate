-- Наполнение таблицы ratings
MERGE INTO ratings (id, name) KEY(id)
VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

-- Наполнение таблицы genres
MERGE INTO genres (id, name) KEY(id)
VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

-- Наполнение таблицы event_type
MERGE INTO event_type (id, name) KEY(id)
VALUES
(1, 'LIKE'),
(2, 'REVIEW'),
(3, 'FRIEND');

-- Наполнение таблицы operation
MERGE INTO operation (id, name) KEY(id)
VALUES
(1, 'REMOVE'),
(2, 'ADD'),
(3, 'UPDATE');
