package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@PathVariable Long bookingId,
                                      @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Запрос бронирования с ID: {}", bookingId);
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByState(@RequestParam(required = false, defaultValue = "ALL") String state,
                                                  @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(required = false, defaultValue = "20") @Min(1) int size,
                                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        RequestBookingState requestBookingState;

        Pageable pageable = PageRequest.of(from / size, size);;

        try {
            requestBookingState = RequestBookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }

        log.info("Запрос списка бронирований от пользователя ID: {}", userId);
        return bookingService.getAllByState(requestBookingState, userId, pageable);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByStateForOwner(@RequestParam(defaultValue = "ALL") String state,
                                                          @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                                          @RequestParam(required = false, defaultValue = "20") @Min(1) int size,
                                                          @RequestHeader("X-Sharer-User-Id") Long userId) {

        RequestBookingState requestBookingState;

        Pageable pageable = PageRequest.of(from / size, size);

        try {
            requestBookingState = RequestBookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }

        log.info("Запрос списка бронирований от владельца ID: {}", userId);
        return bookingService.getAllByStateForOwner(requestBookingState, userId, pageable);
    }

    @PostMapping
    public BookingResponseDto addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody BookingRequestDto bookingRequestDto) {

        log.info("Запрос на создание брони от пользователя ID: {}", userId);
        return bookingService.addBooking(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@PathVariable Long bookingId,
                                      @RequestParam boolean approved,
                                      @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Запрос на подтверждение брони от пользователя ID: {}", userId);
        return bookingService.approve(bookingId, approved, userId);
    }
}
