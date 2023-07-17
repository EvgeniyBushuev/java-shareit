package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {

    ItemDto getItem(Long itemId);

    ItemDto addItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto);

    List<ItemDto> getItemsByUserId(Long userId);

    List<ItemDto> searchItemsForRent(String text);

}