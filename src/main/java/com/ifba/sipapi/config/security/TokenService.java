package com.ifba.sipapi.config.security;



import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ifba.sipapi.user.domain.User;
import com.ifba.sipapi.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Log4j2
public class TokenService {
    @Value("${security.token.jwt.secret}")
    private String secret;

    @Value("${security.token.jwt.expiration}")
    private Long expiration;



    private final UserRepository userRepository;

    public String generateToken(User user) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("wakanda-ai")
                    .withSubject(user.getUsername())
                    .withExpiresAt(genarateExpirationTime())
                    .sign(algorithm);
        }catch (JWTCreationException exception) {
//            throw APIException.build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar token" + exception.getMessage());
            log.error("Aguardando implementação exception  - TOKENSERVICE");
            throw new RuntimeException("Aguardando implementação exception  - TOKENSERVICE");
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("wakanda-ai")
                    .build()
                    .verify(token)
                    .getSubject();
        }catch (JWTVerificationException exception){
            log.error("Erro ao validar token: " + exception.getMessage());
            return null;
        }
    }

    private Instant genarateExpirationTime() {
        return LocalDateTime.now().plusHours(expiration).toInstant(ZoneOffset.of("-03:00"));
    }

}
