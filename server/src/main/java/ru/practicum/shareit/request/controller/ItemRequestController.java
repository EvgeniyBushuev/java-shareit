package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.util.RequestHeader.SHARER_USER_ID;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping
    public List<ItemRequestGetResponseDto> getAllByOwnerId(@RequestHeader(SHARER_USER_ID) Long userId,
                                                           @RequestParam(defaultValue = "0") int from,
                                                           @RequestParam(defaultValue = "20") int size) {

        log.info("Запрос списка всех заявок на вещи пользователя {}", userId);
        return itemRequestService.getAllByRequesterId(userId, from, size);
    }

    @GetMapping("/all")
    public List<ItemRequestGetResponseDto> getAll(@RequestHeader(SHARER_USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "20") int size) {

        log.info("Запрос от пользователя id {} списока всех заявок на вещи", userId);
        return itemRequestService.getAll(userId, from, size);
    }

    @GetMapping("/{itemRequestId}")
    public ItemRequestGetResponseDto getById(@RequestHeader(SHARER_USER_ID) Long userId,
                                             @PathVariable Long itemRequestId) {

        log.info("Запрос от пользователя id {} заявки на вещь id {}", userId, itemRequestId);
        return itemRequestService.getById(userId, itemRequestId);
    }

    @PostMapping
    public ItemRequestCreateResponseDto create(@RequestHeader(SHARER_USER_ID) Long userId,
                                               @RequestBody ItemRequestCreateDto itemRequestCreateDto) {

        log.info("Запрос от пользователя id {} на создание заявки", userId);
        return itemRequestService.create(itemRequestCreateDto, userId);
    }
}