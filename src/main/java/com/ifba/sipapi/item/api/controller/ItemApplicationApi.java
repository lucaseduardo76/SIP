package com.ifba.sipapi.item.api.controller;

import com.ifba.sipapi.item.api.dto.ImageUrlResponseDto;
import com.ifba.sipapi.item.api.dto.ItemDeleteImageDto;
import com.ifba.sipapi.item.api.service.ItemService;
import com.ifba.sipapi.item.dto.ItemRequestDto;
import com.ifba.sipapi.item.dto.ItemResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@Log4j2
public class ItemApplicationApi implements ItemApi {


    private final ItemService itemService;

    @Override
    public ItemResponseDto createItem(String token, ItemRequestDto itemRequestDto) {
       log.info("[start] ItemApplicationApi - createItem");
       ItemResponseDto itemResponseDto = itemService.createItem(itemRequestDto, token);
       log.debug("[finish] ItemApplicationApi - createItem");
        return itemResponseDto;
    }

    @Override
    public List<ImageUrlResponseDto> uploadImages(UUID itemId, List<MultipartFile> itemImages) {
        log.info("[start] ItemApplicationApi - uploadImages");
        List<ImageUrlResponseDto> imageList =  itemService.uploadImages(itemId, itemImages);
        log.debug("[finish] ItemApplicationApi - uploadImages");
        return imageList;
    }

    @Override
    public void deleteImage(ItemDeleteImageDto itemDeleteImageDto) {
        log.info("[start] ItemApplicationApi - deleteImage");
        itemService.deleteImage(itemDeleteImageDto);
        log.debug("[finish] ItemApplicationApi - deleteImage");
    }

    @Override
    public void deleteAllImages(UUID itemId) {
        log.info("[start] ItemApplicationApi - deleteAllImages");
        itemService.deleteAllImages(itemId);
        log.debug("[finish] ItemApplicationApi - deleteAllImages");
    }
}
