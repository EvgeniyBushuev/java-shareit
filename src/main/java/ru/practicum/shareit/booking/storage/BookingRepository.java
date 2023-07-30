package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUserIdOrderByStartDesc(Long userId);

    List<Booking> findAllByUserIdAndEndBeforeOrderByStartDesc(Long userId,
            LocalDateTime endDateTime);

    List<Booking> findAllByUserIdAndStartAfterOrderByStartDesc(Long userId,
            LocalDateTime startDateTime);

    List<Booking> findAllByUserIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId,
            LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Booking> findAllByUserIdAndStatusOrderByStartDesc(Long userId,
            BookingStatus bookingStatus);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId,
            LocalDateTime endDateTime);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId,
            LocalDateTime startDateTime);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId,
            LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    List<Booking> findAllByItemId(Long itemId);

    @Query("select count(b) from Booking b where b.item.id = :itemId and b.user.id = :userId and b"
            + ".status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED and b.end < "
            + ":currentTime")
    Integer findAllApprovedByItemIdAndUserId(Long itemId, Long userId,
            LocalDateTime currentTime);
}
