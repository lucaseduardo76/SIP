package com.ifba.sipapi.item.infra.item;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.item.Status;
import com.ifba.sipapi.item.dto.ItemResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;
import java.util.*;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    @Query("SELECT i.code FROM Item i WHERE i.category = :category ORDER BY i.code DESC")
    List<String> findItemCodesByCategory(@Param("category") Category category);

    @Query("SELECT i FROM Item i WHERE i.status = :status")
    Page<Item> findAllItemByStatus(@Param("status") Status status, Pageable pageable);

    @Query("""
    SELECT i
    FROM Item i
    WHERE (i.findingAt >= :dateToSearch)
      AND (i.donationDate <= :dateCloseToDonation)
      AND (i.category IN :categories)
      AND (i.status = :status)
""")
    Page<Item> findByFilterQuery(Pageable pageable,
                                 @Param("dateToSearch") LocalDate dateToSearch,
                                 @Param("dateCloseToDonation") LocalDate dateCloseToDonation,
                                 @Param("categories") List<Category> categories,
                                 @Param("status") Status status);

    @Query("""
        SELECT i
        FROM Item i
        WHERE (i.findingAt >= :dateToSearch)
          AND (i.donationDate <= :dateCloseToDonation)
          AND (i.status = :status)
    """)
    Page<Item> findByFilterQuery(Pageable pageable,
                                            @Param("dateToSearch") LocalDate dateToSearch,
                                            @Param("dateCloseToDonation")LocalDate dateCloseToDonation,
                                            @Param("status") Status status);
}
