package com.ifba.sipapi.pictures;

import com.ifba.sipapi.item.ItemModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "picture")
public class PictureModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "A url da imagem não pode ser vazia")
    private String url;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private ItemModel item;
}
