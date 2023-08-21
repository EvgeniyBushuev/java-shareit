package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingRequestDto {

    private LocalDateTime start;

    private LocalDateTime end;

    private boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }

    private Long itemId;
}