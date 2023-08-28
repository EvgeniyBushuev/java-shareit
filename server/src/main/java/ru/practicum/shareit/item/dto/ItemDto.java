package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.comment.CommentDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private ItemBooking nextBooking;
    private ItemBooking lastBooking;
    private List<CommentDto> comments;
    private Long requestId;


    @Data
    @Builder
    public static class ItemBooking {
        private Long id;
        private Long bookerId;
    }
}
