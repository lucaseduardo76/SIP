package com.ifba.sipapi.item.infra.item;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.item.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    @Query("SELECT i.code FROM Item i WHERE i.category = :category ORDER BY i.code DESC")
    List<String> findItemCodesByCategory(@Param("category") Category category);

    @Query("SELECT i FROM Item i WHERE i.status = :status")
    Page<Item> findAllItemByStatus(@Param("status") Status status, Pageable pageable);

    @Query("""
        SELECT i
        FROM Item i
        WHERE (i.donationDate <= COALESCE(:dateCloseToDonation, i.donationDate))
          AND (i.description LIKE COALESCE(:itemName, i.description))
          AND (:categories IS NULL OR i.category IN :categories)
          AND (:status IS NULL OR i.status = :status)
          AND (i.findingAt >= COALESCE(:startPeriod, i.findingAt))
          AND (i.findingAt <= COALESCE(:endPeriod, i.findingAt))
    """)
    Page<Item> findByFilterQuery(
            Pageable pageable,
            @Param("dateCloseToDonation") LocalDate dateCloseToDonation,
            @Param("categories") List<Category> categories,
            @Param("status") Status status,
            @Param("itemName") String itemName,
            @Param("startPeriod") LocalDate startPeriod,
            @Param("endPeriod") LocalDate endPeriod
            );

}
