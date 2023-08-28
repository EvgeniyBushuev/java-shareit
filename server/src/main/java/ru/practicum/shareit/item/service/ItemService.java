package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    void delete(Long itemId);

    ItemDto addItem(ItemDto itemDto, Long userId);

    ItemDto getItem(Long itemId, Long userId);

    List<ItemDto> getItemsByUserId(Long userId, int from, int size);

    ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto);

    List<ItemDto> searchItemsForRent(String text, int from, int size);

    CommentDto createComment(CommentDto commentDto, Long userId, Long itemId);
}