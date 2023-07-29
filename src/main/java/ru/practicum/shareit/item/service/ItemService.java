package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long userId);
    ItemDto getItem(Long itemId, Long userId);
    List<ItemDto> getItemsByUserId( Long userId);
    ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto);
    List<ItemDto> searchItemsForRent(String text);
    CommentDto createComment(CommentDto commentDto, Long userId, Long itemId);


}