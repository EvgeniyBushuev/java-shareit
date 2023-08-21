package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.RequestBookingState;
import ru.practicum.shareit.client.BookingClient;
import ru.practicum.shareit.exception.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.util.RequestHeader.SHARER_USER_ID;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@PathVariable Long bookingId,
                                          @RequestHeader(SHARER_USER_ID) Long userId) {

        log.info("Запрос бронирования с ID: {}", bookingId);
        return bookingClient.getById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByState(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(defaultValue = "20") @Min(1) int size,
                                                  @RequestHeader(SHARER_USER_ID) Long userId) {

        RequestBookingState requestBookingState = getStateParam(state);

        log.info("Запрос списка бронирований от пользователя ID: {}", userId);
        return bookingClient.getAllByState(userId, requestBookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByStateForOwner(@RequestParam(defaultValue = "ALL") String state,
                                                          @RequestParam(defaultValue = "0") @Min(0) int from,
                                                          @RequestParam(defaultValue = "20") @Min(1) int size,
                                                          @RequestHeader(SHARER_USER_ID) Long userId) {

        RequestBookingState requestBookingState = getStateParam(state);

        log.info("Запрос списка бронирований от владельца ID: {}", userId);
        return bookingClient.getAllByStateForOwner(userId, requestBookingState, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(SHARER_USER_ID) Long userId,
                                         @Valid @RequestBody BookingRequestDto bookingRequestDto) {

        log.info("Запрос на создание брони от пользователя ID: {}", userId);
        return bookingClient.addBooking(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@PathVariable Long bookingId,
                                      @RequestParam boolean approved,
                                      @RequestHeader(SHARER_USER_ID) Long userId) {

        log.info("Запрос на подтверждение брони от пользователя ID: {}", userId);
        return bookingClient.approve(bookingId, approved, userId);
    }

    private RequestBookingState getStateParam(String state) {

        RequestBookingState requestBookingState;

        try {
            return  requestBookingState = RequestBookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}
