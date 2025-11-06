package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.user.dto.UserDetailsResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class RecoveryResponseByItem {


    private ItemResponseDto item;
    private final List<RecoveryResp> recovery;

    public RecoveryResponseByItem(List<Recovery> recovery) {
        this.recovery = recovery.stream().map(RecoveryResp::new).toList();
        if(!recovery.isEmpty()) this.item = new ItemResponseDto(recovery.get(0).getItem());
    }

    @Getter
    public static class RecoveryResp{
        private final UUID id;
        private final String description;
        private final StatusRecovery status;
        private final UserDetailsResponseDto user;
        private final LocalDateTime requestDate;

        public RecoveryResp(Recovery recovery) {
            this.id = recovery.getId();
            this.description = recovery.getDescription();
            this.status = recovery.getStatus();
            this.requestDate = recovery.getRequestDate();
            this.user = new UserDetailsResponseDto(recovery.getUser());
        }
    }
}
