package com.ifba.sipapi.item.api.service;



import com.ifba.sipapi.item.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface ItemService {
    ItemResponseDto createItem(ItemRequestDto itemRequestDto, String token);
    List<ImageUrlResponseDto> uploadImages(UUID itemId, List<MultipartFile> itemImages);
    void deleteImage(ItemDeleteImageDto itemDeleteImageDto);
    void deleteAllImages(UUID itemId);
    void deleteItem(UUID itemId);
    void editItem(UUID itemId, ItemEditRequestDto itemEditRequestDto);
}
