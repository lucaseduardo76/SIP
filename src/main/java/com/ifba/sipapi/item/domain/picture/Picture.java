package com.ifba.sipapi.item.domain.picture;

import com.ifba.sipapi.item.domain.item.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "picture")
public class Picture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "A url da imagem não pode ser vazia")
    private String url;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
}
