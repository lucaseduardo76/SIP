package com.ifba.sipapi.recoveryRequest.domain;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.domain.item.Item;
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
public class RecoveryRequest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusRequest status;

    @Column(nullable = false)
    private LocalDateTime recoveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
