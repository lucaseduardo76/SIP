package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserDetailsResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class RecoveryResponseByUser {


    private final UserDetailsResponseDto user;
    private final List<RecoveryResp> recovery;

    public RecoveryResponseByUser(List<Recovery> recovery, User user) {
        this.recovery = recovery.stream().map(RecoveryResp::new).toList();
        if(!this.recovery.isEmpty() && !recovery.get(0).getUser().equals(user))
            throw APIException.build(HttpStatus.CONFLICT, "Usuarios não são iguais, procure suporte");

        this.user = new UserDetailsResponseDto(user);
    }

    @Getter
    public static class RecoveryResp{
        private final UUID id;
        private final String description;
        private final StatusRecovery status;
        private final LocalDateTime pickupDate;
        private final LocalDateTime requestDate;
        private final ItemResponseDto item;

        public RecoveryResp(Recovery recovery) {
            this.id = recovery.getId();
            this.description = recovery.getDescription();
            this.status = recovery.getStatus();
            this.requestDate = recovery.getRequestDate();
            this.pickupDate = recovery.getRecoveryDateTime();
            this.item = new ItemResponseDto(recovery.getItem());
        }
    }
}
