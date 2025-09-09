package com.ifba.sipapi.item.api.controller;

import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.dto.*;
import com.ifba.sipapi.item.api.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ItemCreatedResponseDto createItem(String token, ItemRequestDto itemRequestDto) {
       log.info("[start] ItemApplicationApi - createItem");
       ItemCreatedResponseDto itemCreatedResponseDto = itemService.createItem(itemRequestDto, token);
       log.debug("[finish] ItemApplicationApi - createItem");
        return itemCreatedResponseDto;
    }

    @Override
    public ItemResponseDto getItem(UUID idItem) {
        log.info("[start] ItemApplicationApi - getItem");
        ItemResponseDto item = itemService.getItem(idItem);
        log.debug("[finish] ItemApplicationApi - getItem");
        return item;
    }

    @Override
    public Page<ItemResponseDto> getAllItem(Pageable pageable, ItemFilterDto itemFilterDto) {
        log.info("[start] ItemApplicationApi - getAllItem");
        Page<ItemResponseDto> itemList = itemService.getAllItems(pageable, itemFilterDto);
        log.debug("[finish] ItemApplicationApi - getAllItem");
        return itemList;
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

    @Override
    public void deleteItem(UUID itemId) {
        log.info("[start] ItemApplicationApi - deleteItem");
        itemService.deleteItem(itemId);
        log.debug("[finish] ItemApplicationApi - deleteItem");
    }

    @Override
    public void editItem(UUID itemId, ItemEditRequestDto itemEditRequestDto) {
        log.info("[start] ItemApplicationApi - editItem");
        itemService.editItem(itemId, itemEditRequestDto);
        log.debug("[finish] ItemApplicationApi - editItem");
    }
}
