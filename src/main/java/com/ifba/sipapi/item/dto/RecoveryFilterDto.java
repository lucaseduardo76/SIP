package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class RecoveryFilterDto {
    private List<Category> category;
    private String email;
    private StatusRecovery status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String itemName;
}
