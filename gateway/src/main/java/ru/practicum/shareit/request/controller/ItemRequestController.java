package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.util.RequestHeader.SHARER_USER_ID;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader(SHARER_USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(defaultValue = "20") @Min(1) int size) {

        log.info("Запрос списка всех заявок на вещи пользователя {}", userId);
        return itemRequestClient.getAllByRequesterId(userId, from, size);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(SHARER_USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(defaultValue = "20") @Min(1) int size) {

        log.info("Запрос от пользователя id {} списока всех заявок на вещи", userId);
        return itemRequestClient.getAll(userId, from, size);
    }

    @GetMapping("/{itemRequestId}")
    public ResponseEntity<Object> getById(@RequestHeader(SHARER_USER_ID) Long userId,
                                             @PathVariable Long itemRequestId) {

        log.info("Запрос от пользователя id {} заявки на вещь id {}", userId, itemRequestId);
        return itemRequestClient.getById(userId, itemRequestId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(SHARER_USER_ID) Long userId,
                                               @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {

        log.info("Запрос от пользователя id {} на создание заявки", userId);
        return itemRequestClient.create(itemRequestCreateDto, userId);
    }
}
