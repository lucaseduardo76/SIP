package com.ifba.sipapi.item.api.controller;


import com.ifba.sipapi.docs.swagger.ItemsAPIDocs;
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
            @RequestBody ItemRequestDto itemRequestDto);

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
    @ResponseStatus(HttpStatus.OK)
    List<ImageUrlResponseDto> uploadImages(
            @PathVariable UUID itemId,
            @RequestPart("itemImages") List<MultipartFile> itemImages
    );

    @ItemsAPIDocs.DeleteImageItem
    @DeleteMapping(value = "/admin/image/delete")
    @ResponseStatus(HttpStatus.OK)
    void deleteImage(
            @RequestBody @Valid ItemDeleteImageDto itemDeleteImageDto
    );

    @ItemsAPIDocs.DeleteAllImagesItem
    @DeleteMapping(value = "/admin/image/delete-all/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    void deleteAllImages(
            @PathVariable UUID itemId
    );

    @ItemsAPIDocs.DeleteItem
    @DeleteMapping(value = "/admin/delete/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    void deleteItem(
            @PathVariable UUID itemId
    );

    @ItemsAPIDocs.EditItem
    @PutMapping(value = "/admin/edit/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    void editItem(
            @PathVariable UUID itemId,
            @RequestBody ItemEditRequestDto ItemEditRequestDto
    );

    @ItemsAPIDocs.RecoveryItem
    @PostMapping(value = "/recovery")
    @ResponseStatus(HttpStatus.CREATED)
    void recoveryItem(
            @RequestHeader(name = "Authorization", required = true) String token,
            @RequestBody @Valid ItemRecoveryRequestDto itemRecoveryRequest
    );

}
