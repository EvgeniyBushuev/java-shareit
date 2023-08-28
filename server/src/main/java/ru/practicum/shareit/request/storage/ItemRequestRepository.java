package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequesterIdNotOrderByCreatedDesc(Long userId, Pageable pageable);

    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long requesterId, Pageable pageable);
}
