package com.ifba.sipapi.region;

import com.ifba.sipapi.item.ItemModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "region")
public class RegionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "O nome da região onde o item foi achado não pode ser vazio")
    private String name;

    @OneToMany(mappedBy = "region")
    private List<ItemModel> items;
}
