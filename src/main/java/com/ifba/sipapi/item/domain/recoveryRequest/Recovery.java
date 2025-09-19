package com.ifba.sipapi.item.domain.recoveryRequest;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.dto.ItemRecoveryRequestDto;
import com.ifba.sipapi.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@EqualsAndHashCode
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Recovery(ItemRecoveryRequestDto itemRecoveryRequestDto, User user, Item item) {
        this.item = item;
        this.user = user;
        this.description = itemRecoveryRequestDto.getDescription();
        this.status = StatusRecovery.PENDING;
        this.requestDate = LocalDateTime.now();
    }

    public void processRequestAcceptance(StatusRecovery newStatus) {
        if(newStatus == null)
            throw APIException.build(HttpStatus.BAD_REQUEST, "status não pode ser null");


        if (this.status != StatusRecovery.PENDING) {
            System.out.println("O status é: " + this.status);
            throw APIException.build(HttpStatus.BAD_REQUEST, "Status da solicitação não pode mais ser alterado");
        }
        updateRequestAcceptance(newStatus);
    }

    private void updateRequestAcceptance(StatusRecovery newStatus) {
        switch (newStatus) {
            case APPROVED -> this.status = StatusRecovery.APPROVED;
            case REFUSED -> this.status = StatusRecovery.REFUSED;
            default -> throw APIException.build(HttpStatus.BAD_REQUEST, "Status inválido");
        }
    }


}
