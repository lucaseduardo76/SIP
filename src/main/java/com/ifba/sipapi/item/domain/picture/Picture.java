package com.ifba.sipapi.item.domain.picture;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.item.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "picture")
@NoArgsConstructor
@AllArgsConstructor
public class Picture {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "A url da imagem não pode ser vazia")
    private String url;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    public Picture(String urlImage, Item item) {
        this.url = urlImage;
        this.item = item;
    }

    public void assertBelongsTo(Item item) {
        if(!item.getId().equals(this.item.getId()))
            throw APIException.build(HttpStatus.BAD_REQUEST, "Imagem não pertence a item selecionado");
    }
}
