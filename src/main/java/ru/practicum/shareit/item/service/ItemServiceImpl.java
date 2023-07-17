package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemDto getItem(Long itemId) {
        return itemStorage.getItem(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotFoundException("Некоректный идентификатор обьекта"));
    }

    public ItemDto addItem(@Valid ItemDto itemDto, Long userId) {
        User user = userStorage.getById(userId).orElseThrow(() -> new NotFoundException(
                "Пользователь не найден"));

        Item item = ItemMapper.toItem(itemDto, user, null);
        Item addedItem = itemStorage.add(item);
        return ItemMapper.toItemDto(addedItem);
    }

    public ItemDto updateItem(Long itemId, Long userId, @Valid ItemDto itemDto) {

        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item existItem = itemStorage.getItem(itemId).get();

        checkOwner(userId, existItem);

        Item item = ItemMapper.toItem(itemDto, user, null);

        return ItemMapper.toItemDto(itemStorage.updateItem(itemId, userId, item));
    }

    public List<ItemDto> getItemsByUserId(Long userId) {
        return itemStorage.getByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchItemsForRent(String text) {
        return itemStorage.searchByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkOwner(Long userId, Item item) {
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Попытка обновить вещь другого пользователя");
        }
    }
}