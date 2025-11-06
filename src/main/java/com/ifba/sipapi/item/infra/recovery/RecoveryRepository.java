package com.ifba.sipapi.item.infra.recovery;

import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecoveryRepository extends JpaRepository<Recovery, UUID> {
    List<Recovery> findAllByItem(Item item);
    List<Recovery>findAllByUser(User user);
    long countByUserAndStatus(User user, StatusRecovery statusRecovery);
    boolean existsByUserAndItemAndStatusNot(User user, Item item, StatusRecovery statusRecovery);
}
