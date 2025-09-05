package com.ifba.sipapi.item.dto;


import com.ifba.sipapi.item.domain.item.Area;
import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.DayPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ItemEditRequestDto {

    @Schema(description = "Descrição do item", example = "Chave de carro vermelha encontrada próximo à biblioteca")
    private String description;

    @Schema(description = "Cor do item", example = "Vermelho")
    private String color;

    @Schema(description = "Data em que o item foi encontrado", example = "2025-08-27")
    private LocalDate finding_date;

    @Schema(description = "Período do dia em que o item foi encontrado", example = "MORNING")
    private DayPeriod day_period;

    @Schema(description = "Categoria do item", example = "ELECTRONIC")
    private Category category;

    @Schema(description = "Área onde o item foi encontrado", example = "LIBRARY")
    private Area area;

}
