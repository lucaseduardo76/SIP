package com.ifba.sipapi.item.infra.recovery;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RecoveryRepository extends JpaRepository<Recovery, UUID> {
    List<Recovery> findAllByItem(Item item);
    List<Recovery>findAllByUser(User user);
    long countByUserAndStatus(User user, StatusRecovery statusRecovery);
    boolean existsByUserAndItemAndStatusNot(User user, Item item, StatusRecovery statusRecovery);
    Page<Recovery> findAllByStatus(StatusRecovery status, Pageable pageable);
    List<Recovery> findAllByUserAndStatus(User user, StatusRecovery status);

    @Query("""
        SELECT r
        FROM Recovery r
        JOIN r.item i
        WHERE r.user = :user
          AND (:status IS NULL OR r.status = :status)
          AND (:categories IS NULL OR i.category IN :categories)
          AND (:itemName IS NULL OR LOWER(i.description) LIKE LOWER(CAST(:itemName AS string)))
          AND (r.requestDate >= COALESCE(:startDateTime, r.requestDate))
          AND (r.requestDate <= COALESCE(:endDateTime, r.requestDate))
    """)
    Page<Recovery> findSelfRecoveriesByFilter(
            @Param("user") User user,
            @Param("status") StatusRecovery status,
            @Param("categories") List<Category> categories,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("itemName") String itemName,
            Pageable pageable
    );

}
