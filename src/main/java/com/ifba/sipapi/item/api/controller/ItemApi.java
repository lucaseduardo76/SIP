package com.ifba.sipapi.item.api.controller;


import com.ifba.sipapi.docs.swagger.ItemsAPIDocs;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.item.dto.ItemRecoveryRequestDto;
import com.ifba.sipapi.item.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    ItemCreatedResponseDto createItem(
            @RequestHeader(name = "Authorization", required = true) String token,
            @RequestBody @Valid  ItemRequestDto itemRequestDto);

    @ItemsAPIDocs.GetItem
    @GetMapping("/{idItem}")
    @ResponseStatus(HttpStatus.OK)
    ItemResponseDto getItem(@PathVariable UUID idItem);

    @ItemsAPIDocs.GetAllItems
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    Page<ItemResponseDto> getAllItem(Pageable pageable,
                                     @ModelAttribute ItemFilterDto itemFilterDto);

    @ItemsAPIDocs.UpdateImagesItem
    @PostMapping(value = "/admin/images/{itemId}")
    @ResponseStatus(HttpStatus.CREATED)
    List<ImageUrlResponseDto> uploadImages(
            @PathVariable UUID itemId,
            @RequestPart("itemImages") List<MultipartFile> itemImages
    );

    @ItemsAPIDocs.DeleteImageItem
    @DeleteMapping(value = "/admin/image/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteImage(
            @RequestBody @Valid ItemDeleteImageDto itemDeleteImageDto
    );

    @ItemsAPIDocs.DeleteAllImagesItem
    @DeleteMapping(value = "/admin/image/delete-all/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAllImages(
            @PathVariable UUID itemId
    );

    @ItemsAPIDocs.DeleteItem
    @DeleteMapping(value = "/root/delete/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteItem(
            @PathVariable UUID itemId
    );

    @ItemsAPIDocs.EditItem
    @PutMapping(value = "/admin/edit/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editItem(
            @PathVariable UUID itemId,
            @RequestBody ItemEditRequestDto ItemEditRequestDto
    );

    @ItemsAPIDocs.RecoveryItem
    @PostMapping(value = "/recovery/withdrawal-requests")
    @ResponseStatus(HttpStatus.CREATED)
    void recoveryItem(
            @RequestHeader(name = "Authorization", required = true) String token,
            @RequestBody @Valid ItemRecoveryRequestDto itemRecoveryRequest
    );

    @ItemsAPIDocs.RecoveryReview
    @PostMapping(value = "/admin/recovery/withdrawal-requests/review")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void recoveryReview(
            @RequestBody @Valid ItemRequestReviewDto itemRequestReviewDto
    );

    @ItemsAPIDocs.GetAllRecoveries
    @GetMapping(value = "/admin/recovery")
    @ResponseStatus(HttpStatus.OK)
    Page<RecoveryResponse> getAllRecovery(
            Pageable pageable,
            @RequestParam(required = false) StatusRecovery status);

    @ItemsAPIDocs.GetAllRecoveries
    @GetMapping(value = "/admin/recovery-by-item/{idItem}")
    @ResponseStatus(HttpStatus.OK)
    RecoveryResponseByItem getRecoveryByItem(@PathVariable  UUID idItem);

    @ItemsAPIDocs.GetAllRecoveries
    @GetMapping(value = "/admin/recovery-by-user/{idUser}")
    @ResponseStatus(HttpStatus.OK)
    RecoveryResponseByUser getRecoveryByUser(@PathVariable UUID idUser);

    @ItemsAPIDocs.GetAllSelfUserRecoveries
    @GetMapping(value = "/recovery-self")
    @ResponseStatus(HttpStatus.OK)
    RecoveryResponseByUser getAllSelfUserRecoveries(@RequestHeader(name = "Authorization", required = true) String token,
                                                    @RequestParam String email,
                                                    @RequestParam(required = false) StatusRecovery status
    );
}
