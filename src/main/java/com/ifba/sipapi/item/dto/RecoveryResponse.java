package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.user.dto.UserDetailsResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class RecoveryResponse {

    private final UUID id;
    private final String description;
    private final StatusRecovery status;
    private final LocalDateTime requestDate;
    private final ItemResponseDto item;
    private final UserDetailsResponseDto user;

    public RecoveryResponse(Recovery recovery) {
        this.id = recovery.getId();
        this.description = recovery.getDescription();
        this.status = recovery.getStatus();
        this.requestDate = recovery.getRequestDate();
        this.item = new ItemResponseDto(recovery.getItem());
        this.user = new UserDetailsResponseDto(recovery.getUser());
    }
}
