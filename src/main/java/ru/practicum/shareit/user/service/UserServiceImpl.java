package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.fromDto(userDto);
        try {
            return UserMapper.toDto(userRepository.save(user));
        } catch (Exception e) {
            throw new InvalidDataException("Email занят");
        }
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto, Long userId) {

        User stored = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + userId));

        Optional.ofNullable(userDto.getName()).ifPresent(stored::setName);
        Optional.ofNullable(userDto.getEmail()).ifPresent(stored::setEmail);

            try {
                return UserMapper.toDto(userRepository.save(stored));
            } catch (DataIntegrityViolationException e) {
                throw new InvalidDataException("Ошибка валидации данных");
            }
    }

    @Override
    @Transactional
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto getUserById(Long userId) {
        return UserMapper.toDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + userId)));
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }
}
