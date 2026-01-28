package com.ifba.sipapi.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URI;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class ItemDeleteImageDto {

    @Schema(example = "ff2ce525-98f5-418e-84b0-b562ccbc4dba")
    @NotNull(message = "Id do item não pode ser nulo")
    private UUID itemId;

    @Schema(example = "http://192.168.1.2:9000/itemsimage/ELEC-1000_966b0de1-c2b8-434c-a1db-f18b94df3e25_item.jpg")
    @NotBlank(message = "Url da imagem não pode ser nula")
    private String imageUrl;

    public String getUrlSemHost() {
        try {
            URI uri = new URI(imageUrl);
            return uri.getPath();
        } catch (Exception e) {
            throw new RuntimeException("URL inválida", e);
        }
    }
}
