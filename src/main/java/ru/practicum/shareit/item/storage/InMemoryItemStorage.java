package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long id = 0L;

    @Override
    public Optional<Item> getItem(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Item add(Item item) {
        ++id;
        item.setId(id);
        items.put(id, item);
        return item;
    }

    @Override
    public Item updateItem(Long itemId, Long userId, Item itemUpdate) {
        Item item = items.get(itemId);

        if (item == null) {
            throw new BadRequestException("Некоректный индентификатор запроса: " + itemId);
        }

        checkOwner(userId, item);
        updateItemInfo(item, itemUpdate);
        return item;
    }

    @Override
    public Collection<Item> getAll() {
        return items.values();
    }

    @Override
    public Collection<Item> searchByText(String text) {

        if (text == null || text.isEmpty()) return List.of();

        String lowerCaseText = text.toLowerCase();

        return items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(lowerCaseText) ||
                                item.getDescription().toLowerCase().contains(lowerCaseText)))
                .collect(Collectors.toList());
    }

    private void updateItemInfo(Item item, Item itemUpdate) {
        if (itemUpdate.getName() != null) {
            item.setName(itemUpdate.getName());
        }
        if (itemUpdate.getDescription() != null) {
            item.setDescription(itemUpdate.getDescription());
        }
        if (itemUpdate.getAvailable() != null) {
            item.setAvailable(itemUpdate.getAvailable());
        }
    }

    private void checkOwner(Long userId, Item item) {
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Попытка обновить элемент другого пользователя");
        }
    }
}