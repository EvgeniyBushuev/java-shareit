package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final InMemoryUserStorage inMemoryUserStorage;

    public UserDto addUser(UserDto userDto) {
        return UserMapper.toUserDto(inMemoryUserStorage.add(UserMapper.toUser(userDto)));
    }

    public UserDto updateUser(UserDto userDto, Long userId) {
        return UserMapper.toUserDto(inMemoryUserStorage.update(UserMapper.toUser(userDto), userId));
    }

    public Collection<UserDto> getAllUsers() {
        return inMemoryUserStorage.getAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(inMemoryUserStorage.getById(userId)
                .orElseThrow(() -> new InvalidDataException("Некорректный идентификатор "
                        + "пользователя " + userId)));
    }

    public void deleteUserById(Long userId) {
        inMemoryUserStorage.deleteById(userId);
    }
}
