package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto addBooking(BookingRequestDto bookingRequestDto, Long userId);

    BookingResponseDto getById(Long bookingId, Long userId);

    List<BookingResponseDto> getAllByState(RequestBookingState requestBookingState, Long userId);

    List<BookingResponseDto> getAllByStateForOwner(RequestBookingState requestBookingState,
            Long userId);

    BookingResponseDto approve(Long bookingId, boolean approved, Long userId);
}