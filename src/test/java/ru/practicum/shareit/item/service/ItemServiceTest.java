package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Test
    void createTest() {
        User user = getUser(1L);

        ItemRequest itemRequest = getItemRequest(10L);

        Item item = getItem(100L);
        item.setOwner(user);
        item.setItemRequest(itemRequest);

        ItemDto createDto = ItemDto.builder()
                .requestId(itemRequest.getId())
                .build();

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.ofNullable(user));
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.ofNullable(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto resultDto = itemService.addItem(createDto, user.getId());

        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(resultDto.getRequestId(), equalTo(itemRequest.getId()));

        verify(userRepository, times(1)).findById(eq(user.getId()));
        verify(itemRequestRepository, times(1)).findById(eq(itemRequest.getId()));
        verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    void createCommentTest() {
        User user = getUser(1L);
        Item item = getItem(10L);

        Comment comment = getComment(100L);
        comment.setItem(item);
        comment.setAuthor(user);

        CommentDto createDto = CommentDto.builder().build();

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));
        when(bookingRepository.findCountAllApprovedByItemIdAndUserId(eq(item.getId()), eq(user.getId()), any(LocalDateTime.class))).thenReturn(1);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto resultDto = itemService.createComment(createDto, user.getId(), item.getId());

        assertThat(resultDto.getId(), equalTo(comment.getId()));
        assertThat(resultDto.getText(), equalTo(comment.getText()));
        assertThat(resultDto.getAuthorName(), equalTo(user.getName()));

        verify(userRepository, times(1)).findById(eq(user.getId()));
        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verify(bookingRepository, times(1)).findCountAllApprovedByItemIdAndUserId(eq(item.getId()), eq(user.getId()), any(LocalDateTime.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void createCommentTest_NoBookings() {
        User user = getUser(1L);
        Item item = getItem(10L);

        Comment comment = getComment(100L);
        comment.setItem(item);
        comment.setAuthor(user);

        CommentDto createDto = CommentDto.builder().build();

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));
        when(bookingRepository.findCountAllApprovedByItemIdAndUserId(eq(item.getId()), eq(user.getId()), any(LocalDateTime.class))).thenReturn(0);

        BadRequestException e = assertThrows(BadRequestException.class, () -> {
            itemService.createComment(createDto, user.getId(), item.getId());
        });

        verify(userRepository, times(1)).findById(eq(user.getId()));
        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verify(bookingRepository, times(1)).findCountAllApprovedByItemIdAndUserId(eq(item.getId()), eq(user.getId()), any(LocalDateTime.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getByIdAsOwnerTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L);
        item.setOwner(owner);

        Booking lastBooking = getBooking(100L, booker, item);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking nextBooking = getBooking(101L, booker, item);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        List<Booking> bookingList = Arrays.asList(
                lastBooking,
                nextBooking
        );

        Comment comment1 = getComment(1000L);
        comment1.setAuthor(booker);

        Comment comment2 = getComment(1001L);
        comment2.setAuthor(booker);

        List<Comment> commentList = Arrays.asList(
                comment1,
                comment2
        );

        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));
        when(bookingRepository.findAllByItemId(eq(item.getId()))).thenReturn(bookingList);
        when(commentRepository.findAllByItemId(eq(item.getId()))).thenReturn(commentList);

        ItemDto resultDto = itemService.getItem(item.getId(), owner.getId());

        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));

        assertThat(resultDto.getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(resultDto.getNextBooking().getBookerId(), equalTo(booker.getId()));
        assertThat(resultDto.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(resultDto.getLastBooking().getBookerId(), equalTo(booker.getId()));

        assertThat(resultDto.getComments().size(), equalTo(2));
        assertThat(resultDto.getComments().get(0).getId(), equalTo(comment1.getId()));
        assertThat(resultDto.getComments().get(0).getText(), equalTo(comment1.getText()));
        assertThat(resultDto.getComments().get(0).getAuthorName(), equalTo(booker.getName()));
        assertThat(resultDto.getComments().get(1).getId(), equalTo(comment2.getId()));
        assertThat(resultDto.getComments().get(1).getText(), equalTo(comment2.getText()));
        assertThat(resultDto.getComments().get(1).getAuthorName(), equalTo(booker.getName()));

        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verify(bookingRepository, times(1)).findAllByItemId(eq(item.getId()));
        verify(commentRepository, times(1)).findAllByItemId(eq(item.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getByIdAsNotOwnerTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);
        User notOwner = getUser(3L);

        Item item = getItem(10L);
        item.setOwner(owner);

        Booking lastBooking = getBooking(100L, booker, item);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking nextBooking = getBooking(101L, booker, item);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        List<Booking> bookingList = Arrays.asList(
                lastBooking,
                nextBooking
        );

        Comment comment1 = getComment(1000L);
        comment1.setAuthor(booker);

        Comment comment2 = getComment(1001L);
        comment2.setAuthor(booker);

        List<Comment> commentList = Arrays.asList(
                comment1,
                comment2
        );

        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));
        when(commentRepository.findAllByItemId(eq(item.getId()))).thenReturn(commentList);

        ItemDto resultDto = itemService.getItem(item.getId(), notOwner.getId());

        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));

        assertThat(resultDto.getNextBooking(), equalTo(null));
        assertThat(resultDto.getLastBooking(), equalTo(null));

        assertThat(resultDto.getComments().size(), equalTo(2));
        assertThat(resultDto.getComments().get(0).getId(), equalTo(comment1.getId()));
        assertThat(resultDto.getComments().get(0).getText(), equalTo(comment1.getText()));
        assertThat(resultDto.getComments().get(0).getAuthorName(), equalTo(booker.getName()));
        assertThat(resultDto.getComments().get(1).getId(), equalTo(comment2.getId()));
        assertThat(resultDto.getComments().get(1).getText(), equalTo(comment2.getText()));
        assertThat(resultDto.getComments().get(1).getAuthorName(), equalTo(booker.getName()));

        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verify(commentRepository, times(1)).findAllByItemId(eq(item.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getAllByOwnerIdTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item1 = getItem(10L);
        item1.setOwner(owner);

        Item item2 = getItem(11L);
        item2.setOwner(owner);

        List<Item> itemList = Arrays.asList(
                item1,
                item2
        );

        Booking item1lastBooking = getBooking(100L, booker, item1);
        item1lastBooking.setStart(LocalDateTime.now().minusDays(2));
        item1lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking item1nextBooking = getBooking(101L, booker, item1);
        item1nextBooking.setStart(LocalDateTime.now().plusDays(1));
        item1nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        List<Booking> itemBookingList = Arrays.asList(
                item1lastBooking,
                item1nextBooking
        );

        Comment comment1 = getComment(1000L);
        comment1.setItem(item1);
        comment1.setAuthor(booker);

        Comment comment2 = getComment(1001L);
        comment2.setItem(item1);
        comment2.setAuthor(booker);

        List<Comment> itemCommentList = Arrays.asList(
                comment1,
                comment2
        );

        when(itemRepository.findAllByOwnerId(eq(owner.getId()), any(Pageable.class))).thenReturn(itemList);
        when(bookingRepository.findAllByItemIdIn(eq(List.of(item1.getId(), item2.getId())))).thenReturn(itemBookingList);
        when(commentRepository.findAllByItemIdIn(eq(List.of(item1.getId(), item2.getId())))).thenReturn(itemCommentList);

        Pageable pageable = PageRequest.of(0, 10);

        List<ItemDto> resultDtoList = itemService.getItemsByUserId(owner.getId(), pageable);

        assertThat(resultDtoList.size(), equalTo(2));
        assertThat(resultDtoList.get(0).getId(), equalTo(item1.getId()));
        assertThat(resultDtoList.get(0).getName(), equalTo(item1.getName()));
        assertThat(resultDtoList.get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(resultDtoList.get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(resultDtoList.get(0).getNextBooking().getId(), equalTo(item1nextBooking.getId()));
        assertThat(resultDtoList.get(0).getNextBooking().getBookerId(), equalTo(booker.getId()));
        assertThat(resultDtoList.get(0).getLastBooking().getId(), equalTo(item1lastBooking.getId()));
        assertThat(resultDtoList.get(0).getLastBooking().getBookerId(), equalTo(booker.getId()));

        assertThat(resultDtoList.get(1).getId(), equalTo(item2.getId()));
        assertThat(resultDtoList.get(1).getName(), equalTo(item2.getName()));
        assertThat(resultDtoList.get(1).getDescription(), equalTo(item2.getDescription()));
        assertThat(resultDtoList.get(1).getAvailable(), equalTo(item2.getAvailable()));

        assertThat(resultDtoList.get(1).getNextBooking(), equalTo(null));
        assertThat(resultDtoList.get(1).getLastBooking(), equalTo(null));

        assertThat(resultDtoList.get(1).getComments().size(), equalTo(0));

        verify(itemRepository, times(1)).findAllByOwnerId(eq(owner.getId()), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByItemIdIn(eq(List.of(item1.getId(), item2.getId())));
        verify(commentRepository, times(1)).findAllByItemIdIn(eq(List.of(item1.getId(), item2.getId())));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getAllBySearchTextTest() {
        String searchText = "Item";

        Item item1 = getItem(1L);
        Item item2 = getItem(2L);

        List<Item> itemList = Arrays.asList(
                item1,
                item2
        );

        when(itemRepository.findBySearchText(eq(searchText), any(Pageable.class))).thenReturn(itemList);

        Pageable pageable = PageRequest.of(0, 10);

        List<ItemDto> resultDtoList = itemService.searchItemsForRent(searchText, pageable);

        assertThat(resultDtoList.size(), equalTo(2));

        assertThat(resultDtoList.get(0).getId(), equalTo(item1.getId()));
        assertThat(resultDtoList.get(0).getName(), equalTo(item1.getName()));
        assertThat(resultDtoList.get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(resultDtoList.get(0).getAvailable(), equalTo(item1.getAvailable()));

        assertThat(resultDtoList.get(1).getId(), equalTo(item2.getId()));
        assertThat(resultDtoList.get(1).getName(), equalTo(item2.getName()));
        assertThat(resultDtoList.get(1).getDescription(), equalTo(item2.getDescription()));
        assertThat(resultDtoList.get(1).getAvailable(), equalTo(item2.getAvailable()));

        verify(itemRepository, times(1)).findBySearchText(eq(searchText), any(Pageable.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void getAllBySearchTextTest_BlankQuery() {

        Pageable pageable = PageRequest.of(0, 10);

        List<ItemDto> resultDtoList = itemService.searchItemsForRent(" ", pageable);

        assertThat(resultDtoList.size(), equalTo(0));

        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void updateTest() {
        ItemDto inputDto = ItemDto.builder().build();

        User owner = getUser(1L);

        Item item = getItem(10L);
        item.setOwner(owner);

        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto resultDto = itemService.updateItem(item.getId(), owner.getId(), inputDto);

        assertThat(resultDto.getId(), equalTo(item.getId()));
        assertThat(resultDto.getName(), equalTo(item.getName()));
        assertThat(resultDto.getDescription(), equalTo(item.getDescription()));
        assertThat(resultDto.getAvailable(), equalTo(item.getAvailable()));

        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void updateTest_NotOwner() {
        ItemDto inputDto = ItemDto.builder().build();

        User owner = getUser(1L);
        User notOwner = getUser(2L);

        Item item = getItem(10L);
        item.setOwner(owner);

        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));

        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            itemService.updateItem(item.getId(), notOwner.getId(), inputDto);
        });

        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void updateTest_InvalidDto() {
        ItemDto inputDto = ItemDto.builder()
                .name("")
                .build();

        User owner = getUser(1L);

        Item item = getItem(10L);
        item.setOwner(owner);

        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));

        BadRequestException e = assertThrows(BadRequestException.class, () -> {
            itemService.updateItem(item.getId(), owner.getId(), inputDto);
        });

        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void updateTest_Conflict() {
        ItemDto inputDto = ItemDto.builder().build();

        User owner = getUser(1L);

        Item item = getItem(10L);
        item.setOwner(owner);

        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));
        when(itemRepository.save(any(Item.class))).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));

        InvalidDataException e = assertThrows(InvalidDataException.class, () -> {
            itemService.updateItem(item.getId(), owner.getId(), inputDto);
        });

        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    private User getUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@user.com")
                .build();
    }

    private Item getItem(Long id) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .description("ItemDescr " + id)
                .available(true)
                .build();
    }

    private Comment getComment(Long id) {
        return Comment.builder()
                .id(id)
                .text("Comment text " + id)
                .build();
    }

    private ItemRequest getItemRequest(Long id) {
        return ItemRequest.builder()
                .id(id)
                .description("Request " + id)
                .build();
    }

    private Booking getBooking(Long id, User booker, Item item) {
        return Booking.builder()
                .id(id)
                .status(BookingStatus.APPROVED)
                .user(booker)
                .item(item)
                .build();
    }
}