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
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Log4j2
public class ItemApplicationService implements ItemService {

    private final ItemRepository itemRepository;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final MinioClient minioClient;
    private final PictureRepository pictureRepository;

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
    public List<ImageUrlResponseDto> uploadImages(UUID itemId, List<MultipartFile> itemImages) {
        log.info("[start] ItemApplicationService - uploadImages");
        Item item = itemRepository.findById(itemId).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Item not found"));
        itemImages.forEach(multipartFile -> {
            String urlImage = minioClient.uploadItemsImage(multipartFile, item);
            Picture picture = new Picture(urlImage, item);
            pictureRepository.save(picture);
        });
        log.debug("[finish] ItemApplicationService - uploadImages");
        return pictureRepository.findByItem(item).stream().map(ImageUrlResponseDto::new).collect(Collectors.toList());
    }
}
