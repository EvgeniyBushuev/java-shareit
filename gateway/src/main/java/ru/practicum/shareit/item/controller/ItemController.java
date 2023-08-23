package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.util.RequestHeader.SHARER_USER_ID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable int itemId) {
        itemClient.delete(itemId);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@Valid @RequestBody ItemDto itemDto,
                                          @RequestHeader(SHARER_USER_ID) Long userId) {
        log.info("Запрос на добавление новой вещи {} пользователем с id = {}", itemDto, userId);
        return itemClient.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId,
            @RequestHeader(SHARER_USER_ID) Long userId,
            @RequestBody ItemDto itemDto) {
        log.info("Запрос на обновление вещи {}", itemDto);
        return itemClient.update(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(SHARER_USER_ID) Long userId,
            @PathVariable Long itemId) {
        log.info("Запрос вещи ID: {}", itemId);
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader(SHARER_USER_ID) Long userId,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "20") @Min(1) int size) {

        log.info("Запрос списка вещей пользователя ID: {}", userId);
        return itemClient.getItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                     @RequestParam(defaultValue = "0") @Min(0) int from,
                                     @RequestParam(defaultValue = "20") @Min(1) int size) {

        log.info("Поисковыый запрос {}", text);
        return itemClient.searchItemsForRent(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable Long itemId,
            @RequestHeader(SHARER_USER_ID) Long userId,
            @Valid @RequestBody CommentDto commentDto) {
        log.info("Запрос на создание комментария к вещи ID: {}, от пользователя ID: {} ",
                itemId, userId);
        return itemClient.createComment(commentDto, userId, itemId);
    }
}
