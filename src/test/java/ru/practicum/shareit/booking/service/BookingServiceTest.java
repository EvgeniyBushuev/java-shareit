package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.RequestBookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
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
public class BookingServiceTest {
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Test
    void getByIdTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);

        when(bookingRepository.findById(eq(booking.getId()))).thenReturn(Optional.ofNullable(booking));
        when(userRepository.findById(eq(owner.getId()))).thenReturn(Optional.ofNullable(owner));

        BookingResponseDto responseDto = bookingService.getById(booking.getId(), owner.getId());

        assertThat(responseDto.getId(), equalTo(booking.getId()));
        assertThat(responseDto.getStatus(), equalTo(booking.getStatus()));
        assertThat(responseDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(responseDto.getItem().getId(), equalTo(item.getId()));
        assertThat(responseDto.getItem().getName(), equalTo(item.getName()));

        verify(bookingRepository, times(1)).findById(eq(booking.getId()));
        verify(userRepository, times(1)).findById(eq(owner.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getByIdTest_UserIsNotOwner() {
        User owner = getUser(1L);
        User booker = getUser(2L);
        User notOwner = getUser(3L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);

        when(bookingRepository.findById(eq(booking.getId()))).thenReturn(Optional.ofNullable(booking));
        when(userRepository.findById(eq(notOwner.getId()))).thenReturn(Optional.ofNullable(notOwner));

        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            bookingService.getById(booking.getId(), notOwner.getId());
        });

        verify(bookingRepository, times(1)).findById(eq(booking.getId()));
        verify(userRepository, times(1)).findById(eq(notOwner.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getAllByStateTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item1 = getItem(10L, owner);
        Item item2 = getItem(11L, owner);

        Booking booking1 = getBooking(100L, booker, item1);
        booking1.setStart(LocalDateTime.now().minusDays(10));
        booking1.setEnd(LocalDateTime.now().minusDays(9));
        Booking booking2 = getBooking(101L, booker, item2);
        booking2.setStart(LocalDateTime.now().minusDays(8));
        booking2.setEnd(LocalDateTime.now().minusDays(7));

        List<Booking> bookingList = Arrays.asList(
                booking1,
                booking2
        );

        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.ofNullable(booker));
        when(bookingRepository.findAllByUserIdOrderByStartDesc(eq(booker.getId()), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByUserIdAndEndBeforeOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByUserIdAndStartAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(BookingStatus.WAITING), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByUserIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(BookingStatus.REJECTED), any(Pageable.class))).thenReturn(bookingList);

        List<BookingResponseDto> responseDtoList;

        Pageable pageable = PageRequest.of(0, 10);

        responseDtoList = bookingService.getAllByState(RequestBookingState.ALL, booker.getId(), pageable);
        bookingService.getAllByState(RequestBookingState.PAST, booker.getId(), pageable);
        bookingService.getAllByState(RequestBookingState.FUTURE, booker.getId(), pageable);
        bookingService.getAllByState(RequestBookingState.CURRENT, booker.getId(), pageable);
        bookingService.getAllByState(RequestBookingState.WAITING, booker.getId(), pageable);
        bookingService.getAllByState(RequestBookingState.REJECTED, booker.getId(), pageable);

        assertThat(responseDtoList.get(0).getId(), equalTo(booking1.getId()));
        assertThat(responseDtoList.get(1).getId(), equalTo(booking2.getId()));

        verify(userRepository, times(6)).findById(eq(booker.getId()));
        verify(bookingRepository, times(1)).findAllByUserIdOrderByStartDesc(eq(booker.getId()), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByUserIdAndEndBeforeOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByUserIdAndStartAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByUserIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(BookingStatus.WAITING), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByUserIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(BookingStatus.REJECTED), any(Pageable.class));

        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void getAllByStateForOwnerTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item1 = getItem(10L, owner);
        Item item2 = getItem(11L, owner);

        Booking booking1 = getBooking(100L, booker, item1);
        booking1.setStart(LocalDateTime.now().minusDays(10));
        booking1.setEnd(LocalDateTime.now().minusDays(9));
        Booking booking2 = getBooking(101L, booker, item2);
        booking2.setStart(LocalDateTime.now().minusDays(8));
        booking2.setEnd(LocalDateTime.now().minusDays(7));

        List<Booking> bookingList = Arrays.asList(
                booking1,
                booking2
        );

        when(userRepository.findById(eq(owner.getId()))).thenReturn(Optional.ofNullable(owner));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(BookingStatus.WAITING), any(Pageable.class))).thenReturn(bookingList);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(BookingStatus.REJECTED), any(Pageable.class))).thenReturn(bookingList);

        List<BookingResponseDto> responseDtoList;

        Pageable pageable = PageRequest.of(0, 10);

        responseDtoList = bookingService.getAllByStateForOwner(RequestBookingState.ALL, owner.getId(), pageable);
        bookingService.getAllByStateForOwner(RequestBookingState.PAST, owner.getId(), pageable);
        bookingService.getAllByStateForOwner(RequestBookingState.FUTURE, owner.getId(), pageable);
        bookingService.getAllByStateForOwner(RequestBookingState.CURRENT, owner.getId(), pageable);
        bookingService.getAllByStateForOwner(RequestBookingState.WAITING, owner.getId(), pageable);
        bookingService.getAllByStateForOwner(RequestBookingState.REJECTED, owner.getId(), pageable);

        assertThat(responseDtoList.get(0).getId(), equalTo(booking1.getId()));
        assertThat(responseDtoList.get(1).getId(), equalTo(booking2.getId()));

        verify(userRepository, times(6)).findById(eq(owner.getId()));
        verify(bookingRepository, times(1)).findAllByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(BookingStatus.WAITING), any(Pageable.class));
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(BookingStatus.REJECTED), any(Pageable.class));

        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .build();

        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.ofNullable(booker));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto responseDto = bookingService.addBooking(requestDto, booker.getId());

        assertThat(responseDto.getId(), equalTo(booking.getId()));
        assertThat(responseDto.getStatus(), equalTo(booking.getStatus()));
        assertThat(responseDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(responseDto.getItem().getId(), equalTo(item.getId()));
        assertThat(responseDto.getItem().getName(), equalTo(item.getName()));

        verify(userRepository, times(1)).findById(eq(booker.getId()));
        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createTest_NotAvailableItem() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);
        item.setAvailable(false);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .build();

        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.ofNullable(booker));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));

        BadRequestException e = assertThrows(BadRequestException.class, () -> {
            bookingService.addBooking(requestDto, booker.getId());
        });

        verify(userRepository, times(1)).findById(eq(booker.getId()));
        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createTest_BookOwnItem() {
        User owner = getUser(1L);

        Item item = getItem(10L, owner);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .build();

        when(userRepository.findById(eq(owner.getId()))).thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.ofNullable(item));

        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            bookingService.addBooking(requestDto, owner.getId());
        });

        verify(userRepository, times(1)).findById(eq(owner.getId()));
        verify(itemRepository, times(1)).findById(eq(item.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(eq(booking.getId()))).thenReturn(Optional.ofNullable(booking));
        when(userRepository.findById(eq(owner.getId()))).thenReturn(Optional.ofNullable(owner));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto responseDto = bookingService.approve(booking.getId(), true, owner.getId());

        assertThat(responseDto.getId(), equalTo(booking.getId()));
        assertThat(responseDto.getStatus(), equalTo(booking.getStatus()));
        assertThat(responseDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(responseDto.getItem().getId(), equalTo(item.getId()));
        assertThat(responseDto.getItem().getName(), equalTo(item.getName()));

        verify(bookingRepository, times(1)).findById(eq(booking.getId()));
        verify(userRepository, times(1)).findById(eq(owner.getId()));
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest_ByNotOwner() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(eq(booking.getId()))).thenReturn(Optional.ofNullable(booking));
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.ofNullable(booker));

        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            bookingService.approve(booking.getId(), true, booker.getId());
        });

        verify(bookingRepository, times(1)).findById(eq(booking.getId()));
        verify(userRepository, times(1)).findById(eq(booker.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void approveTest_ForNotWaitingBooking() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        Item item = getItem(10L, owner);

        Booking booking = getBooking(100L, booker, item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(eq(booking.getId()))).thenReturn(Optional.ofNullable(booking));
        when(userRepository.findById(eq(owner.getId()))).thenReturn(Optional.ofNullable(owner));

        BadRequestException e = assertThrows(BadRequestException.class, () -> {
            bookingService.approve(booking.getId(), true, owner.getId());
        });

        verify(bookingRepository, times(1)).findById(eq(booking.getId()));
        verify(userRepository, times(1)).findById(eq(owner.getId()));
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    private User getUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@user.com")
                .build();
    }

    private Item getItem(Long id, User owner) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .description("ItemDescr " + id)
                .available(true)
                .owner(owner)
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
