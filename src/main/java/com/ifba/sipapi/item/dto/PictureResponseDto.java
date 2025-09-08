package com.ifba.sipapi.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PictureResponseDto {

    @Schema(example = "ff2ce525-98f5-418e-84b0-b562ccbc4dba")
    private UUID id;

    @Schema(example = "http://192.168.1.2:9000/itemsimage/ELEC-1000_966b0de1-c2b8-434c-a1db-f18b94df3e25_item.jpg")
    private String url;


}
