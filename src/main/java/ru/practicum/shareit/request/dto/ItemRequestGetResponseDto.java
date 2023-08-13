package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestGetResponseDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<RequestedItem> items;

    @Data
    @Builder
    public static class RequestedItem {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Integer requestId;
    }
}