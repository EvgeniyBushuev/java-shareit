package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestGetResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void getAllByOwnerIdTest() throws Exception {
        Long userId = 1L;

        ItemRequestGetResponseDto responseDto1 = getResponseDto(10L);
        ItemRequestGetResponseDto responseDto2 = getResponseDto(11L);

        List<ItemRequestGetResponseDto> responseDtoList = Arrays.asList(
                responseDto1,
                responseDto2
        );

        when(itemRequestService.getAllByRequesterId(eq(userId), anyInt(), anyInt())).thenReturn(responseDtoList);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()));

        verify(itemRequestService, times(1)).getAllByRequesterId(eq(userId), anyInt(), anyInt());
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getAllTest() throws Exception {
        Long userId = 1L;

        ItemRequestGetResponseDto responseDto1 = getResponseDto(10L);
        ItemRequestGetResponseDto responseDto2 = getResponseDto(11L);

        List<ItemRequestGetResponseDto> responseDtoList = Arrays.asList(
                responseDto1,
                responseDto2
        );

        when(itemRequestService.getAll(eq(userId), anyInt(), anyInt())).thenReturn(responseDtoList);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()));

        verify(itemRequestService, times(1)).getAll(eq(userId), anyInt(), anyInt());
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getByIdTest() throws Exception {
        Long userId = 1L;

        ItemRequestGetResponseDto responseDto = getResponseDto(10L);

        when(itemRequestService.getById(eq(userId), eq(responseDto.getId()))).thenReturn(responseDto);

        mockMvc.perform(get("/requests/" + responseDto.getId())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemRequestService, times(1)).getById(eq(userId), eq(responseDto.getId()));
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void createTest() throws Exception {
        Long userId = 1L;

        ItemRequestCreateDto requestDto = new ItemRequestCreateDto("TestItem");

        ItemRequestCreateResponseDto responseDto = ItemRequestCreateResponseDto.builder()
                .id(1L)
                .build();

        when(itemRequestService.create(any(ItemRequestCreateDto.class), eq(userId))).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(itemRequestService, times(1)).create(any(ItemRequestCreateDto.class), eq(userId));
        verifyNoMoreInteractions(itemRequestService);
    }

    private ItemRequestGetResponseDto getResponseDto(Long id) {
        return ItemRequestGetResponseDto.builder()
                .id(id)
                .build();
    }
}
