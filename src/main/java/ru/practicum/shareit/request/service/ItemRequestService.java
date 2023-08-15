package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestGetResponseDto> getAllByRequesterId(Long userId, Pageable pageable);

    List<ItemRequestGetResponseDto> getAll(Long userId, Pageable pageable);

    ItemRequestGetResponseDto getById(Long userId, Long itemRequestId);

    ItemRequestCreateResponseDto create(ItemRequestCreateDto itemRequestCreateDto, Long userId);
}
