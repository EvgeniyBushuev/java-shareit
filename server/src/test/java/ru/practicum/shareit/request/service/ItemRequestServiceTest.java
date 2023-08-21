package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Test
    void getAllByRequesterIdTest() {
        User owner = getUser(1L);
        User requester = getUser(2L);

        ItemRequest itemRequest1 = getItemRequest(10L);
        itemRequest1.setRequester(requester);

        ItemRequest itemRequest2 = getItemRequest(11L);
        itemRequest2.setRequester(requester);

        Item item1 = getItem(100L);
        item1.setOwner(owner);
        item1.setItemRequest(itemRequest1);

        Item item2 = getItem(101L);
        item2.setOwner(owner);
        item2.setItemRequest(itemRequest2);

        List<ItemRequest> itemRequestList = Arrays.asList(
                itemRequest1,
                itemRequest2
        );

        when(userRepository.findById(requester.getId())).thenReturn(Optional.ofNullable(requester));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(eq(requester.getId()), any(Pageable.class))).thenReturn(itemRequestList);
        when(itemRepository.findAllByItemRequestIdIn(eq(List.of(itemRequest1.getId(),itemRequest2.getId())))).thenReturn(Arrays.asList(item1, item2));

        List<ItemRequestGetResponseDto> resultDtoList = itemRequestService.getAllByRequesterId(requester.getId(), 0, 10);

        assertThat(resultDtoList.size(), equalTo(2));

        assertThat(resultDtoList.get(0).getId(), equalTo(itemRequest1.getId()));
        assertThat(resultDtoList.get(0).getDescription(), equalTo(itemRequest1.getDescription()));
        assertThat(resultDtoList.get(0).getItems().size(), equalTo(1));
        assertThat(resultDtoList.get(0).getItems().get(0).getId(), equalTo(item1.getId()));
        assertThat(resultDtoList.get(0).getItems().get(0).getName(), equalTo(item1.getName()));
        assertThat(resultDtoList.get(0).getItems().get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(resultDtoList.get(0).getItems().get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(resultDtoList.get(0).getItems().get(0).getRequestId(), equalTo(10L));

        assertThat(resultDtoList.get(1).getId(), equalTo(itemRequest2.getId()));
        assertThat(resultDtoList.get(1).getDescription(), equalTo(itemRequest2.getDescription()));
        assertThat(resultDtoList.get(1).getItems().size(), equalTo(1));
        assertThat(resultDtoList.get(1).getItems().get(0).getId(), equalTo(item2.getId()));
        assertThat(resultDtoList.get(1).getItems().get(0).getName(), equalTo(item2.getName()));
        assertThat(resultDtoList.get(1).getItems().get(0).getDescription(), equalTo(item2.getDescription()));
        assertThat(resultDtoList.get(1).getItems().get(0).getAvailable(), equalTo(item2.getAvailable()));
        assertThat(resultDtoList.get(1).getItems().get(0).getRequestId(), equalTo(11L));

        verify(userRepository, times(1)).findById(eq(requester.getId()));
        verify(itemRequestRepository, times(1)).findAllByRequesterIdOrderByCreatedDesc(eq(requester.getId()), any(Pageable.class));
        verify(itemRepository, times(1)).findAllByItemRequestIdIn(List.of(itemRequest1.getId(),itemRequest2.getId()));
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void getAllTest() {
        User owner = getUser(1L);
        User requester = getUser(2L);

        ItemRequest itemRequest1 = getItemRequest(10L);
        itemRequest1.setRequester(requester);

        ItemRequest itemRequest2 = getItemRequest(11L);
        itemRequest2.setRequester(requester);

        Item item1 = getItem(100);
        item1.setOwner(owner);
        item1.setItemRequest(itemRequest1);

        Item item2 = getItem(101);
        item2.setOwner(owner);
        item2.setItemRequest(itemRequest2);

        List<ItemRequest> itemRequestList = Arrays.asList(
                itemRequest1,
                itemRequest2
        );

        Map<ItemRequest, List<Item>> itemByGroup = new HashMap<>();
        itemByGroup.put(itemRequest1, List.of(item1));
        itemByGroup.put(itemRequest2, List.of(item2));

        when(itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(eq(owner.getId()), any(Pageable.class))).thenReturn(itemRequestList);
        when(itemRepository.findAllByItemRequestIdIn(eq(List.of(itemRequest1.getId(),itemRequest2.getId())))).thenReturn(Arrays.asList(item1, item2));

        List<ItemRequestGetResponseDto> resultDtoList = itemRequestService.getAll(owner.getId(), 0, 10);

        assertThat(resultDtoList.size(), equalTo(2));

        assertThat(resultDtoList.get(0).getId(), equalTo(itemRequest1.getId()));
        assertThat(resultDtoList.get(0).getDescription(), equalTo(itemRequest1.getDescription()));
        assertThat(resultDtoList.get(0).getItems().size(), equalTo(1));
        assertThat(resultDtoList.get(0).getItems().get(0).getId(), equalTo(item1.getId()));
        assertThat(resultDtoList.get(0).getItems().get(0).getName(), equalTo(item1.getName()));
        assertThat(resultDtoList.get(0).getItems().get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(resultDtoList.get(0).getItems().get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(resultDtoList.get(0).getItems().get(0).getRequestId(), equalTo(10L));

        assertThat(resultDtoList.get(1).getId(), equalTo(itemRequest2.getId()));
        assertThat(resultDtoList.get(1).getDescription(), equalTo(itemRequest2.getDescription()));
        assertThat(resultDtoList.get(1).getItems().size(), equalTo(1));
        assertThat(resultDtoList.get(1).getItems().get(0).getId(), equalTo(item2.getId()));
        assertThat(resultDtoList.get(1).getItems().get(0).getName(), equalTo(item2.getName()));
        assertThat(resultDtoList.get(1).getItems().get(0).getDescription(), equalTo(item2.getDescription()));
        assertThat(resultDtoList.get(1).getItems().get(0).getAvailable(), equalTo(item2.getAvailable()));
        assertThat(resultDtoList.get(1).getItems().get(0).getRequestId(), equalTo(11L));

        verify(itemRequestRepository, times(1)).findAllByRequesterIdNotOrderByCreatedDesc(eq(owner.getId()), any(Pageable.class));
        verify(itemRepository, times(1)).findAllByItemRequestIdIn(List.of(itemRequest1.getId(),itemRequest2.getId()));
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void getByIdTest() {
        User owner = getUser(1L);
        User requester = getUser(2L);

        ItemRequest itemRequest = getItemRequest(10L);
        itemRequest.setRequester(requester);

        Item item = getItem(100L);
        item.setOwner(owner);
        item.setItemRequest(itemRequest);

        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(eq(itemRequest.getId()))).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByItemRequestIdIn(eq(List.of(itemRequest.getId())))).thenReturn(List.of(item));

        ItemRequestGetResponseDto resultDto = itemRequestService.getById(requester.getId(), itemRequest.getId());

        assertThat(resultDto.getId(), equalTo(itemRequest.getId()));
        assertThat(resultDto.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(resultDto.getItems().size(), equalTo(1));
        assertThat(resultDto.getItems().get(0).getId(), equalTo(item.getId()));
        assertThat(resultDto.getItems().get(0).getName(), equalTo(item.getName()));
        assertThat(resultDto.getItems().get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getItems().get(0).getAvailable(), equalTo(item.getAvailable()));
        assertThat(resultDto.getItems().get(0).getRequestId(), equalTo(10L));

        verify(userRepository, times(1)).findById(eq(requester.getId()));
        verify(itemRequestRepository, times(1)).findById(eq(itemRequest.getId()));
        verify(itemRepository, times(1)).findAllByItemRequestIdIn(eq(List.of(itemRequest.getId())));
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void createTest() {
        User user = getUser(1);
        ItemRequest itemRequest = getItemRequest(10L);

        ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder().build();

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.ofNullable(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestCreateResponseDto resultDto = itemRequestService.create(itemRequestCreateDto, user.getId());

        assertThat(resultDto.getId(), equalTo(itemRequest.getId()));
        assertThat(resultDto.getDescription(), equalTo(itemRequest.getDescription()));

        verify(userRepository, times(1)).findById(eq(user.getId()));
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    private ItemRequest getItemRequest(Long id) {
        return ItemRequest.builder()
                .id(id)
                .description("Request " + id)
                .build();
    }

    private User getUser(long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@user.com")
                .build();
    }

    private Item getItem(long id) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .description("ItemDescr " + id)
                .available(true)
                .build();
    }
}