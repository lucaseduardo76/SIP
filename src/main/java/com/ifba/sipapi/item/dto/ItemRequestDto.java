package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.item.domain.item.Area;
import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.DayPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {

    @NotBlank(message = "A descrição é obrigatória")
    @Schema(description = "Descrição do item", example = "Chave de carro vermelha encontrada próximo à biblioteca")
    private String description;

    @NotNull(message = "A data em que o item foi encontrado é obrigatória")
    @Schema(description = "Data em que o item foi encontrado", example = "2025-08-27")
    private LocalDate finding_date;

    @NotNull(message = "O período do dia é obrigatório")
    @Schema(description = "Período do dia em que o item foi encontrado", example = "MORNING")
    private DayPeriod day_period;

    @NotNull(message = "A categoria do item é obrigatória")
    @Schema(description = "Categoria do item", example = "ELECTRONIC")
    private Category category;

    @NotNull(message = "A área do item é obrigatória")
    @Schema(description = "Área onde o item foi encontrado", example = "LIBRARY")
    private Area area;
}
