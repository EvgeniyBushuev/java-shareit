package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, Long userId);

    List<UserDto> getAllUsers();

    UserDto getUserById(Long userId);

    void deleteUserById(Long userId);
}
