package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
@RequiredArgsConstructor
public class ItemExtractor {
    private final JdbcTemplate jdbcTemplate;

    public List<ItemDto> getItemsWithBookings(Long userId) {
        return null;
    }
}
