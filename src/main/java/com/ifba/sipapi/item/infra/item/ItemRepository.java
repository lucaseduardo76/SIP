package com.ifba.sipapi.item.infra.item;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.*;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    @Query("SELECT i.code FROM Item i WHERE i.category = :category ORDER BY i.code DESC")
    List<String> findItemCodesByCategory(@Param("category") Category category);

}
