package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemDto getItem(Long itemId) {
        return itemStorage.getItem(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotFoundException("Некоректный идентификатор обьекта"));
    }

    public ItemDto addItem(ItemDto itemDto, Long userId) {
        User user = validateAndGetUser(userId);
        validateItemDto(itemDto);

        Item item = ItemMapper.toItem(itemDto, user, null);
        Item addedItem = itemStorage.add(item);

        return ItemMapper.toItemDto(addedItem);
    }

    public ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto) {
        User user = validateAndGetUser(userId);
        if (itemDto.getId() != null && !Objects.equals(itemDto.getId(), itemId)) {
            throw new BadRequestException("Попытка обновления вещи по id, где id не совпадает с id в itemDto");
        }
        Item item = ItemMapper.toItem(itemDto, user, null);
        return ItemMapper.toItemDto(itemStorage.updateItem(itemId, userId, item));
    }

    public List<ItemDto> getItemsByUserId(Long userId) {
        return itemStorage.getAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public Collection<ItemDto> searchItemsForRent(String text) {
        return itemStorage.searchByText(text).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void validateItemDto(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null ||
                itemDto.getName().isBlank() || itemDto.getDescription().isBlank()) {
            throw new BadRequestException("Попытка добавить элемент с отсутствующими полями");
        }
    }

    private User validateAndGetUser(Long userId) {
        return userStorage.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}