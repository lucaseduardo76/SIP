package com.ifba.sipapi.item.domain.item;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.picture.Picture;
import com.ifba.sipapi.item.dto.ItemEditRequestDto;
import com.ifba.sipapi.item.dto.ItemRequestDto;
import com.ifba.sipapi.recoveryRequest.domain.RecoveryRequest;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.util.HandleString;
import com.ifba.sipapi.util.ItemHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "item")
@NoArgsConstructor
@AllArgsConstructor
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

    private LocalDate dateReturned;

    private String code;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Picture> pictures;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecoveryRequest> recoveryRequests;

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

    public void update(ItemEditRequestDto itemEditRequestDto) {
        this.area = itemEditRequestDto.getArea() != null ? itemEditRequestDto.getArea() : this.area;
        this.description = HandleString.capitalize(ItemHelper.getOrDefault(itemEditRequestDto.getDescription(), this.description));
        this.color = HandleString.toLowerCase(ItemHelper.getOrDefault(itemEditRequestDto.getColor(), this.color));
        this.category = itemEditRequestDto.getCategory() != null ? itemEditRequestDto.getCategory() : this.category;
    }

    public void validateItemIsAvailableToUpdate() {
        validateItemIsAvailable();
        LocalDateTime limit = super.getCreatedAt().plusHours(24);

        if(LocalDateTime.now().isAfter(limit))
            throw APIException.build(HttpStatus.BAD_REQUEST, "Itens só podem ser editados até 24 horas após a criação");
    }

    private void validateItemIsAvailable() {
        if(status != Status.DISPONIBLE)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Item não está mais disponível");
    }



}
