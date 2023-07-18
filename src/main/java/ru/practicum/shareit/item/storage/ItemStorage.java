package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {
    Optional<Item> getItem(Long itemId);

    Item add(Item item);

    Item updateItem(Long itemId, Long userId, Item itemUpdate);

    Collection<Item> getByOwnerId(Long ownerId);

    Collection<Item> searchByText(String text);
}
