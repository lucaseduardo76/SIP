package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class ItemFilterDto {

    @Schema(description = "Quantidade de dias para filtrar itens criados recentemente",
            example = "7")
    private Long lastDays;

    @Schema(description = "Categorias para filtrar os itens",
            example = "BOOK")
    private List<Category> category;

    @Schema(description = "Indica se o item está prestes a ser doado",
            example = "true")
    private Boolean aboutToBeDonated;

    @Schema(description = "Data inicial para filtrar os itens pelo período",
            example = "2025-11-01")
    private LocalDate startPeriod;

    @Schema(description = "Data final para filtrar os itens pelo período",
            example = "2025-11-06")
    private LocalDate endPeriod;

    @Schema(description = "Descrição do item para pesquisa",
            example = "Livro azul")
    private String itemName;

    @Schema(description = "Descrição do item para pesquisa",
            example = "DISPONIBLE")
    private Status status;

    public boolean isEmpty() {
        return lastDays == null && category == null && aboutToBeDonated == null && startPeriod == null && endPeriod == null && itemName == null && status == null;
    }
}
