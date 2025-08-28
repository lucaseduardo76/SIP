package com.ifba.sipapi.item.domain.item;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.domain.picture.Picture;
import com.ifba.sipapi.item.dto.ItemRequestDto;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.util.HandleString;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "item")
public class Item extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private LocalDate findingAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    private DayPeriod dayPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Area area;

    private LocalDate returnedAt;

    private String code;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Picture> pictures;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    public Item(ItemRequestDto itemRequestDto, String code) {
        this.description = HandleString.capitalize(itemRequestDto.getDescription());
        this.color = HandleString.toLowerCase(itemRequestDto.getColor());
        this.findingAt = itemRequestDto.getFinding_date();
        this.status = Status.DISPONIBLE;
        this.dayPeriod = itemRequestDto.getDay_period();
        this.category = itemRequestDto.getCategory();
        this.area = itemRequestDto.getArea();
        this.code = code;
    }
}
