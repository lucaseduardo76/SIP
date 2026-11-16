package com.ifba.sipapi.user.api.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.item.domain.item.*;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.item.dto.ItemRecoveryRequestDto;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import com.ifba.sipapi.item.infra.recovery.RecoveryRepository;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import com.ifba.sipapi.minio.api.service.MinioClient;
import com.ifba.sipapi.notification.infra.NotificationRepository;
import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserAccountDeletionDto;
import com.ifba.sipapi.user.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Exclusão de conta (anonimização)")
class UserAccountDeletionTest {

    @Mock private TokenService tokenService;
    @Mock private ObjectMapper objectMapper;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private KafkaApplicationEmailProducer kafkaApplicationEmailProducer;
    @Mock private MinioClient minioClient;
    @Mock private RecoveryRepository recoveryRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks private UserApplicationService service;

    private static final String TOKEN = "Bearer token";
    private static final String EMAIL = "ana@ifba.edu.br";
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "Ana Souza", "52998224725", EMAIL, Role.COMMON,
                StatusMember.ACTIVE, 0, "hash", "73999990000", "123456",
                "http://minio/profimage/ana.jpg", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private Item item(Status status) {
        return new Item(UUID.randomUUID(), "Garrafa", LocalDate.now().minusDays(2), LocalDate.now().plusDays(100),
                status, DayPeriod.MORNING, Category.BOTTLE, Area.LIBRARY, null, "BOTT-1000",
                new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>());
    }

    private Recovery recovery(Item item, StatusRecovery status) {
        Recovery r = new Recovery(new ItemRecoveryRequestDto(item.getId(), EMAIL, "desc", LocalDateTime.now().plusDays(1)), user, item);
        ReflectionTestUtils.setField(r, "status", status);
        return r;
    }

    @Test
    @DisplayName("senha correta: dados pessoais anonimizados, pedidos cancelados e item reservado liberado")
    void correctPassword_anonymizesAndCancelsRequests() {
        Item reservado = item(Status.RESERVED);
        Item disponivel = item(Status.DISPONIBLE);
        Recovery aprovado = recovery(reservado, StatusRecovery.APPROVED);
        Recovery pendente = recovery(disponivel, StatusRecovery.PENDING);

        when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha@123", "hash")).thenReturn(true);
        when(recoveryRepository.findAllByUser(user)).thenReturn(List.of(aprovado, pendente));
        when(notificationRepository.getNotificationByOwner(user)).thenReturn(List.of());
        when(userRepository.countByStatusMember(StatusMember.DELETED)).thenReturn(0L);

        service.deleteOwnAccount(TOKEN, EMAIL, new UserAccountDeletionDto("Senha@123"));

        assertThat(user.getName()).isEqualTo("excluido-01");
        assertThat(user.getEmail()).isEqualTo("excluido-01@excluido.sip");
        assertThat(user.getCpf()).isEqualTo("00000000001");
        assertThat(user.getPhone()).isNull();
        assertThat(user.getStatusMember()).isEqualTo(StatusMember.DELETED);
        assertThat(aprovado.getStatus()).isEqualTo(StatusRecovery.EXPIRED);
        assertThat(pendente.getStatus()).isEqualTo(StatusRecovery.REFUSED);
        assertThat(reservado.getStatus()).isEqualTo(Status.DISPONIBLE);
        verify(minioClient).deleteItemImage("http://minio/profimage/ana.jpg");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("senha incorreta lança BAD_REQUEST e a conta não é alterada")
    void wrongPassword_throwsBadRequest() {
        when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteOwnAccount(TOKEN, EMAIL, new UserAccountDeletionDto("errada")))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(user.getName()).isEqualTo("Ana Souza");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("root anonimiza um usuário comum")
    void rootDeletesUser_anonymizes() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(recoveryRepository.findAllByUser(user)).thenReturn(List.of());
        when(notificationRepository.getNotificationByOwner(user)).thenReturn(List.of());
        when(userRepository.countByStatusMember(StatusMember.DELETED)).thenReturn(4L);

        service.deleteUserByRoot(EMAIL);

        assertThat(user.getName()).isEqualTo("excluido-05");
        assertThat(user.getStatusMember()).isEqualTo(StatusMember.DELETED);
    }

    @Test
    @DisplayName("o usuário root não pode ser excluído")
    void rootUser_cannotBeDeleted() {
        User root = new User(UUID.randomUUID(), "Root", "00000000000", "root@sip.com", Role.ROOT,
                StatusMember.ACTIVE, 0, "hash", null, "1", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(userRepository.findByEmail("root@sip.com")).thenReturn(Optional.of(root));

        assertThatThrownBy(() -> service.deleteUserByRoot("root@sip.com"))
                .isInstanceOf(APIException.class)
                .satisfies(ex -> assertThat(((APIException) ex).getStatusException()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
