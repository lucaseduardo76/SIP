package com.ifba.sipapi.user.api.authentication.service;

import com.ifba.sipapi.config.security.TokenService;
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
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.token.jwt.expiration}")
    private Long expiration;

    @Override
    public void createNewUser(UserCommomRegisterDto userCommomRegisterDto) {
        log.info("[start] AuthenticationApplicationService - createNewUser");
        generatePasswordHash(userCommomRegisterDto);
        userRepository.save(new User(userCommomRegisterDto));
        log.debug("[finish] AuthenticationApplicationService - createNewUser");
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
            System.out.println("Entrei aqui");
            var auth = this.authenticationManager.authenticate(usernamePassword);
            System.out.println("Entrei aqui 2");
            var token = tokenService.generateToken((User) auth.getPrincipal());
            System.out.println("Entrei aqui 2");
            log.debug("[finish] AuthenticationApplicationService - authenticate");
            return new AuthenticationResponseDto(TokenType.BEARER, LocalDateTime.now().plusHours(expiration), token);
        } catch (Exception e) {
            log.error("[error] AuthenticationApplicationService - authenticate - {}", e.getMessage());
//            throw APIException.build(HttpStatus.FORBIDDEN, mensagemPorTipoErro(usuarioAdmLoginDto.getUsername()));
            throw new RuntimeException("AGUARDANDO EXCEPTIONS - AUTHENTICATIONSERVICE");
        }
    }

    private String mensagemPorTipoErro(String username) {
        return userRepository.findByEmail(username)
                .map(usuario -> {
                    if (usuario.getStatusMember() == StatusMember.ACTIVE) {
                        return "Usuário ou senha inválidos. Verifique e tente novamente.";
                    }
                    return "Usuário bloqueado por excesso de tentativas. Entre em contato com o suporte.";
                })
                .orElse("Usuário não encontrado. Verifique e tente novamente.");
    }
}
