package com.ifba.sipapi.user.api.authentication.service;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.mail.domain.EmailSender;
import com.ifba.sipapi.mail.domain.EmailType;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import com.ifba.sipapi.user.api.authentication.controller.AuthenticationResponseDto;
import com.ifba.sipapi.user.api.authentication.controller.TokenType;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import com.ifba.sipapi.user.dto.UserLoginDto;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationApplicationService implements AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final KafkaApplicationEmailProducer kafkaApplicationEmailProducer;
    private final TokenService tokenService;

    @Value("${security.token.jwt.expiration}")
    private Long expiration;

    @Override
    public void createNewUser(UserCommomRegisterDto userCommomRegisterDto) {
        log.info("[start] AuthenticationApplicationService - createNewUser");
        generatePasswordHash(userCommomRegisterDto);
        handleNewUserValidations(userCommomRegisterDto);
        User user = userRepository.save(new User(userCommomRegisterDto));
        sendEmail(user.getEmail(), EmailType.VERIFICATION);
        log.debug("[finish] AuthenticationApplicationService - createNewUser");
    }

    private void sendEmail(String userEmail, EmailType emailType){
        log.info("[start] AuthenticationApplicationService - sendEmail");
        EmailSender payload = new EmailSender(userEmail, emailType);
        kafkaApplicationEmailProducer.publishEmail(payload);
        log.debug("[finish] AuthenticationApplicationService - sendEmail");
    }


    private void handleNewUserValidations(UserCommomRegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "E-mail já está em uso. Por favor, utilize outro.");
        }

        if (userRepository.existsByCpf(dto.getCpf())) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "CPF já está em uso. Por favor, verifique os dados informados.");
        }
    }

    private void generatePasswordHash(UserCommomRegisterDto userCommomRegisterDto) {
        log.info("[start] AuthenticationApplicationService - generatePasswordHash");
        userCommomRegisterDto.updateHasedPassword(passwordEncoder.encode(userCommomRegisterDto.getPassword()));
        log.debug("[finish] AuthenticationApplicationService - generatePasswordHash");
    }

    @Override
    public AuthenticationResponseDto login(UserLoginDto userLoginDto) {
        log.info("[start] AuthenticationApplicationService - login");
        var usernamePassword = new UsernamePasswordAuthenticationToken(userLoginDto.getEmail().toLowerCase(), userLoginDto.getPassword());
        try {
            var auth = this.authenticationManager.authenticate(usernamePassword);
            var token = tokenService.generateTokenUser((User) auth.getPrincipal());
            checkLoginAttempts(LoginType.SUCCESS, userLoginDto.getEmail());
            log.debug("[finish] AuthenticationApplicationService - authenticate");
            return new AuthenticationResponseDto(TokenType.BEARER, LocalDateTime.now().plusHours(expiration), token);
        } catch (Exception e) {
            log.error("[error] AuthenticationApplicationService - authenticate - {}", e.getMessage());
            checkLoginAttempts(LoginType.FAILED, userLoginDto.getEmail());
            throw APIException.build(HttpStatus.FORBIDDEN, handleMessageError(userLoginDto.getEmail()));
        }
    }

    private String handleMessageError(String username) {
        return userRepository.findByEmail(username)
                .map(this::buildErrorMessageForUser)
                .orElse("Usuário não encontrado. Verifique e tente novamente.");
    }

    private String buildErrorMessageForUser(User user) {
        StatusMember status = user.getStatusMember();

        return switch (status) {
            case NOT_VERIFIED -> "Usuário ainda não foi verificado. Procure o código no seu email e faça a verificação.";
            case ACTIVE -> "Usuário ou senha inválidos. Verifique e tente novamente.";
            case BLOCKED -> "Usuário bloqueado por excesso de tentativas. Faça a recuperação da conta.";
            default -> "Status do usuário inválido ou desconhecido.";
        };
    }

    private void checkLoginAttempts(LoginType loginType, String email) {
        log.info("[start] AuthenticationApplicationService - checkLoginAttempts");
        userRepository.findByEmail(email).ifPresent(user -> {
                    user.checkLoginType(loginType);
                    userRepository.save(user);
                });
        log.debug("[finish] AuthenticationApplicationService - checkLoginAttempts");
    }
}
