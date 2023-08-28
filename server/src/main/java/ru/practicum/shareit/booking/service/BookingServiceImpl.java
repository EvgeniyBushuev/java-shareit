package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (!(booking.getUser().getId().equals(userId)
                || booking.getItem().getOwner().getId().equals(userId))) {
            throw new NotFoundException("Бронь для пользователя: " + userId + "не найдена");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByState(RequestBookingState requestBookingState,
            Long userId, int from, int size) {

        LocalDateTime now = LocalDateTime.now();

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Pageable pageable = PageRequest.of(from / size, size);

        switch (requestBookingState) {
            case ALL:
                return bookingRepository.findAllByUserIdOrderByStartDesc(userId, pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByUserIdAndEndBeforeOrderByStartDesc(userId, now,
                                pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByUserIdAndStartAfterOrderByStartDesc(userId, now,
                                pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                now, now, pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING,
                                pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED,
                                pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException("Не поддерживаемый статус в запросе");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByStateForOwner(RequestBookingState requestBookingState, Long userId,
                                                          int from, int size) {
        LocalDateTime now = LocalDateTime.now();

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Pageable pageable = PageRequest.of(from, size);

        switch (requestBookingState) {
            case ALL:
                return bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId, pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now,
                                pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now,
                                pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                now, now, pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING,
                                pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED,
                                pageable)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException("Не поддерживаемый статус в запросе");
        }
    }

    @Override
    @Transactional
    public BookingResponseDto addBooking(BookingRequestDto bookingRequestDto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.fromDto(bookingRequestDto);
        booking.setUser(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long bookingId, boolean approved, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Подтверждение доступно только для владельца вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Вещь не ожидает подтверждения");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }
}
