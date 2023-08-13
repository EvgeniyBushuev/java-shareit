package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequestCreateResponseDto toCreateResponseDto(ItemRequest itemRequest) {
        return ItemRequestCreateResponseDto.builder()
                .id(Long.valueOf(itemRequest.getId()))
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestGetResponseDto toGetResponseDto(ItemRequest itemRequest) {
        return ItemRequestGetResponseDto.builder()
                .id(Long.valueOf(itemRequest.getId()))
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequest fromDto(ItemRequestCreateDto itemRequestCreateDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestCreateDto.getDescription());
        return itemRequest;
    }
}
