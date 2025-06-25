package com.ifba.sipapi.category.domain;

import com.ifba.sipapi.item.domain.item.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "O nome da categoria não pode ser nulo")
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Item> items;
}
