package com.ifba.sipapi.item.api.service;



import com.ifba.sipapi.item.dto.ItemRecoveryRequestDto;
import com.ifba.sipapi.item.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.UUID;

@Service
public interface ItemService {
    ItemCreatedResponseDto createItem(ItemRequestDto itemRequestDto, String token);
    List<ImageUrlResponseDto> uploadImages(UUID itemId, List<MultipartFile> itemImages);
    void deleteImage(ItemDeleteImageDto itemDeleteImageDto);
    void deleteAllImages(UUID itemId);
    void deleteItem(UUID itemId);
    void editItem(UUID itemId, ItemEditRequestDto itemEditRequestDto);
    ItemResponseDto getItem(UUID idItem);
    Page<ItemResponseDto> getAllItems(Pageable pageable, ItemFilterDto itemFilterDto);
    void recoveryItem(ItemRecoveryRequestDto itemRecoveryRequest, String token);
    void recoveryReview(ItemRequestReviewDto itemRequestReviewDto);
    List<RecoveryResponse> getAllRecoveries();
    RecoveryResponseByItem getRecoveriesByItem(UUID idItem);
    RecoveryResponseByUser getRecoveriesByUser(UUID idUser);
}
