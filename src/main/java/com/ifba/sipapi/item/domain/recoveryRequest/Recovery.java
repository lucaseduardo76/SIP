package com.ifba.sipapi.item.domain.recoveryRequest;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.dto.ItemRecoveryRequestDto;
import com.ifba.sipapi.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recovery_request")
public class Recovery extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusRecovery status;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Column(nullable = false)
    private LocalDateTime recoveryDateTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Recovery(ItemRecoveryRequestDto itemRecoveryRequestDto, User user, Item item) {
        this.item = item;
        this.user = user;
        this.description = itemRecoveryRequestDto.getDescription();
        this.status = StatusRecovery.PENDING;
        this.requestDate = LocalDateTime.now();
        this.recoveryDateTime = itemRecoveryRequestDto.getDateTime();
    }

    public void processRequestAcceptance(StatusRecovery newStatus) {
        if(newStatus == null)
            throw APIException.build(HttpStatus.BAD_REQUEST, "status não pode ser null");

        if (this.status != StatusRecovery.PENDING)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Status da solicitação não pode mais ser alterado");

        validateAndUpdateStatus(newStatus);
    }

    private void validateAndUpdateStatus(StatusRecovery newStatus) {
        switch (newStatus) {
            case APPROVED -> this.status = StatusRecovery.APPROVED;
            case REFUSED -> this.status = StatusRecovery.REFUSED;
            default -> throw APIException.build(HttpStatus.BAD_REQUEST, "Status inválido");
        }
    }


}
