package com.ifba.sipapi.item.api.service;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.item.dto.ItemRecoveryRequestDto;
import com.ifba.sipapi.item.domain.item.Status;
import com.ifba.sipapi.item.dto.*;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.picture.Picture;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import com.ifba.sipapi.item.infra.picture.PictureRepository;
import com.ifba.sipapi.item.infra.recovery.RecoveryRepository;
import com.ifba.sipapi.minio.api.service.MinioClient;
import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.infra.UserRepository;
import com.ifba.sipapi.util.GenerateItemCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    private final RecoveryRepository recoveryRepository;

    @Value("${minio.application.max_images_item}")
    private Integer MAX_IMAGES;

    @Value("${application.time.about-to-donate}")
    private Integer TIME_TO_DONATE;

    @Value("${application.time.donation}")
    private Integer DONATION_TIME;

    @Value("${application.item.max-active-requests}")
    private Integer MAX_ACTIVE_REQUESTS;

    private static final LocalDate OLDEST_ACCEPTABLE_DATE = LocalDate.of(1900, 1, 1);
    private static final LocalDate FUTURE_LIMIT_DATE = LocalDate.of(2999, 12, 31);

    @Override
    public ItemCreatedResponseDto createItem(ItemRequestDto itemRequestDto, String token) {
        log.info("[start] ItemApplicationService - createItem");
        String email = tokenService.getSubject(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "User not found"));
        user.requireAdminRole();

        checkDateIsAfterTodayAndThrowException(itemRequestDto.getFinding_date());

        String itemCode = GenerateItemCode.generateItemCode(itemRequestDto, itemRepository.findItemCodesByCategory(itemRequestDto.getCategory()));
        Item item = new Item(itemRequestDto, itemCode, DONATION_TIME);
        log.debug("[finish] ItemApplicationService - createItem");
        log.info("itemCode={}", itemCode);
        return new ItemCreatedResponseDto(itemRepository.save(item));
    }

    private void checkDateIsAfterTodayAndThrowException(LocalDate findingDate) {
        if(LocalDate.now().isBefore(findingDate))
            throw APIException.build(HttpStatus.BAD_REQUEST, "A data não pode ser futura");
    }


    @Override
    @Transactional
    public List<ImageUrlResponseDto> uploadImages(UUID itemId, List<MultipartFile> itemImages) {
        log.info("[start] ItemApplicationService - uploadImages itemId={}", itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Item not found"));

        validateMaxImagesPerItem(item, itemImages);

        List<Picture> savedPictures = itemImages.stream().map(file -> uploadAndSave(file, item)).toList();
        log.debug("[finish] ItemApplicationService - uploadImages itemId={}, savedImages={}", itemId, savedPictures.size());
        return savedPictures.stream().map(ImageUrlResponseDto::new).toList();
    }

    @Override
    public void deleteImage(ItemDeleteImageDto itemDeleteImageDto) {
        log.info("[start] ItemApplicationService - deleteImage");
        Picture picture = pictureRepository.findByUrl(itemDeleteImageDto.getImageUrl()).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Foto não encontrada"));
        Item item = itemRepository.findById(itemDeleteImageDto.getItemId()).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Item não encontrado"));
        picture.assertBelongsTo(item);
        minioClient.deleteItemImage(itemDeleteImageDto.getImageUrl());
        pictureRepository.delete(picture);
        log.debug("[finish] ItemApplicationService - deleteImage");
    }

    @Override
    public void deleteAllImages(UUID itemId) {
        log.info("[start] ItemApplicationService - deleteAllImages");
        Item item = itemRepository.findById(itemId).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Item não encontrado"));
        item.getPictures().forEach(picture -> minioClient.deleteItemImage(picture.getUrl()));
        item.getPictures().clear();
        itemRepository.save(item);
        log.debug("[finish] ItemApplicationService - deleteAllImages");
    }

    @Override
    public void deleteItem(UUID itemId) {
        log.info("[start] ItemApplicationService - deleteItem");
        this.deleteAllImages(itemId);
        itemRepository.deleteById(itemId);
        log.debug("[finish] ItemApplicationService - deleteItem");
    }

    @Override
    public void editItem(UUID itemId, ItemEditRequestDto itemEditRequestDto) {
        log.info("[start] ItemApplicationService - editItem");
        Item item = itemRepository.findById(itemId).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Item não encontrado"));
        item.validateItemIsAvailableToUpdate();
        item.update(itemEditRequestDto);
        itemRepository.save(item);
        log.debug("[finish] ItemApplicationService - editItem");
    }

    @Override
    public ItemResponseDto getItem(UUID idItem) {
        log.info("[start] ItemApplicationService - getItem");
        Item item = itemRepository.findById(idItem).orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Item não encontrado!"));
        log.debug("[finish] ItemApplicationService - getItem");
        return new ItemResponseDto(item);
    }

    @Override
    public Page<ItemResponseDto> getAllItems(Pageable pageable, ItemFilterDto itemFilterDto) {
        log.info("[start] ItemApplicationService - getAllItems");
        Page<ItemResponseDto> itemList;
        if (itemFilterDto.isEmpty())
            itemList = itemRepository.findAllItemByStatus(Status.DISPONIBLE, pageable).map(ItemResponseDto::new);
        else
            itemList = filterSearch(itemFilterDto, pageable);
        log.debug("[finish] ItemApplicationService - getAllItems");
        return itemList;
    }

    @Override
    public void recoveryItem(ItemRecoveryRequestDto itemRecoveryRequest, String token) {
        log.info("[start] ItemApplicationService - recoveryItem");
        User user = assertEmailBelongsToAndReturnUser(token, itemRecoveryRequest.getEmail());
        Item item = itemRepository.findById(itemRecoveryRequest.getItemId()).orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Item não encontrado"));

        validateRecoveryRequest(user, item);
        Recovery recovery = new Recovery(itemRecoveryRequest, user, item);
        recoveryRepository.save(recovery);
        log.debug("[finish] ItemApplicationService - recoveryItem");
    }

    @Override
    public void recoveryReview(ItemRequestReviewDto itemRequestReviewDto) {
        log.info("[start] ItemApplicationService - recoveryReview");
        Recovery recovery = recoveryRepository.findById(itemRequestReviewDto.getIdRecovery()).orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Solicitação inexistente, verifique o ID!"));
        recovery.processRequestAcceptance(itemRequestReviewDto.getStatusRecovery());
        recoveryRepository.save(recovery);

        if (itemRequestReviewDto.getStatusRecovery().equals(StatusRecovery.APPROVED))
            applyClaimToItem(recovery);

        log.debug("[finish] ItemApplicationService - recoveryReview");
    }

    private void applyClaimToItem(Recovery recovery) {
        rejectAllExcept(recovery);
        Item item = recovery.getItem();
        item.updateStatusToClaimed(recovery);
        itemRepository.save(item);
    }


    private void rejectAllExcept(Recovery recovery) {
        List<Recovery> recoveriesByItem = recoveryRepository.findAllByItem(recovery.getItem());
        recoveriesByItem.stream().filter(other -> !other.equals(recovery)).forEach(this::rejectAndSaveRecovery);
    }

    private void rejectAndSaveRecovery(Recovery recovery) {
        recovery.processRequestAcceptance(StatusRecovery.REFUSED);
        recoveryRepository.save(recovery);
    }

    private void validateRecoveryRequest(User user, Item item) {
        validateUser(user);
        validateItem(item);
        validateDuplicateRequest(user, item);
        validateMaxActiveRequests(user);
    }

    private void validateUser(User user) {
        if (user.getRole() == Role.ROOT) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "O usuário ROOT não deve fazer solicitações de itens");
        }
    }

    private void validateItem(Item item) {
        if (item.getStatus() != Status.DISPONIBLE) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Item não pode mais ser solicitado");
        }
    }

    private void validateDuplicateRequest(User user, Item item) {
        if (recoveryRepository.existsByUserAndItemAndStatusNot(user, item, StatusRecovery.REFUSED)) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Solicitação já efetuada");
        }
    }

    private void validateMaxActiveRequests(User user) {
        long activeRequests = recoveryRepository.countByUserAndStatus(user, StatusRecovery.PENDING);
        if (activeRequests >= MAX_ACTIVE_REQUESTS) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Usuário não pode ter mais de " + MAX_ACTIVE_REQUESTS + " solicitações ativas");
        }
    }

    private User assertEmailBelongsToAndReturnUser(String token, String email) {
        User user = userRepository.findByEmail(tokenService.getSubject(token)).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!user.getEmail().equals(email)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Token não corresponde ao email enviado");
        }
        return user;
    }

    private Page<ItemResponseDto> filterSearch(ItemFilterDto itemFilterDto, Pageable pageable) {
        log.info("[start] ItemApplicationService - filterSearch");
        LocalDate dateFrom = calculateDateFrom(itemFilterDto.getLastDays());
        LocalDate dateCloseToDonation = calculateDateCloseToDonation(itemFilterDto.getAboutToBeDonated());

        Page<ItemResponseDto> result;

        if (itemFilterDto.getCategory() != null && !itemFilterDto.getCategory().isEmpty()) {
            result = itemRepository.findByFilterQuery(pageable, dateFrom, dateCloseToDonation, itemFilterDto.getCategory(), Status.DISPONIBLE).map(ItemResponseDto::new);
        } else {
            result = itemRepository.findByFilterQuery(pageable, dateFrom, dateCloseToDonation, Status.DISPONIBLE).map(ItemResponseDto::new);
        }
        log.debug("[finish] ItemApplicationService - filterSearch");
        return result;
    }

    private LocalDate checkIfDonationFilterIsActive(Boolean aboutToBeDonated) {
        return Boolean.TRUE.equals(aboutToBeDonated) ? LocalDate.now().plusDays(TIME_TO_DONATE) : null;
    }

    private LocalDate calculateDateFrom(Long lastDays) {
        return Optional.ofNullable(lastDays)
                .map(days -> LocalDate.now().minusDays(days))
                .orElse(OLDEST_ACCEPTABLE_DATE);
    }

    private LocalDate calculateDateCloseToDonation(Boolean aboutToBeDonated) {
        LocalDate date = checkIfDonationFilterIsActive(aboutToBeDonated);
        return Optional.ofNullable(date)
                .orElse(FUTURE_LIMIT_DATE);
    }

    private void validateMaxImagesPerItem(Item item, List<MultipartFile> itemImages) {
        List<Picture> pictureList = pictureRepository.findByItem(item);
        if (pictureList.size() + itemImages.size() > MAX_IMAGES)
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
