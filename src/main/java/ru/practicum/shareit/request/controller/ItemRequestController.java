package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping
    public List<ItemRequestGetResponseDto> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                           @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                                           @RequestParam(required = false, defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        log.info("Запрос списка всех заявок на вещи пользователя {}", userId);
        return itemRequestService.getAllByRequesterId(userId, pageable);
    }

    @GetMapping("/all")
    public List<ItemRequestGetResponseDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(required = false, defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        log.info("Запрос от пользователя id {} списока всех заявок на вещи", userId);
        return itemRequestService.getAll(userId, pageable);
    }

    @GetMapping("/{itemRequestId}")
    public ItemRequestGetResponseDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemRequestId) {

        log.info("Запрос от пользователя id {} заявки на вещь id {}", userId, itemRequestId);
        return itemRequestService.getById(userId, itemRequestId);
    }

    @PostMapping
    public ItemRequestCreateResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {

        log.info("Запрос от пользователя id {} на создание заявки", userId);
        return itemRequestService.create(itemRequestCreateDto, userId);
    }
}
