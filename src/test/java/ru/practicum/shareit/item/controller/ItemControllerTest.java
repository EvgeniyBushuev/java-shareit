package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;

    @Test
    void createTest() throws Exception {

        ItemDto requestDto = getRequestDto();
        ItemDto responseDto = getItemResponseDto(10L);

        when(itemService.addItem(any(ItemDto.class), eq(1L))).thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemService, times(1)).addItem(any(ItemDto.class), eq(1L));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void createCommentTest() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        CommentDto requestDto = CommentDto.builder()
                .text("Комментарий для вещи")
                .build();

        CommentDto responseDto = CommentDto.builder()
                .id(100L)
                .build();

        when(itemService.createComment(any(CommentDto.class), eq(userId), eq(itemId))).thenReturn(responseDto);

        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemService, times(1)).createComment(any(CommentDto.class), eq(userId), eq(itemId));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getByIdTest() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "test", "test", true, null, null, null, null);

        when(itemService.getItem(anyLong(), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwnerId() throws Exception {
        Long userId = 1L;

        ItemDto responseDto1 = getItemResponseDto(10L);
        ItemDto responseDto2 = getItemResponseDto(11L);

        List<ItemDto> responseDtoList = Arrays.asList(
                responseDto1,
                responseDto2
        );

        when(itemService.getItemsByUserId(eq(userId), any(Pageable.class))).thenReturn(responseDtoList);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()));

        verify(itemService, times(1)).getItemsByUserId(eq(userId), any());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getAllBySearchTextTest() throws Exception {
        ItemDto responseDto1 = getItemResponseDto(10L);
        ItemDto responseDto2 = getItemResponseDto(11L);

        List<ItemDto> responseDtoList = Arrays.asList(
                responseDto1,
                responseDto2
        );

        when(itemService.searchItemsForRent(anyString(), any(Pageable.class))).thenReturn(responseDtoList);

        mockMvc.perform(get("/items/search")
                        .param("text", "text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()));

        verify(itemService, times(1)).searchItemsForRent(eq("text"), any(Pageable.class));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void update() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;

        ItemDto requestDto = getRequestDto();
        ItemDto responseDto = getItemResponseDto(10L);

        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemService, times(1)).updateItem(eq(itemId), eq(userId), any(ItemDto.class));
        verifyNoMoreInteractions(itemService);
    }

    private ItemDto getRequestDto() {
        return ItemDto.builder()
                .name("Вещь")
                .description("Обычная")
                .available(true)
                .build();
    }

    private ItemDto getItemResponseDto(Long id) {
        return ItemDto.builder()
                .id(id)
                .build();
    }
}