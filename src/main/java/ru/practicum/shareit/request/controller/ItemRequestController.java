package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
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
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping
    public List<ItemRequestGetResponseDto> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                           @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                                           @RequestParam(required = false, defaultValue = "20") @Min(1) int size) {
        return itemRequestService.getAllByRequesterId(userId, from, size);
    }

    @GetMapping("/all")
    public List<ItemRequestGetResponseDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(required = false, defaultValue = "20") @Min(1) int size) {
        return itemRequestService.getAll(userId, from, size);
    }

    @GetMapping("/{itemRequestId}")
    public ItemRequestGetResponseDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemRequestId) {
        return itemRequestService.getById(userId, itemRequestId);
    }

    @PostMapping
    public ItemRequestCreateResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        return itemRequestService.create(itemRequestCreateDto, userId);
    }
}
