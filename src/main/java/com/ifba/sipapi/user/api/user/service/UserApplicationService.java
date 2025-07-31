package com.ifba.sipapi.user.api.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.mail.domain.EmailData;
import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.*;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserApplicationService implements UserService {
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaApplicationEmailProducer kafkaApplicationEmailProducer;
    private final S3Client s3Client;

    @Value("${minio.bucketProfile}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

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
        log.info(emailData.code());
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
        User user = assertEmailBelongsToAndReturnUser(token, email);
        ensureBucketExists();

        String filename = generateProfileImageFilename(user, profileImage);
        uploadFileToBucket(profileImage, filename);

        String imageUrl = buildPublicImageUrl(filename);
        user.updateProfileImage(imageUrl);
        userRepository.save(user);
        log.debug("[finish] UserApplicationService - updateProfileImage");
    }

    public UserDetailsResponseDto getUserDetails(String email, String token) {
        log.info("[start] UserApplicationService - getUserDetails");
        User user = assertEmailBelongsToAndReturnUser(token, email);
        UserDetailsResponseDto userDetailsResponse = new UserDetailsResponseDto(user);
        log.debug("[finish] UserApplicationService - getUserDetails");
        return userDetailsResponse;
    }

    private void ensureBucketExists() {
        boolean exists = s3Client.listBuckets().buckets().stream().anyMatch(b -> b.name().equals(bucket));

        if (!exists) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    private String generateProfileImageFilename(User user, MultipartFile file) {
        String extension = extractExtension(file);
        return user.getId() + "_profile" + extension;
    }

    private void uploadFileToBucket(MultipartFile file, String filename) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Unable to save the new image. Please try again.");
        }
    }

    private String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "";
    }

    private String buildPublicImageUrl(String filename) {
        return String.format("%s/%s/%s", minioEndpoint, bucket, filename);
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
}
