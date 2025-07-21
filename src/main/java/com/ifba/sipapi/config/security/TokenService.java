package com.ifba.sipapi.config.security;



import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.dto.UserAccountVerificationPayloadDto;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Log4j2
public class TokenService {
    private final ObjectMapper objectMapper;

    @Value("${security.token.jwt.secret}")
    private String secret;

    @Value("${security.token.jwt.expiration}")
    private Long expiration;

    private final UserRepository userRepository;

    public String generateToken(User user) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("SIP")
                    .withSubject(user.getUsername())
                    .withExpiresAt(generateExpirationTime())
                    .sign(algorithm);
        }catch (JWTCreationException exception) {
            throw APIException.build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar token" + exception.getMessage());
        }
    }

    public String generateToken(UserAccountVerificationPayloadDto payload) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String subjectJson = objectMapper.writeValueAsString(payload);
            return JWT.create()
                    .withIssuer("SIP")
                    .withSubject(subjectJson)
                    .withExpiresAt(generateExpirationTime())
                    .sign(algorithm);
        }catch (JWTCreationException | JsonProcessingException exception) {
            throw APIException.build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar token" + exception.getMessage());
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("SIP")
                    .build()
                    .verify(token)
                    .getSubject();
        }catch (JWTVerificationException exception){
            log.error("Erro ao validar token: {}",  exception.getMessage());
            return null;
        }
    }

    private Instant generateExpirationTime() {
        return LocalDateTime.now().plusHours(expiration).toInstant(ZoneOffset.of("-03:00"));
    }

    public String getSubject(String token) {
        return validateToken(token.replace("Bearer ", ""));
    }

}
