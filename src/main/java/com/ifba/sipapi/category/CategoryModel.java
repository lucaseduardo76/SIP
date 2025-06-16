package com.ifba.sipapi.category;

import com.ifba.sipapi.item.ItemModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "category")
public class CategoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "O nome da categoria não pode ser nulo")
    private String name;

    @OneToMany(mappedBy = "category")
    private List<ItemModel> items;
}
