package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
import java.util.Map;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestGetResponseDto> getAllByRequesterId(Long userId, Pageable pageable) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + userId));

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdOrderByCreatedDesc(userId, pageable);

        List<Item> items = itemRepository.findAllByItemRequestIdIn(requests
                .stream()
                .map(ItemRequest::getId)
                .collect(toList()));


        List<ItemRequestGetResponseDto> responseItemRequests = new ArrayList<>();

        for (ItemRequest request : requests) {
            ItemRequestGetResponseDto itemResponse = ItemRequestMapper.toGetResponseDto(request);

            addItemsInformation(itemResponse, items);

            responseItemRequests.add(itemResponse);
        }

        return responseItemRequests;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestGetResponseDto> getAll(Long userId, Pageable pageable) {

        List<Long> requestsIds = itemRequestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(userId, pageable)
                .stream()
                .map(ItemRequest::getId)
                .collect(toList());

        Map<ItemRequest, List<Item>> itemByGroup = itemRepository.findAllByItemRequestIdIn(requestsIds)
                .stream()
                .collect(groupingBy(Item::getItemRequest, toList()));

        List<ItemRequestGetResponseDto> responseDtoList = new ArrayList<>();

        for (Map.Entry<ItemRequest, List<Item>> list : itemByGroup.entrySet()) {

            ItemRequestGetResponseDto itemRequestGetResponseDto = ItemRequestMapper.toGetResponseDto(list.getKey());
            itemRequestGetResponseDto.setItems(list.getValue().isEmpty() ? new ArrayList<>() :
                    list.getValue().stream()
                            .map(item -> ItemRequestGetResponseDto.RequestedItem.builder()
                                    .id(item.getId())
                                    .name(item.getName())
                                    .description(item.getDescription())
                                    .available(item.getAvailable())
                                    .requestId(item.getItemRequest().getId())
                                    .build()
                            )
                            .collect(toList())
            );

            responseDtoList.add(itemRequestGetResponseDto);
        }

        return responseDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestGetResponseDto getById(Long userId, Long itemRequestId) {

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
                                .requestId(item.getItemRequest().getId())
                                .build()
                        )
                        .collect(toList())
        );

        return itemRequestGetResponseDto;
    }

    private ItemRequestGetResponseDto addItemsInformation(ItemRequestGetResponseDto itemRequestGetResponseDto, List<Item> items) {

        itemRequestGetResponseDto.setItems(items.isEmpty() ? new ArrayList<>() :
                items.stream()
                        .map(item -> ItemRequestGetResponseDto.RequestedItem.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .requestId(item.getItemRequest().getId())
                                .build()
                        )
                        .collect(toList())
        );

        return itemRequestGetResponseDto;
    }
}
