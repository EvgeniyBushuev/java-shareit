package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public User add(User user) {
        checkUserEmail(user.getEmail(), null);

        ++id;
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User update(User user, Long userId) {
        checkUserExists(userId);
        checkUserEmail(user.getEmail(), userId);
        User userUpdate = users.get(userId);

        if (user.getName() != null) {
            userUpdate.setName(user.getName());
        }
        if (user.getEmail() != null) {
            userUpdate.setEmail(user.getEmail());
        }
        return userUpdate;
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void deleteById(Long userId) {
        users.remove(userId);
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    private void checkUserEmail(String email, Long id) {
        boolean isEmailExists = users.values().stream()
                .filter(u -> !Objects.equals(u.getId(), id))
                .anyMatch(u -> Objects.equals(email, u.getEmail()));

        if (isEmailExists) {
            throw new InvalidDataException("Почтовый адрес занят");
        }
    }

    private void checkUserExists(Long id) {
        if (!users.containsKey(id)) {
            throw new BadRequestException("Некорректный идентификатор пользователя");
        }
    }
}
