package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    public UserDto addUser(UserDto userDto) {
        return UserMapper.toUserDto(userStorage.add(UserMapper.toUser(userDto)));
    }

    public UserDto updateUser(UserDto userDto, Long userId) {
        userStorage.getById(userId).orElseThrow(() -> new BadRequestException("Некорректный "
                + "идентификатор пользователя"));
        return UserMapper.toUserDto(userStorage.update(UserMapper.toUser(userDto), userId));
    }

    public Collection<UserDto> getAllUsers() {
        return userStorage.getAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(userStorage.getById(userId)
                .orElseThrow(() -> new InvalidDataException("Некорректный идентификатор "
                        + "пользователя " + userId)));
    }

    public void deleteUserById(Long userId) {
        userStorage.deleteById(userId);
    }
}
