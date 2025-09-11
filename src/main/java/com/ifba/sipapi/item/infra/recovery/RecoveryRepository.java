package com.ifba.sipapi.item.infra.recovery;

import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface RecoveryRepository extends JpaRepository<Recovery, UUID> {
    Boolean existsByUserAndItem(User user, Item item);
    List<Recovery> findByUserAndStatus(User user, StatusRecovery status);
}
