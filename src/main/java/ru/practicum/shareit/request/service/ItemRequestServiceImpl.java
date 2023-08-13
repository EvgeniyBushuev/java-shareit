package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestGetResponseDto> getAllByRequesterId(Long userId, int from, int size) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + userId));

        return itemRequestRepository
                .findAllByRequesterIdOrderByCreatedDesc(userId, PageRequest.of(from / size, size)).stream()
                .map(ItemRequestMapper::toGetResponseDto)
                .map(this::addItemsInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestGetResponseDto> getAll(Long userId, int from, int size) {
        return itemRequestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(userId, PageRequest.of(from / size, size)).stream()
                .map(ItemRequestMapper::toGetResponseDto)
                .map(this::addItemsInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestGetResponseDto getById(Long userId, Long itemRequestId) {
       // ServiceUtil.getUserOrThrowNotFound(userId, userRepository);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + userId));

        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + itemRequestId));
        ItemRequestGetResponseDto responseDto = ItemRequestMapper.toGetResponseDto(itemRequest);
        responseDto = addItemsInfo(responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ItemRequestCreateResponseDto create(ItemRequestCreateDto itemRequestCreateDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + userId));

        ItemRequest itemRequest = ItemRequestMapper.fromDto(itemRequestCreateDto);
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());

        return ItemRequestMapper.toCreateResponseDto(itemRequestRepository.save(itemRequest));
    }

    private ItemRequestGetResponseDto addItemsInfo(ItemRequestGetResponseDto itemRequestGetResponseDto) {
        List<Item> items = itemRepository.findAllByItemRequestId(itemRequestGetResponseDto.getId());

        itemRequestGetResponseDto.setItems(items.isEmpty() ? new ArrayList<>() :
                items.stream()
                        .map(item -> ItemRequestGetResponseDto.RequestedItem.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .requestId(Math.toIntExact(item.getItemRequest().getId()))
                                .build()
                        )
                        .collect(Collectors.toList())
        );

        return itemRequestGetResponseDto;
    }
}