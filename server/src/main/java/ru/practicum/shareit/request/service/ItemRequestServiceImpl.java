package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    public List<ItemRequestGetResponseDto> getAllByRequesterId(Long userId, int from, int size) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + userId));

        Pageable pageable = PageRequest.of(from, size);

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdOrderByCreatedDesc(userId, pageable);

        return getResponseItemRequestList(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestGetResponseDto> getAll(Long userId, int from, int size) {

        Pageable pageable = PageRequest.of(from, size);

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(userId, pageable);

        return getResponseItemRequestList(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestGetResponseDto getById(Long userId, Long itemRequestId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + userId));

        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException("Некорректный идентификатор: " + itemRequestId));

        return getResponseItemRequestList(List.of(itemRequest)).get(0);
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

    private List<ItemRequestGetResponseDto> getResponseItemRequestList(List<ItemRequest> requests) {

        List<ItemRequestGetResponseDto> responseItemRequests = new ArrayList<>();

        List<Long> requestsIds = requests
                .stream()
                .map(ItemRequest::getId)
                .collect(toList());

        Map<ItemRequest, List<Item>> itemByGroup = itemRepository.findAllByItemRequestIdIn(requestsIds)
                .stream()
                .collect(groupingBy(Item::getItemRequest, toList()));

        for (ItemRequest request : requests) {

            ItemRequestGetResponseDto itemResponse = ItemRequestMapper.toGetResponseDto(request);

            itemResponse.setItems(itemByGroup.get(request) == null ? new ArrayList<>() :
                    itemByGroup.get(request).stream()
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

            responseItemRequests.add(itemResponse);
        }
        return responseItemRequests;
    }
}
