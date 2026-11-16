package com.ifba.sipapi.item.api.service;

import com.ifba.sipapi.agenda.domain.AvailableDay;
import com.ifba.sipapi.agenda.domain.AvailableTime;
import com.ifba.sipapi.agenda.domain.DayOfWeekEnum;
import com.ifba.sipapi.agenda.repository.AvailableDayRepository;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.item.domain.item.Area;
import com.ifba.sipapi.item.domain.item.Category;
import com.ifba.sipapi.item.domain.item.DayPeriod;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.item.Status;
import com.ifba.sipapi.item.domain.picture.Picture;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.item.dto.ImageUrlResponseDto;
import com.ifba.sipapi.item.dto.ItemCreatedResponseDto;
import com.ifba.sipapi.item.dto.ItemDeleteImageDto;
import com.ifba.sipapi.item.dto.ItemEditRequestDto;
import com.ifba.sipapi.item.dto.ItemFilterDto;
import com.ifba.sipapi.item.dto.ItemRecoveryRequestDto;
import com.ifba.sipapi.item.dto.ItemRequestDto;
import com.ifba.sipapi.item.dto.ItemRequestReviewDto;
import com.ifba.sipapi.item.dto.ItemResponseDto;
import com.ifba.sipapi.item.dto.RecoveryFilterDto;
import com.ifba.sipapi.item.dto.RecoveryResponse;
import com.ifba.sipapi.item.dto.RecoveryResponseByItem;
import com.ifba.sipapi.item.dto.RecoveryResponseByUser;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import com.ifba.sipapi.item.infra.picture.PictureRepository;
import com.ifba.sipapi.item.infra.recovery.RecoveryRepository;
import com.ifba.sipapi.minio.api.service.MinioClient;
import com.ifba.sipapi.notification.application.NotificationToItemService;
import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemApplicationService")
class ItemApplicationServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private TokenService tokenService;
    @Mock private UserRepository userRepository;
    @Mock private MinioClient minioClient;
    @Mock private PictureRepository pictureRepository;
    @Mock private RecoveryRepository recoveryRepository;
    @Mock private AvailableDayRepository availableDayRepository;
    @Mock private NotificationToItemService notificationToItemService;

    @InjectMocks private ItemApplicationService service;

    private static final int MAX_IMAGES = 5;
    private static final int TIME_TO_DONATE = 7;
    private static final int DONATION_TIME = 30;
    private static final int MAX_ACTIVE_REQUESTS = 3;
    private static final String TOKEN = "Bearer test-token";
    private static final String EMAIL = "user@ifba.edu.br";

    private UUID itemId;
    private UUID userId;
    private UUID recoveryId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "MAX_IMAGES", MAX_IMAGES);
        ReflectionTestUtils.setField(service, "TIME_TO_DONATE", TIME_TO_DONATE);
        ReflectionTestUtils.setField(service, "DONATION_TIME", DONATION_TIME);
        ReflectionTestUtils.setField(service, "MAX_ACTIVE_REQUESTS", MAX_ACTIVE_REQUESTS);
        ReflectionTestUtils.setField(service, "RESERVATION_EXPIRATION_DAYS", 2);
        ReflectionTestUtils.setField(service, "PICTURES_RETENTION_DAYS", 45);
        itemId = UUID.randomUUID();
        userId = UUID.randomUUID();
        recoveryId = UUID.randomUUID();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Item buildItem(Status status) {
        Item item = new Item(
            itemId, "Livro de teste",
            LocalDate.now().minusDays(5), LocalDate.now().plusDays(25),
            status, DayPeriod.MORNING, Category.BOOK, Area.LIBRARY,
            null, "BOOK-1000",
            new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>()
        );
        ReflectionTestUtils.setField(item, "createdAt", LocalDateTime.now().minusHours(1));
        return item;
    }

    private User buildUser(Role role) {
        return new User(
            userId, "Usuario Teste", "12345678900", EMAIL, role,
            StatusMember.ACTIVE, 0, "senha123", "71999999999",
            "CODE123", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
    }

    private Recovery buildRecovery(Item item, User user, StatusRecovery status) {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2)
            .withHour(9).withMinute(0).withSecond(0).withNano(0);
        ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(
            item.getId(), user.getEmail(), "Preciso do item de volta", futureDate
        );
        Recovery recovery = new Recovery(dto, user, item);
        ReflectionTestUtils.setField(recovery, "id", recoveryId);
        if (status != StatusRecovery.PENDING) {
            ReflectionTestUtils.setField(recovery, "status", status);
        }
        return recovery;
    }

    private AvailableDay buildAvailableDay(DayOfWeekEnum day, LocalTime start, LocalTime end) {
        AvailableTime slot = new AvailableTime(UUID.randomUUID(), start, end, new ArrayList<>());
        return new AvailableDay(UUID.randomUUID(), day, new ArrayList<>(List.of(slot)));
    }

    /** Returns next Monday in the future at 09:00, guaranteed to be after now. */
    private LocalDateTime nextMonday() {
        LocalDateTime next = LocalDateTime.now().plusDays(1)
            .withHour(9).withMinute(0).withSecond(0).withNano(0);
        while (next.getDayOfWeek() != DayOfWeek.MONDAY) {
            next = next.plusDays(1);
        }
        return next;
    }

    // ── createItem ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createItem")
    class CreateItem {

        @Test
        @DisplayName("admin com data passada retorna ItemCreatedResponseDto")
        void adminUser_validDate_returnsDto() {
            User admin = buildUser(Role.ADMIN);
            ItemRequestDto dto = new ItemRequestDto(
                "Livro azul", LocalDate.now().minusDays(1),
                DayPeriod.MORNING, Category.BOOK, Area.LIBRARY
            );
            Item saved = buildItem(Status.DISPONIBLE);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(admin));
            when(itemRepository.findItemCodesByCategory(Category.BOOK)).thenReturn(List.of());
            when(itemRepository.save(any(Item.class))).thenReturn(saved);

            ItemCreatedResponseDto result = service.createItem(dto, TOKEN);

            assertThat(result.getItemId()).isEqualTo(itemId);
            verify(notificationToItemService).itemCreated(saved);
        }

        @Test
        @DisplayName("usuário não encontrado lança NOT_FOUND")
        void userNotFound_throwsNotFound() {
            ItemRequestDto dto = new ItemRequestDto(
                "Livro", LocalDate.now().minusDays(1), DayPeriod.MORNING, Category.BOOK, Area.LIBRARY
            );
            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.NOT_FOUND));
        }

        @Test
        @DisplayName("usuário sem role ADMIN lança FORBIDDEN")
        void nonAdminUser_throwsForbidden() {
            User common = buildUser(Role.COMMON);
            ItemRequestDto dto = new ItemRequestDto(
                "Livro", LocalDate.now().minusDays(1), DayPeriod.MORNING, Category.BOOK, Area.LIBRARY
            );
            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(common));

            assertThatThrownBy(() -> service.createItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.FORBIDDEN));
        }

        @Test
        @DisplayName("data de encontro no futuro lança BAD_REQUEST")
        void futureFindingDate_throwsBadRequest() {
            User admin = buildUser(Role.ADMIN);
            ItemRequestDto dto = new ItemRequestDto(
                "Livro", LocalDate.now().plusDays(1), DayPeriod.MORNING, Category.BOOK, Area.LIBRARY
            );
            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(admin));

            assertThatThrownBy(() -> service.createItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── uploadImages ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("uploadImages")
    class UploadImages {

        @Test
        @DisplayName("upload válido retorna lista de URLs")
        void success_returnsImageUrlList() {
            Item item = buildItem(Status.DISPONIBLE);
            MultipartFile file = mock(MultipartFile.class);
            Picture picture = new Picture(UUID.randomUUID(), "http://localhost/img/test.jpg", item);

            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(pictureRepository.findByItem(item)).thenReturn(new ArrayList<>());
            when(minioClient.uploadItemsImage(file, item)).thenReturn("http://localhost/img/test.jpg");
            when(pictureRepository.save(any(Picture.class))).thenReturn(picture);

            List<ImageUrlResponseDto> result = service.uploadImages(itemId, "edit", List.of(file));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getImgUrl()).isEqualTo("http://localhost/img/test.jpg");
        }

        @Test
        @DisplayName("item não encontrado lança NOT_FOUND")
        void itemNotFound_throwsNotFound() {
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadImages(itemId, "edit", List.of(mock(MultipartFile.class))))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.NOT_FOUND));
        }

        @Test
        @DisplayName("quantidade total de imagens ultrapassa limite lança BAD_REQUEST")
        void existingPlusNewExceedsMax_throwsBadRequest() {
            Item item = buildItem(Status.DISPONIBLE);
            List<Picture> existing = new ArrayList<>();
            for (int i = 0; i < MAX_IMAGES; i++) {
                existing.add(new Picture(UUID.randomUUID(), "url" + i, item));
            }
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(pictureRepository.findByItem(item)).thenReturn(existing);

            assertThatThrownBy(() -> service.uploadImages(itemId, "edit", List.of(mock(MultipartFile.class))))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("lista de arquivos vazia lança BAD_REQUEST")
        void emptyFileList_throwsBadRequest() {
            Item item = buildItem(Status.DISPONIBLE);
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(pictureRepository.findByItem(item)).thenReturn(new ArrayList<>());

            assertThatThrownBy(() -> service.uploadImages(itemId, "edit", new ArrayList<>()))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── deleteImage ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteImage")
    class DeleteImage {

        @Test
        @DisplayName("imagem existente é removida do Minio e do repositório")
        void success_deletesPictureFromMinioAndRepository() {
            Item item = buildItem(Status.DISPONIBLE);
            Picture picture = new Picture(UUID.randomUUID(), "/itemsimage/test.jpg", item);
            item.getPictures().add(picture);
            ItemDeleteImageDto dto = new ItemDeleteImageDto(itemId, "http://localhost:9000/itemsimage/test.jpg");

            when(pictureRepository.findByUrl("/itemsimage/test.jpg")).thenReturn(Optional.of(picture));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

            service.deleteImage(dto);

            verify(minioClient).deleteItemImage("/itemsimage/test.jpg");
            verify(itemRepository).save(item);
            assertThat(item.getPictures()).doesNotContain(picture);
        }

        @Test
        @DisplayName("foto não encontrada lança NOT_FOUND")
        void pictureNotFound_throwsNotFound() {
            ItemDeleteImageDto dto = new ItemDeleteImageDto(itemId, "http://localhost:9000/itemsimage/test.jpg");
            when(pictureRepository.findByUrl("/itemsimage/test.jpg")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteImage(dto))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.NOT_FOUND));
        }

        @Test
        @DisplayName("item não encontrado lança NOT_FOUND")
        void itemNotFound_throwsNotFound() {
            Item item = buildItem(Status.DISPONIBLE);
            Picture picture = new Picture(UUID.randomUUID(), "/itemsimage/test.jpg", item);
            ItemDeleteImageDto dto = new ItemDeleteImageDto(itemId, "http://localhost:9000/itemsimage/test.jpg");

            when(pictureRepository.findByUrl("/itemsimage/test.jpg")).thenReturn(Optional.of(picture));
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteImage(dto))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.NOT_FOUND));
        }

        @Test
        @DisplayName("imagem pertence a outro item lança BAD_REQUEST")
        void pictureBelongsToDifferentItem_throwsBadRequest() {
            Item item1 = buildItem(Status.DISPONIBLE);
            UUID otherId = UUID.randomUUID();
            Item item2 = new Item(
                otherId, "Outro item", LocalDate.now().minusDays(2), LocalDate.now().plusDays(28),
                Status.DISPONIBLE, DayPeriod.MORNING, Category.BOOK, Area.LIBRARY,
                null, "BOOK-2000", new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>()
            );
            Picture picture = new Picture(UUID.randomUUID(), "/itemsimage/test.jpg", item1);
            ItemDeleteImageDto dto = new ItemDeleteImageDto(otherId, "http://localhost:9000/itemsimage/test.jpg");

            when(pictureRepository.findByUrl("/itemsimage/test.jpg")).thenReturn(Optional.of(picture));
            when(itemRepository.findById(otherId)).thenReturn(Optional.of(item2));

            assertThatThrownBy(() -> service.deleteImage(dto))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── deleteAllImages ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteAllImages")
    class DeleteAllImages {

        @Test
        @DisplayName("remove todas as imagens do Minio e limpa a lista")
        void success_deletesFromMinioAndClearsList() {
            Item item = buildItem(Status.DISPONIBLE);
            Picture p1 = new Picture(UUID.randomUUID(), "http://url1", item);
            Picture p2 = new Picture(UUID.randomUUID(), "http://url2", item);
            ReflectionTestUtils.setField(item, "pictures", new ArrayList<>(List.of(p1, p2)));

            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(itemRepository.save(item)).thenReturn(item);

            service.deleteAllImages(itemId);

            verify(minioClient).deleteItemImage("http://url1");
            verify(minioClient).deleteItemImage("http://url2");
            assertThat(item.getPictures()).isEmpty();
        }

        @Test
        @DisplayName("item não encontrado lança NOT_FOUND")
        void itemNotFound_throwsNotFound() {
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteAllImages(itemId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }

    // ── deleteItem ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteItem")
    class DeleteItem {

        @Test
        @DisplayName("item DISPONIBLE é deletado com sucesso")
        void disponibleItem_deletesSuccessfully() {
            Item item = buildItem(Status.DISPONIBLE);
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

            service.deleteItem(itemId);

            verify(itemRepository).deleteById(itemId);
        }

        @Test
        @DisplayName("item não encontrado lança BAD_REQUEST")
        void itemNotFound_throwsBadRequest() {
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteItem(itemId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("item CLAIMED lança CONFLICT")
        void claimedItem_throwsConflict() {
            Item item = buildItem(Status.CLAIMED);
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.deleteItem(itemId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.CONFLICT));
        }

        @Test
        @DisplayName("item CHARITY lança CONFLICT")
        void charityItem_throwsConflict() {
            Item item = buildItem(Status.CHARITY);
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.deleteItem(itemId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.CONFLICT));
        }
    }

    // ── editItem ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("editItem")
    class EditItem {

        @Test
        @DisplayName("item DISPONIBLE é atualizado")
        void disponibleItem_updatesItem() {
            Item item = buildItem(Status.DISPONIBLE);
            ItemEditRequestDto dto = new ItemEditRequestDto("Nova descricao", Category.ELECTRONIC, Area.RC);

            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(itemRepository.save(item)).thenReturn(item);

            service.editItem(itemId, dto);

            verify(itemRepository).save(item);
        }

        @Test
        @DisplayName("item não encontrado lança NOT_FOUND")
        void itemNotFound_throwsNotFound() {
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.editItem(itemId, new ItemEditRequestDto(null, null, null)))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.NOT_FOUND));
        }

        @Test
        @DisplayName("item não DISPONIBLE lança BAD_REQUEST")
        void nonDisponibleItem_throwsBadRequest() {
            Item item = buildItem(Status.CLAIMED);
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.editItem(itemId, new ItemEditRequestDto(null, null, null)))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── getItem ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getItem")
    class GetItem {

        @Test
        @DisplayName("item existente retorna DTO com dados corretos")
        void existingItem_returnsDto() {
            Item item = buildItem(Status.DISPONIBLE);
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

            ItemResponseDto result = service.getItem(itemId);

            assertThat(result.getId()).isEqualTo(itemId);
            assertThat(result.getStatus()).isEqualTo(Status.DISPONIBLE);
        }

        @Test
        @DisplayName("item não encontrado lança BAD_REQUEST")
        void notFound_throwsBadRequest() {
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getItem(itemId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── getAllItems ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllItems")
    class GetAllItems {

        @Test
        @DisplayName("filtro vazio busca apenas itens DISPONIBLE")
        void emptyFilter_queriesDisponibleItems() {
            ItemFilterDto emptyFilter = new ItemFilterDto(null, null, null, null, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> page = new PageImpl<>(List.of(buildItem(Status.DISPONIBLE)));

            when(itemRepository.findAllItemByStatus(Status.DISPONIBLE, pageable)).thenReturn(page);

            Page<ItemResponseDto> result = service.getAllItems(pageable, emptyFilter);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("filtro com status usa query de filtro")
        void withStatusFilter_usesFilterQuery() {
            ItemFilterDto filter = new ItemFilterDto(null, null, null, null, null, Status.CLAIMED);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> page = new PageImpl<>(List.of(buildItem(Status.CLAIMED)));

            when(itemRepository.findByFilterQuery(
                eq(pageable), any(), isNull(), eq(Status.CLAIMED), isNull(), isNull(), isNull())
            ).thenReturn(page);

            Page<ItemResponseDto> result = service.getAllItems(pageable, filter);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("período com data inicial após final lança BAD_REQUEST")
        void startPeriodAfterEndPeriod_throwsBadRequest() {
            ItemFilterDto filter = new ItemFilterDto(
                null, null, LocalDate.now().plusDays(5), LocalDate.now(), null, null
            );

            assertThatThrownBy(() -> service.getAllItems(PageRequest.of(0, 10), filter))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── recoveryItem ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("recoveryItem")
    class RecoveryItem {

        private LocalDateTime futureDateTime;
        private AvailableDay availableDay;

        @BeforeEach
        void prepareAgenda() {
            futureDateTime = nextMonday();
            availableDay = buildAvailableDay(
                DayOfWeekEnum.valueOf(futureDateTime.getDayOfWeek().name()),
                LocalTime.of(8, 0), LocalTime.of(12, 0)
            );
        }

        @Test
        @DisplayName("solicitação válida salva recovery e notifica")
        void validRequest_savesRecoveryAndNotifies() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, EMAIL, "Meu item", futureDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(recoveryRepository.existsByUserAndItemAndStatusIn(eq(user), eq(item), any())).thenReturn(false);
            when(recoveryRepository.countByUserAndStatus(user, StatusRecovery.PENDING)).thenReturn(0L);
            when(availableDayRepository.findAllByAvailableDay(DayOfWeekEnum.valueOf(futureDateTime.getDayOfWeek().name())))
                .thenReturn(Optional.of(availableDay));

            service.recoveryItem(dto, TOKEN);

            verify(recoveryRepository).save(any(Recovery.class));
            verify(notificationToItemService).requestCreated(item, user);
        }

        @Test
        @DisplayName("item não encontrado lança BAD_REQUEST")
        void itemNotFound_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, EMAIL, "desc", futureDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("email do token diferente do email da requisição lança UNAUTHORIZED")
        void emailMismatch_throwsUnauthorized() {
            User user = buildUser(Role.COMMON);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, "other@ifba.edu.br", "desc", futureDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
        }

        @Test
        @DisplayName("usuário ROOT não pode fazer solicitação lança BAD_REQUEST")
        void rootUser_throwsBadRequest() {
            User root = buildUser(Role.ROOT);
            Item item = buildItem(Status.DISPONIBLE);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, EMAIL, "desc", futureDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(root));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("item não DISPONIBLE lança BAD_REQUEST")
        void itemNotDisponible_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            Item claimed = buildItem(Status.CLAIMED);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, EMAIL, "desc", futureDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(claimed));

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("solicitação duplicada lança BAD_REQUEST")
        void duplicateRequest_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, EMAIL, "desc", futureDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(recoveryRepository.existsByUserAndItemAndStatusIn(eq(user), eq(item), any())).thenReturn(true);

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("limite máximo de solicitações ativas atingido lança BAD_REQUEST")
        void maxActiveRequestsExceeded_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, EMAIL, "desc", futureDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(recoveryRepository.existsByUserAndItemAndStatusIn(eq(user), eq(item), any())).thenReturn(false);
            when(recoveryRepository.countByUserAndStatus(user, StatusRecovery.PENDING)).thenReturn((long) MAX_ACTIVE_REQUESTS);

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("data/hora da retirada no passado lança BAD_REQUEST")
        void dateTimeInPast_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(
                itemId, EMAIL, "desc", LocalDateTime.now().minusDays(1)
            );

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(recoveryRepository.existsByUserAndItemAndStatusIn(eq(user), eq(item), any())).thenReturn(false);
            when(recoveryRepository.countByUserAndStatus(user, StatusRecovery.PENDING)).thenReturn(0L);

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("dia não disponível na agenda lança BAD_REQUEST")
        void dayNotAvailableInAgenda_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, EMAIL, "desc", futureDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(recoveryRepository.existsByUserAndItemAndStatusIn(eq(user), eq(item), any())).thenReturn(false);
            when(recoveryRepository.countByUserAndStatus(user, StatusRecovery.PENDING)).thenReturn(0L);
            when(availableDayRepository.findAllByAvailableDay(DayOfWeekEnum.valueOf(futureDateTime.getDayOfWeek().name())))
                .thenReturn(Optional.empty());
            when(availableDayRepository.findAll()).thenReturn(new ArrayList<>());

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("horário fora do intervalo disponível lança BAD_REQUEST")
        void timeNotAvailableInAgenda_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            // Agenda: 08:00-12:00 / request: 15:00 (fora do intervalo)
            LocalDateTime afternoonDateTime = futureDateTime.withHour(15);
            ItemRecoveryRequestDto dto = new ItemRecoveryRequestDto(itemId, EMAIL, "desc", afternoonDateTime);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(recoveryRepository.existsByUserAndItemAndStatusIn(eq(user), eq(item), any())).thenReturn(false);
            when(recoveryRepository.countByUserAndStatus(user, StatusRecovery.PENDING)).thenReturn(0L);
            when(availableDayRepository.findAllByAvailableDay(DayOfWeekEnum.valueOf(afternoonDateTime.getDayOfWeek().name())))
                .thenReturn(Optional.of(availableDay));

            assertThatThrownBy(() -> service.recoveryItem(dto, TOKEN))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── recoveryReview ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("recoveryReview")
    class RecoveryReview {

        @Test
        @DisplayName("APPROVED: item fica RESERVED, demais solicitações são REFUSED")
        void approved_claimsItemAndRejectsOtherRequests() {
            User user1 = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery recovery = buildRecovery(item, user1, StatusRecovery.PENDING);

            User user2 = new User(
                UUID.randomUUID(), "Outro", "98765432100", "other@ifba.edu.br",
                Role.COMMON, StatusMember.ACTIVE, 0, "pass", "71888888888",
                "CODE456", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
            );
            Recovery recovery2 = new Recovery(
                new ItemRecoveryRequestDto(itemId, "other@ifba.edu.br", "Outro pedido", LocalDateTime.now().plusDays(3)),
                user2, item
            );
            ReflectionTestUtils.setField(recovery2, "id", UUID.randomUUID());

            ItemRequestReviewDto reviewDto = new ItemRequestReviewDto(recoveryId, StatusRecovery.APPROVED);

            when(recoveryRepository.findById(recoveryId)).thenReturn(Optional.of(recovery));
            when(recoveryRepository.findAllByItem(item)).thenReturn(List.of(recovery, recovery2));
            when(recoveryRepository.save(any(Recovery.class))).thenReturn(recovery);
            when(itemRepository.save(item)).thenReturn(item);

            service.recoveryReview(reviewDto);

            assertThat(recovery.getStatus()).isEqualTo(StatusRecovery.APPROVED);
            assertThat(recovery2.getStatus()).isEqualTo(StatusRecovery.REFUSED);
            assertThat(item.getStatus()).isEqualTo(Status.RESERVED);
            assertThat(item.getOwner()).isNull();
            assertThat(item.getDateReturned()).isNull();
            verify(notificationToItemService).sendNotificationToRequester(recovery);
            verify(notificationToItemService).recoveryRejected(recovery2, true);
        }

        @Test
        @DisplayName("APPROVED: solicitação recusada anteriormente não é alterada e a aprovação é concluída")
        void approved_ignoresPreviouslyRefusedRequests() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.PENDING);
            Recovery refused = buildRecovery(item, user, StatusRecovery.REFUSED);
            ReflectionTestUtils.setField(refused, "id", UUID.randomUUID());
            ItemRequestReviewDto reviewDto = new ItemRequestReviewDto(recoveryId, StatusRecovery.APPROVED);

            when(recoveryRepository.findById(recoveryId)).thenReturn(Optional.of(recovery));
            when(recoveryRepository.findAllByItem(item)).thenReturn(List.of(refused, recovery));
            when(recoveryRepository.save(any(Recovery.class))).thenReturn(recovery);
            when(itemRepository.save(item)).thenReturn(item);

            service.recoveryReview(reviewDto);

            assertThat(recovery.getStatus()).isEqualTo(StatusRecovery.APPROVED);
            assertThat(refused.getStatus()).isEqualTo(StatusRecovery.REFUSED);
            assertThat(item.getStatus()).isEqualTo(Status.RESERVED);
            assertThat(item.getOwner()).isNull();
            assertThat(item.getDateReturned()).isNull();
            verify(notificationToItemService, never()).recoveryRejected(refused, true);
        }

        @Test
        @DisplayName("REFUSED: status atualizado e notificação enviada ao solicitante")
        void refused_updatesStatusAndNotifies() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.PENDING);
            ItemRequestReviewDto reviewDto = new ItemRequestReviewDto(recoveryId, StatusRecovery.REFUSED);

            when(recoveryRepository.findById(recoveryId)).thenReturn(Optional.of(recovery));
            when(recoveryRepository.save(recovery)).thenReturn(recovery);

            service.recoveryReview(reviewDto);

            assertThat(recovery.getStatus()).isEqualTo(StatusRecovery.REFUSED);
            verify(notificationToItemService).recoveryRejected(recovery, false);
        }

        @Test
        @DisplayName("solicitação não encontrada lança BAD_REQUEST")
        void recoveryNotFound_throwsBadRequest() {
            ItemRequestReviewDto reviewDto = new ItemRequestReviewDto(recoveryId, StatusRecovery.APPROVED);
            when(recoveryRepository.findById(recoveryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.recoveryReview(reviewDto))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("solicitação já processada (não PENDING) lança BAD_REQUEST")
        void recoveryAlreadyProcessed_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery alreadyApproved = buildRecovery(item, user, StatusRecovery.APPROVED);
            ItemRequestReviewDto reviewDto = new ItemRequestReviewDto(recoveryId, StatusRecovery.REFUSED);

            when(recoveryRepository.findById(recoveryId)).thenReturn(Optional.of(alreadyApproved));

            assertThatThrownBy(() -> service.recoveryReview(reviewDto))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── confirmDelivery ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("confirmDelivery")
    class ConfirmDelivery {

        @Test
        @DisplayName("item RESERVED e pedido APPROVED: item vira CLAIMED e pedido vira DELIVERED")
        void reservedItem_confirmsDelivery() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.RESERVED);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.APPROVED);

            when(recoveryRepository.findById(recoveryId)).thenReturn(Optional.of(recovery));

            service.confirmDelivery(recoveryId);

            assertThat(item.getStatus()).isEqualTo(Status.CLAIMED);
            assertThat(item.getOwner()).isEqualTo(user);
            assertThat(item.getDateReturned()).isEqualTo(LocalDate.now());
            assertThat(recovery.getStatus()).isEqualTo(StatusRecovery.DELIVERED);
            verify(itemRepository).save(item);
            verify(recoveryRepository).save(recovery);
        }

        @Test
        @DisplayName("item que não está reservado lança BAD_REQUEST")
        void itemNotReserved_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.APPROVED);

            when(recoveryRepository.findById(recoveryId)).thenReturn(Optional.of(recovery));

            assertThatThrownBy(() -> service.confirmDelivery(recoveryId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
            assertThat(recovery.getStatus()).isEqualTo(StatusRecovery.APPROVED);
        }

        @Test
        @DisplayName("solicitação não encontrada lança BAD_REQUEST")
        void recoveryNotFound_throwsBadRequest() {
            when(recoveryRepository.findById(recoveryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.confirmDelivery(recoveryId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── expireReservations ───────────────────────────────────────────────────

    @Nested
    @DisplayName("expireReservations")
    class ExpireReservations {

        @Test
        @DisplayName("reserva não retirada no prazo: pedido vira EXPIRED, item volta a DISPONIBLE e o solicitante é avisado")
        void overdueReservation_expiresAndReleasesItem() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.RESERVED);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.APPROVED);

            when(recoveryRepository.findAllByStatusAndRecoveryDateTimeBefore(eq(StatusRecovery.APPROVED), any()))
                .thenReturn(List.of(recovery));

            service.expireReservations();

            assertThat(recovery.getStatus()).isEqualTo(StatusRecovery.EXPIRED);
            assertThat(item.getStatus()).isEqualTo(Status.DISPONIBLE);
            verify(notificationToItemService).reservationExpired(recovery);
        }

        @Test
        @DisplayName("sem reservas vencidas nada é alterado")
        void noOverdueReservations_doesNothing() {
            when(recoveryRepository.findAllByStatusAndRecoveryDateTimeBefore(eq(StatusRecovery.APPROVED), any()))
                .thenReturn(List.of());

            service.expireReservations();

            verify(itemRepository, never()).save(any());
            verify(notificationToItemService, never()).reservationExpired(any());
        }
    }

    // ── removeExpiredPictures ────────────────────────────────────────────────

    @Nested
    @DisplayName("removeExpiredPictures")
    class RemoveExpiredPictures {

        @Test
        @DisplayName("fotos de itens entregues há mais tempo que o prazo são apagadas do Minio")
        void claimedItemPastRetention_deletesPictures() {
            Item item = buildItem(Status.CLAIMED);
            Picture picture = new Picture("http://minio/itemsimage/foto.jpg", item);
            item.getPictures().add(picture);

            when(itemRepository.findByDateReturnedLessThanEqualAndStatus(any(), eq(Status.CLAIMED))).thenReturn(List.of(item));
            when(itemRepository.findByDonationDateLessThanEqualAndStatus(any(), eq(Status.CHARITY))).thenReturn(List.of());

            service.removeExpiredPictures();

            verify(minioClient).deleteItemImage("http://minio/itemsimage/foto.jpg");
            assertThat(item.getPictures()).isEmpty();
            verify(itemRepository).save(item);
        }

        @Test
        @DisplayName("itens sem fotos não geram exclusões")
        void itemsWithoutPictures_doNothing() {
            Item item = buildItem(Status.CHARITY);

            when(itemRepository.findByDateReturnedLessThanEqualAndStatus(any(), eq(Status.CLAIMED))).thenReturn(List.of());
            when(itemRepository.findByDonationDateLessThanEqualAndStatus(any(), eq(Status.CHARITY))).thenReturn(List.of(item));

            service.removeExpiredPictures();

            verify(minioClient, never()).deleteItemImage(any());
            verify(itemRepository, never()).save(any());
        }
    }

    // ── getAllRecoveries ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllRecoveries")
    class GetAllRecoveries {

        @Test
        @DisplayName("sem filtro retorna todas as solicitações paginadas")
        void noFilter_returnsAllPaged() {
            Pageable pageable = PageRequest.of(0, 10);
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.PENDING);
            Page<Recovery> page = new PageImpl<>(List.of(recovery));

            when(recoveryRepository.findAll(pageable)).thenReturn(page);

            Page<RecoveryResponse> result = service.getAllRecoveries(pageable, null);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("com filtro de status retorna apenas solicitações com aquele status")
        void withStatusFilter_returnsFilteredPage() {
            Pageable pageable = PageRequest.of(0, 10);
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.PENDING);
            Page<Recovery> page = new PageImpl<>(List.of(recovery));

            when(recoveryRepository.findAllByStatus(StatusRecovery.PENDING, pageable)).thenReturn(page);

            Page<RecoveryResponse> result = service.getAllRecoveries(pageable, StatusRecovery.PENDING);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(StatusRecovery.PENDING);
        }
    }

    // ── getRecoveriesByItem ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getRecoveriesByItem")
    class GetRecoveriesByItem {

        @Test
        @DisplayName("item existente retorna suas solicitações")
        void existingItem_returnsRecoveries() {
            Item item = buildItem(Status.DISPONIBLE);
            User user = buildUser(Role.COMMON);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.PENDING);

            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(recoveryRepository.findAllByItem(item)).thenReturn(List.of(recovery));

            RecoveryResponseByItem result = service.getRecoveriesByItem(itemId);

            assertThat(result.getRecovery()).hasSize(1);
        }

        @Test
        @DisplayName("item não encontrado lança BAD_REQUEST")
        void itemNotFound_throwsBadRequest() {
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getRecoveriesByItem(itemId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── getRecoveriesByUser ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getRecoveriesByUser")
    class GetRecoveriesByUser {

        @Test
        @DisplayName("usuário existente retorna suas solicitações")
        void existingUser_returnsRecoveries() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.PENDING);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(recoveryRepository.findAllByUser(user)).thenReturn(List.of(recovery));

            RecoveryResponseByUser result = service.getRecoveriesByUser(userId);

            assertThat(result.getRecovery()).hasSize(1);
        }

        @Test
        @DisplayName("usuário não encontrado lança BAD_REQUEST")
        void userNotFound_throwsBadRequest() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getRecoveriesByUser(userId))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── getSelfRecoveriesByUser ───────────────────────────────────────────────

    @Nested
    @DisplayName("getSelfRecoveriesByUser")
    class GetSelfRecoveriesByUser {

        @Test
        @DisplayName("requisição válida retorna página de solicitações do usuário")
        void validRequest_returnsPagedRecoveries() {
            User user = buildUser(Role.COMMON);
            Item item = buildItem(Status.DISPONIBLE);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.PENDING);
            RecoveryFilterDto filterDto = new RecoveryFilterDto(null, EMAIL, null, null, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Recovery> page = new PageImpl<>(List.of(recovery), pageable, 1);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(recoveryRepository.findSelfRecoveriesByFilter(
                eq(user), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable))
            ).thenReturn(page);

            Page<RecoveryResponseByUser> result = service.getSelfRecoveriesByUser(TOKEN, pageable, filterDto);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("email do token diferente do filtro lança UNAUTHORIZED")
        void emailMismatch_throwsUnauthorized() {
            User user = buildUser(Role.COMMON);
            RecoveryFilterDto filterDto = new RecoveryFilterDto(null, "other@ifba.edu.br", null, null, null, null);

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.getSelfRecoveriesByUser(TOKEN, PageRequest.of(0, 10), filterDto))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
        }

        @Test
        @DisplayName("data inicial do período após data final lança BAD_REQUEST")
        void startDateAfterEndDate_throwsBadRequest() {
            User user = buildUser(Role.COMMON);
            RecoveryFilterDto filterDto = new RecoveryFilterDto(
                null, EMAIL, null, LocalDate.now().plusDays(5), LocalDate.now(), null
            );

            when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.getSelfRecoveriesByUser(TOKEN, PageRequest.of(0, 10), filterDto))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ── refreshItemToCharity ─────────────────────────────────────────────────

    @Nested
    @DisplayName("refreshItemToCharity")
    class RefreshItemToCharity {

        @Test
        @DisplayName("itens expirados viram CHARITY e solicitações pendentes são REFUSED")
        void expiredItems_setsCharityAndRejectsPendingRecoveries() {
            Item item = buildItem(Status.DISPONIBLE);
            User user = buildUser(Role.COMMON);
            Recovery recovery = buildRecovery(item, user, StatusRecovery.PENDING);

            when(itemRepository.findByDonationDateLessThanEqualAndStatus(LocalDate.now(), Status.DISPONIBLE))
                .thenReturn(List.of(item));
            when(recoveryRepository.findAllByItem(item)).thenReturn(List.of(recovery));
            when(recoveryRepository.save(any(Recovery.class))).thenReturn(recovery);

            service.refreshItemToCharity();

            assertThat(item.getStatus()).isEqualTo(Status.CHARITY);
            assertThat(recovery.getStatus()).isEqualTo(StatusRecovery.REFUSED);
            verify(notificationToItemService).newItemOnCharity(item);
            verify(notificationToItemService).recoveryRejected(recovery, true);
        }

        @Test
        @DisplayName("solicitações já recusadas não são alteradas ao separar o item para doação")
        void expiredItems_ignoresAlreadyRefusedRecoveries() {
            Item item = buildItem(Status.DISPONIBLE);
            User user = buildUser(Role.COMMON);
            Recovery refused = buildRecovery(item, user, StatusRecovery.REFUSED);

            when(itemRepository.findByDonationDateLessThanEqualAndStatus(LocalDate.now(), Status.DISPONIBLE))
                .thenReturn(List.of(item));
            when(recoveryRepository.findAllByItem(item)).thenReturn(List.of(refused));

            service.refreshItemToCharity();

            assertThat(item.getStatus()).isEqualTo(Status.CHARITY);
            verify(notificationToItemService, never()).recoveryRejected(any(), anyBoolean());
            verify(notificationToItemService).newItemOnCharity(item);
        }

        @Test
        @DisplayName("sem itens expirados nenhuma notificação é enviada")
        void noExpiredItems_doesNothing() {
            when(itemRepository.findByDonationDateLessThanEqualAndStatus(LocalDate.now(), Status.DISPONIBLE))
                .thenReturn(new ArrayList<>());

            service.refreshItemToCharity();

            verify(notificationToItemService, never()).newItemOnCharity(any());
        }
    }
}
