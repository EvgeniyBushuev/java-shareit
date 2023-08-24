package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public void delete(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto itemDto = ItemMapper.toDto(item);

        if ((item.getOwner().getId().equals(userId))) {

            addBookingInfo(itemDto);
        }

        addComments(itemDto);

        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByUserId(Long userId, int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        List<Item> items = itemRepository.findAllByOwnerId(userId, pageable);

        List<Long> itemsId = items.stream().map(Item::getId).collect(Collectors.toList());

        List<Booking> itemBookings = bookingRepository.findAllByItemIdIn(itemsId);

        List<Comment> itemComments = commentRepository.findAllByItemIdIn(itemsId);

        List<ItemDto> fullItemDto = new ArrayList<>();

        for (Item item : items) {

            ItemDto itemDto = ItemMapper.toDto(item);

            addBookings(itemDto, itemBookings);

            addItemComments(itemDto, itemComments);

            fullItemDto.add(itemDto);
        }

        return fullItemDto;
    }

    @Override
    @Transactional
    public ItemDto addItem(ItemDto itemDto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.fromDto(itemDto);
        item.setOwner(user);

        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запросов на данную вещь не найдено"));

            item.setItemRequest(itemRequest);
        }

        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(
                "Вещь не найдена"));

        checkOwner(userId, item);

        Optional.ofNullable(itemDto.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(item::setAvailable);


        try {
            return ItemMapper.toDto(itemRepository.save(item));
        } catch (DataIntegrityViolationException e) {
            throw new InvalidDataException("Ошибка целостности данных");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItemsForRent(String text, int from, int size) {

        Pageable pageable = PageRequest.of(from, size);

        if (text.isBlank()) {
            return List.of();
        } else {
            return itemRepository.findBySearchText(text, pageable).stream()
                    .map(ItemMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto, Long userId, Long itemId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь не найдена"));

        Comment comment = CommentMapper.fromDto(commentDto);

        if (bookingRepository.findCountAllApprovedByItemIdAndUserId(itemId, userId, LocalDateTime.now()) == 0) {
            throw new BadRequestException("Комментирование доступно после аренды");
        }

        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private ItemDto addBookings(ItemDto itemDto, List<Booking> bookings) {

        List<Booking> bookingsByItem = bookings
                .stream()
                .filter(booking -> booking.getItem().getId().equals(itemDto.getId()))
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        Booking nextBooking = bookingsByItem.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        Booking lastBooking = bookingsByItem.stream()
                .filter(booking -> booking.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        itemDto.setNextBooking(nextBooking != null ? ItemDto.ItemBooking.builder()
                .id(nextBooking.getId())
                .bookerId(nextBooking.getUser().getId())
                .build() : null);

        itemDto.setLastBooking(lastBooking != null ? ItemDto.ItemBooking.builder()
                .id(lastBooking.getId())
                .bookerId(lastBooking.getUser().getId())
                .build() : null);

        return itemDto;
    }

    private ItemDto addBookingInfo(ItemDto itemDto) {
        List<Booking> bookings = bookingRepository.findAllByItemId(itemDto.getId());

        LocalDateTime now = LocalDateTime.now();

        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);
        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        itemDto.setNextBooking(nextBooking != null ? ItemDto.ItemBooking.builder()
                .id(nextBooking.getId())
                .bookerId(nextBooking.getUser().getId())
                .build() : null);
        itemDto.setLastBooking(lastBooking != null ? ItemDto.ItemBooking.builder()
                .id(lastBooking.getId())
                .bookerId(lastBooking.getUser().getId())
                .build() : null);

        return itemDto;
    }

    private ItemDto addItemComments(ItemDto itemDto, List<Comment> comments) {

        List<CommentDto> itemComments = comments.stream()
                .filter(comment -> comment.getItem().getId().equals(itemDto.getId()))
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());

        itemDto.setComments(itemComments);

        return itemDto;
    }

    private ItemDto addComments(ItemDto itemDto) {
        itemDto.setComments(commentRepository.findAllByItemId(itemDto.getId()).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList()));

        return itemDto;
    }

    private void checkOwner(Long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Отсутствие прав доступа, пользователь " + userId + " не "
                    + "владеет вещью: " + item.getName() + ".");
        }
    }
}