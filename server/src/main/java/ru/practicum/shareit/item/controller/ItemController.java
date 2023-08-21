package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.util.RequestHeader.SHARER_USER_ID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestBody ItemDto itemDto,
                           @RequestHeader(SHARER_USER_ID) Long userId) {
        log.info("Запрос на добавление новой вещи {} пользователем с id = {}", itemDto, userId);
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestHeader(SHARER_USER_ID) Long userId,
                              @RequestBody ItemDto itemDto) {
        log.info("Запрос на обновление вещи {}", itemDto);
        return itemService.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(SHARER_USER_ID) Long userId,
                           @PathVariable Long itemId) {
        log.info("Запрос вещи ID: {}", itemId);
        return itemService.getItem(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllByOwnerId(@RequestHeader(SHARER_USER_ID) Long userId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "20") int size) {

        log.info("Запрос списка вещей пользователя ID: {}", userId);
        return itemService.getItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestParam(defaultValue = "0") int from,
                                     @RequestParam(defaultValue = "20") int size) {

        log.info("Поисковыый запрос {}", text);
        return itemService.searchItemsForRent(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@PathVariable Long itemId,
                                    @RequestHeader(SHARER_USER_ID) Long userId,
                                    @RequestBody CommentDto commentDto) {
        log.info("Запрос на создание комментария к вещи ID: {}, от пользователя ID: {} ",
                itemId, userId);
        return itemService.createComment(commentDto, userId, itemId);
    }
}