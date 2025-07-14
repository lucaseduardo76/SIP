package com.ifba.sipapi.user.api.authentication.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ifba.sipapi.config.security.TokenService;
import com.ifba.sipapi.user.api.authentication.controller.AuthenticationResponseDto;
import com.ifba.sipapi.user.api.authentication.controller.TokenType;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import com.ifba.sipapi.user.dto.UserLoginDto;
import com.ifba.sipapi.user.infra.UserRepository;
import com.ifba.sipapi.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationApplicationService implements AuthenticationService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${security.token.jwt.expiration}")
    private Long expiration;

    @Override
    public void createNewUser(UserCommomRegisterDto userCommomRegisterDto) {
        log.info("[start] AuthenticationApplicationService - createNewUser");
        generatePasswordHash(userCommomRegisterDto);
        userRepository.save(new User(userCommomRegisterDto));
        log.debug("[finish] AuthenticationApplicationService - createNewUser");
    }

    @Override
    public void verifyAccount(String token, String verificationCode) {
        log.info("[start] AuthenticationApplicationService - verifyAccount");

        DecodedJWT jwt = jwtUtils.verifyToken(token);
        String email = jwt.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

//        if (!verificationCode.equals(user.getVerificationCode())) {
//            throw new RuntimeException("Código de verificação inválido");
//        }
//
//        user.setVerified(true);
//        userRepository.save(user);

        log.debug("[finish] AuthenticationApplicationService - verifyAccount");
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
            var token = tokenService.generateToken((User) auth.getPrincipal());
            checkLoginAttempts(LoginType.SUCCESS, userLoginDto.getEmail());
            log.debug("[finish] AuthenticationApplicationService - authenticate");
            return new AuthenticationResponseDto(TokenType.BEARER, LocalDateTime.now().plusHours(expiration), token);
        } catch (Exception e) {
            log.error("[error] AuthenticationApplicationService - authenticate - {}", e.getMessage());
//            throw APIException.build(HttpStatus.FORBIDDEN, mensagemPorTipoErro(userLoginDto.getEmail()));
            checkLoginAttempts(LoginType.FAILED, userLoginDto.getEmail());
            throw new RuntimeException("AGUARDANDO EXCEPTIONS - AUTHENTICATIONSERVICE");
        }
    }

    private String mensagemPorTipoErro(String username) {
        return userRepository.findByEmail(username)
                .map(usuario -> {
                    if(usuario.getStatusMember() == StatusMember.NOT_VERIFIED){
                        return "Usuario ainda não foi verificado, procure o codigo no seu email e faça a verificação.";
                    }else if (usuario.getStatusMember() == StatusMember.ACTIVE) {
                        return "Usuário ou senha inválidos. Verifique e tente novamente.";
                    }
                    return "Usuário bloqueado por excesso de tentativas. Faça a recuperação da conta.";
                })
                .orElse("Usuário não encontrado. Verifique e tente novamente.");
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
