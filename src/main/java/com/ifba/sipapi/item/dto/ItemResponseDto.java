package com.ifba.sipapi.item.dto;


import com.ifba.sipapi.item.domain.item.Item;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponseDto {

    @NotNull
    @Schema(description = "ID do item criado", example = "6357de9b-8581-477d-a38d-87e1c42eb532")
    private UUID itemId;

    public ItemResponseDto(Item item) {
        this.itemId = item.getId();
    }
}
