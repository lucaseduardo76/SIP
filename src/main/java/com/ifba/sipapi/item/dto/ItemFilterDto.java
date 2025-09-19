package com.ifba.sipapi.item.dto;

import com.ifba.sipapi.item.domain.item.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

    public boolean isEmpty() {
        return lastDays == null && category == null && aboutToBeDonated == null;
    }
}
