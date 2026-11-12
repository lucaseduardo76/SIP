package com.ifba.sipapi.item.domain.item;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.picture.Picture;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.item.dto.ItemEditRequestDto;
import com.ifba.sipapi.item.dto.ItemRequestDto;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.notification.domain.Notification;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.util.HandleString;
import com.ifba.sipapi.util.ItemHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Log4j2
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
    private LocalDate findingAt;

    @Column(nullable = false)
    private LocalDate donationDate;

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
    private List<Notification> notifications;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recovery> recoveries;

    public Item(ItemRequestDto itemRequestDto, String code, Integer donationTime) {
        this.description = HandleString.capitalize(itemRequestDto.getDescription());
        this.findingAt = itemRequestDto.getFinding_date();
        this.status = Status.DISPONIBLE;
        this.dayPeriod = itemRequestDto.getDay_period();
        this.category = itemRequestDto.getCategory();
        this.area = itemRequestDto.getArea();
        this.code = code;
        this.donationDate =  itemRequestDto.getFinding_date().plusDays(donationTime);
    }

    public void update(ItemEditRequestDto itemEditRequestDto) {
        this.area = itemEditRequestDto.getArea() != null ? itemEditRequestDto.getArea() : this.area;
        this.description = HandleString.capitalize(ItemHelper.getNewDescriptionOrDefault(itemEditRequestDto.getDescription(), this.description));
        this.category = itemEditRequestDto.getCategory() != null ? itemEditRequestDto.getCategory() : this.category;
    }

    public void validateItemIsAvailableToUpdate() {
        validateItemIsAvailable();
    }

    private void validateItemIsAvailable() {
        if(status != Status.DISPONIBLE)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Item não está mais disponível");
    }


    /**
     * Reserva o item para o solicitante cujo pedido foi aprovado.
     * O item fica aguardando a retirada e ainda não é considerado devolvido.
     */
    public void reserve(Recovery recovery) {
        validateRecoveryNotNull(recovery);
        validateItemIsAvailable();
        validateRecoveryBelongsToItem(recovery);
        validateRecoveryApproved(recovery);
        this.status = Status.RESERVED;
    }

    /**
     * Registra a entrega do item ao solicitante, confirmada pelo administrador no momento da retirada.
     */
    public void updateStatusToClaimed(Recovery recovery) {
        validateRecoveryNotNull(recovery);
        if (this.status != Status.RESERVED)
            throw APIException.build(HttpStatus.BAD_REQUEST, "O item não está reservado para retirada");

        validateRecoveryBelongsToItem(recovery);
        validateRecoveryApproved(recovery);
        updateOwner(recovery.getUser());
        updateDateReturned();
        this.status = Status.CLAIMED;
    }

    /** Libera a reserva do item, que volta a ficar disponível para novos pedidos. */
    public void releaseReservation() {
        if (this.status != Status.RESERVED)
            throw APIException.build(HttpStatus.BAD_REQUEST, "O item não está reservado");
        this.status = Status.DISPONIBLE;
    }

    private void validateRecoveryNotNull(Recovery recovery) {
        if(recovery == null)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Recovery não pode ser nulo");
    }

    private void updateDateReturned() {
        this.dateReturned = LocalDate.now();
    }

    private void updateOwner(User user) {
        if(user == null)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Solicitação precisa de um usuario como referencia");
        this.owner = user;
    }

    private void validateRecoveryBelongsToItem(Recovery recovery) {
        if (!Objects.equals(recovery.getItem(), this))
            throw APIException.build(HttpStatus.CONFLICT, "Item da solicitação é incompatível com o item atual");
    }

    private void validateRecoveryApproved(Recovery recovery) {
        if (recovery.getStatus() != StatusRecovery.APPROVED)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Solicitação ainda não foi autorizada");
    }

    public void setToCharity() {
        this.status = Status.CHARITY;
        log.info("Item code={} set to CHARITY", this.getCode());
    }
}
