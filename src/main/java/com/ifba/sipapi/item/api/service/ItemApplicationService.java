package com.ifba.sipapi.item.api.service;

import com.ifba.sipapi.agenda.domain.AvailableDay;
import com.ifba.sipapi.agenda.domain.AvailableTime;
import com.ifba.sipapi.agenda.domain.DayOfWeekEnum;
import com.ifba.sipapi.agenda.repository.AvailableDayRepository;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.item.Status;
import com.ifba.sipapi.item.domain.picture.Picture;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.item.dto.*;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import com.ifba.sipapi.item.infra.picture.PictureRepository;
import com.ifba.sipapi.item.infra.recovery.RecoveryRepository;
import com.ifba.sipapi.minio.api.service.MinioClient;
import com.ifba.sipapi.notification.application.NotificationToItemService;
import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.infra.UserRepository;
import com.ifba.sipapi.util.GenerateItemCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
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
    private final RecoveryRepository recoveryRepository;
    private final AvailableDayRepository availableDayRepository;
    private final NotificationToItemService  notificationToItemService;

    @Value("${minio.application.max_images_item}")
    private Integer MAX_IMAGES;

    @Value("${application.time.about-to-donate}")
    private Integer TIME_TO_DONATE;

    @Value("${application.time.donation}")
    private Integer DONATION_TIME;

    @Value("${application.item.max-active-requests}")
    private Integer MAX_ACTIVE_REQUESTS;

    private static final LocalDate FUTURE_LIMIT_DATE = LocalDate.of(2999, 12, 31);

    @Override
    public ItemCreatedResponseDto createItem(ItemRequestDto itemRequestDto, String token) {
        log.info("[start] ItemApplicationService - createItem");
        String email = tokenService.getSubject(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "User not found"));
        user.requireAdminRole();

        checkDateIsAfterTodayAndThrowException(itemRequestDto.getFinding_date());

        String itemCode = GenerateItemCode.generateItemCode(itemRequestDto, itemRepository.findItemCodesByCategory(itemRequestDto.getCategory()));
        Item itemSaved = itemRepository.save(new Item(itemRequestDto, itemCode, DONATION_TIME));

        notificationToItemService.itemCreated(itemSaved);
        log.info("itemCode={}", itemCode);
        return new ItemCreatedResponseDto(itemSaved);
    }

    private void checkDateIsAfterTodayAndThrowException(LocalDate findingDate) {
        if (LocalDate.now().isBefore(findingDate))
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
        validateAgenda(itemRecoveryRequest.getDateTime());
        Recovery recovery = new Recovery(itemRecoveryRequest, user, item);
        recoveryRepository.save(recovery);

        notificationToItemService.requestCreated(item, user);
        log.debug("[finish] ItemApplicationService - recoveryItem");
    }

    private void validateAgenda(LocalDateTime dateTime) {
        log.info("[start] ItemApplicationService - validateAgenda");
        AvailableDay availableDay = availableDayRepository.findAllByAvailableDay(DayOfWeekEnum.valueOf(dateTime.getDayOfWeek().name())).orElseThrow(
                () -> APIException.build(HttpStatus.BAD_REQUEST, "Dia escolhido não está disponivel"));

        LocalTime time = LocalTime.of(dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
        AtomicReference<AvailableTime> availableTime = new AtomicReference<>();

        availableDay.getAvailableTimeList().forEach(dbTime -> {
            if ( (time.isAfter(dbTime.getStartTime()) || time.equals(dbTime.getStartTime())) &&
                    (time.isBefore(dbTime.getEndTime())) || time.equals(dbTime.getEndTime()))
                availableTime.set(dbTime);
        });

        if (availableTime.get() == null)
            throw APIException.build(HttpStatus.BAD_REQUEST, "O horário selecionado não está disponível");

        log.debug("[finish] ItemApplicationService - validateAgenda");
    }

    @Override
    public void recoveryReview(ItemRequestReviewDto itemRequestReviewDto) {
        log.info("[start] ItemApplicationService - recoveryReview");
        Recovery recovery = recoveryRepository.findById(itemRequestReviewDto.getIdRecovery()).orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Solicitação inexistente, verifique o ID!"));
        recovery.processRequestAcceptance(itemRequestReviewDto.getStatusRecovery());
        recoveryRepository.save(recovery);

        if (itemRequestReviewDto.getStatusRecovery().equals(StatusRecovery.APPROVED)) {
            applyClaimToItem(recovery);
            notificationToItemService.sendNotificationToRequester(recovery);
        }else {
            notificationToItemService.recoveryRejected(recovery, false);
        }

        log.debug("[finish] ItemApplicationService - recoveryReview");
    }

    @Override
    public Page<RecoveryResponse> getAllRecoveries(Pageable pageable, StatusRecovery statusRecovery) {
        log.info("[start] ItemApplicationService - getAllRecoveries");

        Page<Recovery> recoveryPage;

        if (statusRecovery == null)
            recoveryPage = recoveryRepository.findAll(pageable);
        else
            recoveryPage = recoveryRepository.findAllByStatus(statusRecovery, pageable);

        Page<RecoveryResponse> recoveryResponsePage = recoveryPage.map(RecoveryResponse::new);

        log.debug("[finish] ItemApplicationService - getAllRecoveries");
        return recoveryResponsePage;
    }

    @Override
    public RecoveryResponseByItem getRecoveriesByItem(UUID idItem) {
        log.info("[start] ItemApplicationService - getRecoveriesByItem");
        Item item = itemRepository.findById(idItem).orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Item não encontrado"));
        RecoveryResponseByItem recoveryList = new RecoveryResponseByItem(recoveryRepository.findAllByItem(item));
        log.debug("[finish] ItemApplicationService - getRecoveriesByItem");
        return recoveryList;
    }

    @Override
    public RecoveryResponseByUser getRecoveriesByUser(UUID idUser) {
        log.info("[start] ItemApplicationService - getRecoveriesByUser");
        User user = userRepository.findById(idUser).orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));
        RecoveryResponseByUser recoveryList = new RecoveryResponseByUser(recoveryRepository.findAllByUser(user), user);
        log.debug("[finish] ItemApplicationService - getRecoveriesByUser");
        return recoveryList;
    }

    @Override
    public Page<RecoveryResponseByUser> getSelfRecoveriesByUser(String token, Category category, Pageable pageable, String email, StatusRecovery status) {
        log.info("[start] ItemApplicationService - getSelfRecoveriesByUser");
        User user = assertEmailBelongsToAndReturnUser(token, email);
        List<Recovery> recoveryPage;

        if (status != null)
            recoveryPage = recoveryRepository.findAllByUserAndStatus(user, status);
        else
            recoveryPage = recoveryRepository.findAllByUser(user);

        if(category != null)
            recoveryPage = recoveryPage.stream().filter(r -> r.getItem().getCategory().equals(category)).collect(Collectors.toList());

        RecoveryResponseByUser response = new RecoveryResponseByUser(recoveryPage, user);
        log.debug("[finish] ItemApplicationService - getSelfRecoveriesByUser");
        return new PageImpl<>(List.of(response), pageable, recoveryPage.size());
    }

    @Override
    public void refreshItemToCharity() {
        log.info("[start] ItemApplicationService - refreshItemToCharity");
        itemRepository.findByDonationDateLessThanEqualAndStatus(LocalDate.now(), Status.DISPONIBLE).forEach(item -> {
            recoveryRepository.findAllByItem(item).forEach(this::rejectAndSaveRecovery);
            item.setToCharity();
            notificationToItemService.newItemOnCharity(item);
        });
        log.debug("[finish] ItemApplicationService - refreshItemToCharity");
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
        notificationToItemService.recoveryRejected(recovery, true);
    }

    private void validateRecoveryRequest(User user, Item item) {
        validateUser(user);
        validateItem(item);
        validateDuplicateRequest(user, item);
        validateMaxActiveRequests(user);
    }

    private void validateUser(User user) {
        if (user.getRole() == Role.ROOT)
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "O usuário ROOT não deve fazer solicitações de itens");
    }

    private void validateItem(Item item) {
        if (item.getStatus() != Status.DISPONIBLE)
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Item não pode mais ser solicitado");
    }

    private void validateDuplicateRequest(User user, Item item) {
        if (recoveryRepository.existsByUserAndItemAndStatusNot(user, item, StatusRecovery.REFUSED))
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Solicitação já efetuada");
    }

    private void validateMaxActiveRequests(User user) {
        long activeRequests = recoveryRepository.countByUserAndStatus(user, StatusRecovery.PENDING);
        if (activeRequests >= MAX_ACTIVE_REQUESTS)
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Usuário não pode ter mais de " + MAX_ACTIVE_REQUESTS + " solicitações ativas");
    }

    private User assertEmailBelongsToAndReturnUser(String token, String email) {
        User user = userRepository.findByEmail(tokenService.getSubject(token)).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        if (!user.getEmail().equals(email))
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Token não corresponde ao email enviado");

        return user;
    }

    private Page<ItemResponseDto> filterSearch(ItemFilterDto itemFilterDto, Pageable pageable) {
        log.info("[start] ItemApplicationService - filterSearch");

        LocalDate dateCloseToDonation = calculateDateCloseToDonation(itemFilterDto.getAboutToBeDonated());
        validateSearchPeriod(itemFilterDto.getStartPeriod(), itemFilterDto.getEndPeriod());

        String itemNamePattern = (itemFilterDto.getItemName() != null && !itemFilterDto.getItemName().isBlank())
                ? "%" + itemFilterDto.getItemName() + "%"
                : null;

        List<Category> categories = (itemFilterDto.getCategory() != null && !itemFilterDto.getCategory().isEmpty())
                ? itemFilterDto.getCategory()
                : null;

        Status status = itemFilterDto.getStatus() != null ? itemFilterDto.getStatus() : null;

        Page<ItemResponseDto> result = itemRepository
                .findByFilterQuery(pageable, dateCloseToDonation, categories, status, itemNamePattern, itemFilterDto.getStartPeriod(), itemFilterDto.getEndPeriod())
                .map(ItemResponseDto::new);

        log.debug("[finish] ItemApplicationService - filterSearch");
        return result;
    }


    private void validateSearchPeriod(LocalDate startPeriod, LocalDate endPeriod) {
        if (startPeriod == null || endPeriod == null)
            return;

        if (startPeriod.isAfter(endPeriod))
            throw APIException.build(HttpStatus.BAD_REQUEST, "Data inicial do periodo de busca é posterior a data final, corriga e tente novamente");
    }

    private LocalDate checkIfDonationFilterIsActive(Boolean aboutToBeDonated) {
        return aboutToBeDonated != null &&  aboutToBeDonated ? LocalDate.now().plusDays(TIME_TO_DONATE) : null;
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
