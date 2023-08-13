package ru.practicum.shareit.item.mapper;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemResultSetExtractor implements ResultSetExtractor<List<Item>> {
    @Override
    public List<Item> extractData(ResultSet rs) throws SQLException, DataAccessException {

        List<Item> items = new ArrayList<>();

        while (rs.next()) {
            ItemDto itemDto = new ItemDto();

            itemDto.setId(rs.getLong(1));
            itemDto.setName(rs.getString(2));
            itemDto.setDescription(rs.getString(3));
            itemDto.setAvailable(rs.getBoolean(4));
            itemDto.setNextBooking(ItemDto.ItemBooking.builder()
                    .id(rs.getLong(5))
                    .bookerId(rs.getLong(7)).build());
            itemDto.setLastBooking(ItemDto.ItemBooking.builder()
                    .id(rs.getLong(6))
                    .bookerId(rs.getLong(7)).build());

        }
        return items;
    }
}
