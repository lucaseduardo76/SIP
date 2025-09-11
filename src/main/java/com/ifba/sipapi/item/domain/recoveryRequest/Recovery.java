package com.ifba.sipapi.item.domain.recoveryRequest;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.dto.ItemRecoveryRequestDto;
import com.ifba.sipapi.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
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
}
