package com.ifba.sipapi.item.api.dto;

import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.picture.Picture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImageUrlResponseDto {

    private UUID itemId;
    private String imgUrl;

    public ImageUrlResponseDto(Picture picture) {
        this.itemId = picture.getItem().getId();
        this.imgUrl = picture.getUrl();
    }

}
