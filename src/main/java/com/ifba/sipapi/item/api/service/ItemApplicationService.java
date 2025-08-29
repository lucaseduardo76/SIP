package com.ifba.sipapi.item.api.service;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.item.api.dto.ImageUrlResponseDto;
import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.picture.Picture;
import com.ifba.sipapi.item.dto.ItemRequestDto;
import com.ifba.sipapi.item.dto.ItemResponseDto;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import com.ifba.sipapi.item.infra.picture.PictureRepository;
import com.ifba.sipapi.minio.application.service.MinioClient;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.infra.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Log4j2
public class ItemApplicationService implements ItemService {

    private final ItemRepository itemRepository;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final MinioClient minioClient;
    private final PictureRepository pictureRepository;

    @Value("${minio.application.max_images_item}")
    private Integer MAX_IMAGES;

    @Override
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto, String token) {
        log.info("[start] ItemApplicationService - createItem");
        String email = tokenService.getSubject(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "User not found"));
        user.requireAdminRole();

        String itemCode = generateItemCode(itemRequestDto);
        Item item = new Item(itemRequestDto, itemCode);
        log.debug("[finish] ItemApplicationService - createItem");
        log.info("itemCode={}", itemCode);
        return new ItemResponseDto(itemRepository.save(item));
    }

    private String generateItemCode(ItemRequestDto itemRequestDto) {
        Category category = itemRequestDto.getCategory();

        String prefix = category.name().length() > 4 ? category.name().substring(0, 4) : category.name();
        List<String> codes = itemRepository.findItemCodesByCategory(category);

        int maxNumber = codes.stream().map(code -> code.replace(prefix + "-", ""))
                .mapToInt(numStr -> {
                    try {
                        return Integer.parseInt(numStr);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(999);

        int nextNumber = maxNumber + 1;

        return prefix + "-" + nextNumber;
    }

    @Override
    @Transactional
    public List<ImageUrlResponseDto> uploadImages(UUID itemId, List<MultipartFile> itemImages) {
        log.info("[start] ItemApplicationService - uploadImages itemId={}", itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Item not found"));

        validateMaxImagesPerItem(item,itemImages);

        List<Picture> savedPictures = itemImages.stream().map(file -> uploadAndSave(file, item)).toList();
        log.debug("[finish] ItemApplicationService - uploadImages itemId={}, savedImages={}", itemId, savedPictures.size());
        return savedPictures.stream().map(ImageUrlResponseDto::new).toList();
    }

    private void validateMaxImagesPerItem(Item item, List<MultipartFile> itemImages) {
        List<Picture> pictureList = pictureRepository.findByItem(item);
        if(pictureList.size() + itemImages.size() > MAX_IMAGES)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Limite de imagem para um item ultrapassado");

        validateImages(itemImages);
    }

    private void validateImages(List<MultipartFile> itemImages) {
        if (itemImages == null || itemImages.isEmpty())
            throw APIException.build(HttpStatus.BAD_REQUEST, "Nenhuma imagem enviada");

        if (itemImages.size() > MAX_IMAGES)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Você pode enviar no máximo " + MAX_IMAGES + " imagens");
    }

    private Picture uploadAndSave(MultipartFile multipartFile, Item item) {
        String urlImage = minioClient.uploadItemsImage(multipartFile, item);
        Picture picture = new Picture(urlImage, item);
        return pictureRepository.save(picture);
    }
}
