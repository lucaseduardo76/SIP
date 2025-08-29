package com.ifba.sipapi.item.api.controller;


import com.ifba.sipapi.docs.swagger.ItemsAPIDocs;
import com.ifba.sipapi.item.api.dto.ImageUrlResponseDto;
import com.ifba.sipapi.item.dto.ItemRequestDto;
import com.ifba.sipapi.item.dto.ItemResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/items")
@Tag(name = "ItemApi", description = "Controle responsavel pelas requisições do items.")
public interface ItemApi {


    @ItemsAPIDocs.CreateItem
    @PostMapping("/admin/create")
    @ResponseStatus(HttpStatus.CREATED)
    ItemResponseDto createItem(
            @RequestHeader(name = "Authorization", required = true) String token,
            @RequestBody ItemRequestDto itemRequestDto);


    @ItemsAPIDocs.UpdateImagesItem
    @PostMapping(value = "/admin/{itemId}/images")
    @ResponseStatus(HttpStatus.OK)
    List<ImageUrlResponseDto> uploadImages(
            @PathVariable UUID itemId,
            @RequestPart("itemImages") List<MultipartFile> itemImages
    );

}
