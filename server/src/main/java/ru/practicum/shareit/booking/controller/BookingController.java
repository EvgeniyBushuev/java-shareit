package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;

import java.util.List;

import static ru.practicum.shareit.util.RequestHeader.SHARER_USER_ID;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@PathVariable Long bookingId,
                                      @RequestHeader(SHARER_USER_ID) Long userId) {

        log.info("Запрос бронирования с ID: {}", bookingId);
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByState(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestHeader(SHARER_USER_ID) Long userId) {
        RequestBookingState requestBookingState;

        try {
            requestBookingState = RequestBookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }

        log.info("Запрос списка бронирований от пользователя ID: {}", userId);
        return bookingService.getAllByState(requestBookingState, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByStateForOwner(@RequestParam(defaultValue = "ALL") String state,
                                                          @RequestParam(defaultValue = "0") int from,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          @RequestHeader(SHARER_USER_ID) Long userId) {

        RequestBookingState requestBookingState;

        try {
            requestBookingState = RequestBookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }

        log.info("Запрос списка бронирований от владельца ID: {}", userId);
        return bookingService.getAllByStateForOwner(requestBookingState, userId, from, size);
    }

    @PostMapping
    public BookingResponseDto addBooking(@RequestHeader(SHARER_USER_ID) Long userId,
                                         @RequestBody BookingRequestDto bookingRequestDto) {

        log.info("Запрос на создание брони от пользователя ID: {}", userId);
        return bookingService.addBooking(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@PathVariable Long bookingId,
                                      @RequestParam boolean approved,
                                      @RequestHeader(SHARER_USER_ID) Long userId) {

        log.info("Запрос на подтверждение брони от пользователя ID: {}", userId);
        return bookingService.approve(bookingId, approved, userId);
    }
}