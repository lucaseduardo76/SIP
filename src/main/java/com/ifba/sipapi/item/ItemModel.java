package com.ifba.sipapi.item;

import com.ifba.sipapi.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.ifba.sipapi.pictures.PictureModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "item")
public class ItemModel extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Descrição não pode ser vazia")
    private String description;

    @Column(nullable = false)
    @NotBlank(message = "A cor não pode ser vazia")
    private String color;

    @Column(nullable = false)
    @NotBlank(message = "A data em que o item foi achado deve ser preenchida")
    private Date finding_date;

    @Column(nullable = false)
    @NotBlank(message = "O status deve ser preenchido com os valores: DISPONIBLE | CLAIMED | CHARITY")
    private Status status;

    private DayPeriod day_period;

    private Date date_return;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PictureModel> pictures;

}
