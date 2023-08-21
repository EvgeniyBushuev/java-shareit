package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestGetResponseDto> getAllByRequesterId(Long userId, int from, int size);

    List<ItemRequestGetResponseDto> getAll(Long userId, int from, int size);

    ItemRequestGetResponseDto getById(Long userId, Long itemRequestId);

    ItemRequestCreateResponseDto create(ItemRequestCreateDto itemRequestCreateDto, Long userId);
}
