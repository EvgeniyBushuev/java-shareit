package ru.practicum.shareit.booking.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class BookingIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    EntityManager entityManager;

    @Test
    @Order(1)
    @Transactional
    @Rollback(false)
    void createBookingTest() {
        User owner = getUser(1L);
        User booker = getUser(2L);

        TypedQuery<Long> userCountQuery = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u", Long.class
        );

        Long userCount = userCountQuery.getSingleResult();
        assertThat(userCount, equalTo(2L));

        Item item = getItem(1L, owner);

        TypedQuery<Long> itemCountQuery = entityManager.createQuery(
                "SELECT COUNT(i) FROM Item i", Long.class
        );

        Long itemCount = itemCountQuery.getSingleResult();
        assertThat(itemCount, equalTo(1L));

        Booking booking1 = getBooking(1L, booker, item, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Booking booking2 = getBooking(2L, booker, item, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));
        Booking booking3 = getBooking(3L, booker, item, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6));
        Booking booking4 = getBooking(4L, booker, item, LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(8));
        Booking booking5 = getBooking(5L, booker, item, LocalDateTime.now().plusDays(9), LocalDateTime.now().plusDays(10));
        Booking booking6 = getBooking(6L, booker, item, LocalDateTime.now().plusDays(11), LocalDateTime.now().plusDays(12));
        Booking booking7 = getBooking(7L, booker, item, LocalDateTime.now().plusDays(13), LocalDateTime.now().plusDays(14));

        TypedQuery<Long> bookingCountQuery = entityManager.createQuery(
                "SELECT COUNT(b) FROM Booking b", Long.class);

        Long bookingCount = bookingCountQuery.getSingleResult();

        assertThat(bookingCount, equalTo(7L));
    }

    @Test
    @Order(2)
    @Transactional
    void paginationTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "5")
                        .header("X-Sharer-User-Id", String.valueOf(1)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();

        List<BookingResponseDto> bookingList = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookingResponseDto.class));

        assertThat(bookingList.size(), equalTo(5));

        assertThat(bookingList.get(0).getId(), equalTo(7L));
        assertThat(bookingList.get(1).getId(), equalTo(6L));
        assertThat(bookingList.get(2).getId(), equalTo(5L));
        assertThat(bookingList.get(3).getId(), equalTo(4L));
        assertThat(bookingList.get(4).getId(), equalTo(3L));

        mvcResult = mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "5")
                        .param("size", "5")
                        .header("X-Sharer-User-Id", String.valueOf(1)))
                .andExpect(status().isOk())
                .andReturn();

        jsonResponse = mvcResult.getResponse().getContentAsString();

        bookingList = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookingResponseDto.class));

        assertThat(bookingList.size(), equalTo(2));

        assertThat(bookingList.get(0).getId(), equalTo(2L));
        assertThat(bookingList.get(1).getId(), equalTo(1L));

        mvcResult = mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "10")
                        .param("size", "5")
                        .header("X-Sharer-User-Id", String.valueOf(1)))
                .andExpect(status().isOk())
                .andReturn();

        jsonResponse = mvcResult.getResponse().getContentAsString();

        bookingList = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookingResponseDto.class));

        assertThat(bookingList.size(), equalTo(0));

        mvcResult = mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "4")
                        .header("X-Sharer-User-Id", String.valueOf(1)))
                .andExpect(status().isOk())
                .andReturn();

        jsonResponse = mvcResult.getResponse().getContentAsString();

        bookingList = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookingResponseDto.class));

        assertThat(bookingList.size(), equalTo(4));

        assertThat(bookingList.get(0).getId(), equalTo(7L));
        assertThat(bookingList.get(1).getId(), equalTo(6L));
        assertThat(bookingList.get(2).getId(), equalTo(5L));
        assertThat(bookingList.get(3).getId(), equalTo(4L));

        mvcResult = mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "4")
                        .param("size", "4")
                        .header("X-Sharer-User-Id", String.valueOf(1)))
                .andExpect(status().isOk())
                .andReturn();

        jsonResponse = mvcResult.getResponse().getContentAsString();

        bookingList = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, BookingResponseDto.class));

        assertThat(bookingList.size(), equalTo(3));

        assertThat(bookingList.get(0).getId(), equalTo(3L));
        assertThat(bookingList.get(1).getId(), equalTo(2L));
        assertThat(bookingList.get(2).getId(), equalTo(1L));
    }

    private User getUser(Long id) {
        User user = User.builder()
                .name("User " + id)
                .email("user" + id + "@user.com")
                .build();

        entityManager.persist(user);

        return user;
    }

    private Item getItem(Long id, User owner) {
        Item item = Item.builder()
                .name("Item " + id)
                .description("ItemDescr " + id)
                .available(true)
                .owner(owner)
                .build();

        entityManager.persist(item);

        return item;
    }

    private Booking getBooking(Long id, User booker, Item item, LocalDateTime start, LocalDateTime end) {
        Booking booking = Booking.builder()
                .status(BookingStatus.APPROVED)
                .user(booker)
                .item(item)
                .start(start)
                .end(end)
                .build();

        entityManager.persist(booking);

        return booking;
    }
}
