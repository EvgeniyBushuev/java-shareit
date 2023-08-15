package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на добавление новой вещи {} пользователем с id = {}", itemDto, userId);
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemDto itemDto) {
        log.info("Запрос на обновление вещи {}", itemDto);
        return itemService.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId) {
        log.info("Запрос вещи ID: {}", itemId);
        return itemService.getItem(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                         @RequestParam(required = false, defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        log.info("Запрос списка вещей пользователя ID: {}", userId);
        return itemService.getItemsByUserId(userId, pageable);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                     @RequestParam(required = false, defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        log.info("Поисковыый запрос {}", text);
        return itemService.searchItemsForRent(text, pageable);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody CommentDto commentDto) {
        log.info("Запрос на создание комментария к вещи ID: {}, от пользователя ID: {} ",
                itemId, userId);
        return itemService.createComment(commentDto, userId, itemId);
    }
}
