package com.ifba.sipapi.user.api.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import com.ifba.sipapi.item.domain.item.Status;
import com.ifba.sipapi.item.domain.recoveryRequest.StatusRecovery;
import com.ifba.sipapi.item.infra.item.ItemRepository;
import com.ifba.sipapi.item.infra.recovery.RecoveryRepository;
import com.ifba.sipapi.minio.api.service.MinioClient;
import com.ifba.sipapi.notification.infra.NotificationRepository;
import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.*;
import com.ifba.sipapi.user.infra.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserApplicationService implements UserService {
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaApplicationEmailProducer kafkaApplicationEmailProducer;
    private final MinioClient minioClient;
    private final RecoveryRepository recoveryRepository;
    private final ItemRepository itemRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public void verifyAccountWithToken(String token) {
        log.info("[start] verifyAccountWithToken");
        EmailData emailData = extractPayloadFromToken(token);
        log.info(emailData.to());
        verifyAccount(emailData);
        log.debug("[finish] verifyAccountWithToken");
    }

    @Override
    public void verifyAccount(EmailData emailData) {
        log.info("[start] verifyAccount");
        User user = userRepository.findByEmail(emailData.to())
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        user.handleAccountVerification(emailData.code());
        userRepository.save(user);
        log.debug("[finish] verifyAccount");
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("[start] AuthenticationApplicationService - resendVerificationEmail");
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getStatusMember().equals(StatusMember.NOT_VERIFIED))
                .orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Usuário ja foi verificado ou email não encontrado!"));
        sendEmail(user.getEmail(), EmailType.VERIFICATION);
        log.debug("[finish] AuthenticationApplicationService - resendVerificationEmail");
    }

    @Override
    public void recoverPassword(String email) {
        log.info("[start] UserApplicationService - recoverPassword");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        user.updatePasswordRecoveryCode();
        userRepository.save(user);
        this.sendEmail(email, EmailType.RECOVER_PASSWORD);
        log.debug("[finish] UserApplicationService - recoverPassword");
    }

    @Override
    public void resetPassword(UserPasswordRecoveryDto userPasswordRecoveryDto) {
        log.info("[start] UserApplicationService - resetPassword");
        EmailData payload = extractPayloadFromToken(userPasswordRecoveryDto.getToken());
        User user = userRepository.findByEmail(payload.to())
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        user.resetPassword(payload.code(), passwordEncoder.encode(userPasswordRecoveryDto.getPassword()));
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - resetPassword");
    }

    @Override
    public void checkAndSendEmail(String email) {
        log.info("[start] AuthenticationApplicationService - checkAndSendEmail");
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getStatusMember().equals(StatusMember.BLOCKED))
                .orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Usuário não está bloqueado ou email não encontrado!"));
        sendEmail(user.getEmail(), EmailType.REACTIVATE);
        user.updateAccountReactivationCode();
        userRepository.save(user);
        log.debug("[finish] AuthenticationApplicationService - checkAndSendEmail");
    }

    @Override
    public void accountReactivation(String token) {
        log.info("[start] UserApplicationService - accountReactivation");
        EmailData emailData = extractPayloadFromToken(token);
        User user = userRepository.findByEmail(emailData.to())
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        user.handleAccountReactivation(emailData.code());
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - accountReactivation");
    }

    @Override
    public void updateUser(UserUpdateDto userUpdateDto, String email, String token) {
        log.info("[start] UserApplicationService - updateUser");
        User user = assertEmailBelongsToAndReturnUser(token, email);
        user.updateUser(userUpdateDto);
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - updateUser");
    }

    @Override
    public void updateUserByRoot(UserUpdateDto userUpdateDto, String email) {
        log.info("[start] UserApplicationService - updateUserRoot");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        user.updateUser(userUpdateDto);
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - updateUserRoot");
    }

    @Override
    public void updatePassword(String email, UserPasswordUpdateDto userPasswordUpdateDto, String token) {
        log.info("[start] UserApplicationService - updatePassword");
        User user = assertEmailBelongsToAndReturnUser(token, email);
        generatePasswordHash(userPasswordUpdateDto);
        user.updatePassword(userPasswordUpdateDto, passwordEncoder);
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - updatePassword");
    }

    @Override
    public void updateProfileImage(MultipartFile profileImage, String token, String email) {
        log.info("[start] UserApplicationService - updateProfileImage");
        User user  = userRepository.findByEmail(email).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuario não encontrado!"));

        if(token != null || !user.getStatusMember().equals(StatusMember.NOT_VERIFIED))
            assertEmailBelongsToAndReturnUser(token, email);

        String oldImageUrl = user.getProfileImageUrl();
        user.updateProfileImage(minioClient.uploadUserProfileImage(profileImage, user));
        userRepository.save(user);
        deleteOldProfileImageQuietly(oldImageUrl);
        log.debug("[finish] UserApplicationService - updateProfileImage");
    }

    @Override
    public void updateProfileImageByRoot(MultipartFile profileImage, String email) {
        log.info("[start] UserApplicationService - updateProfileImageByRoot");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        String oldImageUrl = user.getProfileImageUrl();
        user.updateProfileImage(minioClient.uploadUserProfileImage(profileImage, user));
        userRepository.save(user);
        deleteOldProfileImageQuietly(oldImageUrl);
        log.debug("[finish] UserApplicationService - updateProfileImageByRoot");
    }

    private void deleteOldProfileImageQuietly(String oldImageUrl) {
        if (oldImageUrl == null || oldImageUrl.isBlank())
            return;
        try {
            minioClient.deleteItemImage(oldImageUrl);
        } catch (Exception e) {
            log.warn("Falha ao remover foto de perfil antiga ({}): {}", oldImageUrl, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteAdmin(String email) {
        log.info("[start] UserApplicationService - deleteAdmin");
        deleteUserByRoot(email);
        log.debug("[finish] UserApplicationService - deleteAdmin");
    }

    @Override
    @Transactional
    public void deleteOwnAccount(String token, String email, UserAccountDeletionDto userAccountDeletionDto) {
        log.info("[start] UserApplicationService - deleteOwnAccount");
        User user = assertEmailBelongsToAndReturnUser(token, email);
        if (!passwordEncoder.matches(userAccountDeletionDto.getPassword(), user.getPassword()))
            throw APIException.build(HttpStatus.BAD_REQUEST, "Senha atual informada inválida.");
        anonymizeUser(user);
        log.debug("[finish] UserApplicationService - deleteOwnAccount");
    }

    @Override
    @Transactional
    public void deleteUserByRoot(String email) {
        log.info("[start] UserApplicationService - deleteUserByRoot");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        anonymizeUser(user);
        log.debug("[finish] UserApplicationService - deleteUserByRoot");
    }

    /**
     * Elimina os dados pessoais do usuário por anonimização: cancela os pedidos em andamento,
     * libera os itens reservados, apaga os avisos e a foto de perfil e substitui os dados cadastrais.
     */
    private void anonymizeUser(User user) {
        if (user.getRole() == Role.ROOT)
            throw APIException.build(HttpStatus.BAD_REQUEST, "O usuário root não pode ser excluído");
        if (user.isDeleted())
            throw APIException.build(HttpStatus.BAD_REQUEST, "Conta já excluída");

        recoveryRepository.findAllByUser(user).stream()
                .filter(r -> r.getStatus() == StatusRecovery.PENDING || r.getStatus() == StatusRecovery.APPROVED)
                .forEach(recovery -> {
                    boolean wasApproved = recovery.getStatus() == StatusRecovery.APPROVED;
                    recovery.cancel();
                    recoveryRepository.save(recovery);
                    if (wasApproved && recovery.getItem().getStatus() == Status.RESERVED) {
                        recovery.getItem().releaseReservation();
                        itemRepository.save(recovery.getItem());
                    }
                });

        notificationRepository.deleteAll(notificationRepository.getNotificationByOwner(user));

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank())
            minioClient.deleteItemImage(user.getProfileImageUrl());

        long sequence = userRepository.countByStatusMember(StatusMember.DELETED) + 1;
        while (userRepository.existsByEmail("excluido-" + String.format("%02d", sequence) + "@excluido.sip"))
            sequence++;
        user.anonymize(sequence);
        userRepository.save(user);
    }

    @Override
    public UserDetailsResponseDto getUserDetails(String email, String token) {
        log.info("[start] UserApplicationService - getUserDetails");
        User user = assertEmailBelongsToAndReturnUser(token, email);
        UserDetailsResponseDto userDetailsResponse = new UserDetailsResponseDto(user);
        log.debug("[finish] UserApplicationService - getUserDetails");
        return userDetailsResponse;
    }

    private User assertEmailBelongsToAndReturnUser(String token, String email) {
        User user = userRepository.findByEmail(tokenService.getSubject(token)).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if(!user.getEmail().equals(email)){
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Token não corresponde ao email enviado");
        }
        return user;
    }

    private void sendEmail(String userEmail, EmailType emailType) {
        log.info("[start] AuthenticationApplicationService - sendEmail");
        EmailSender payload = new EmailSender(userEmail, emailType);
        kafkaApplicationEmailProducer.publishEmail(payload);
        log.debug("[finish] AuthenticationApplicationService - sendEmail");
    }

    private EmailData extractPayloadFromToken(String token) {
        String json;
        try {
            json = tokenService.validateToken(token);
            return objectMapper.readValue(json, EmailData.class);
        } catch (Exception e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Erro ao processar o token, verifique a validade e tente novamente.");
        }
    }

    private void generatePasswordHash(UserPasswordUpdateDto userPasswordUpdateDto) {
        log.info("[start] UserApplicationService - generatePasswordHash");
        userPasswordUpdateDto.updateHashedPassword(passwordEncoder.encode(userPasswordUpdateDto.getNewPassword()));
        log.debug("[finish] UserApplicationService - generatePasswordHash");
    }

    public List<UserDetailsResponseDto> getUserAdmins(String email, String token) {
        log.info("[start] UserApplicationService - getUserAdmins");

        assertEmailBelongsToAndReturnUser(token, email);

        List<UserDetailsResponseDto> admins = userRepository.findByRole(Role.ADMIN)
                .stream()
                .map(UserDetailsResponseDto::new)
                .toList();

        log.info("[finish] UserApplicationService - getUserAdmins");
        return admins;
    }

    public UserDetailsResponseDto getAdminDetail(String email) {
        log.info("[start] UserApplicationService - getAdminDetail email={}", email);
        if (email == null || email.isBlank())
            throw APIException.build(HttpStatus.BAD_REQUEST, "Email é obrigatório");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        log.info("[finish] UserApplicationService - getAdminDetail email={}", email);
        return new UserDetailsResponseDto(user);
    }

}
