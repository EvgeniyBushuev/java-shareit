package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    User update(User user, Long userId);

    Collection<User> getAll();

    void deleteById(Long userId);

    Optional<User> getById(Long id);
}