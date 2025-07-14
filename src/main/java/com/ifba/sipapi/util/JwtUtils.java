package com.ifba.sipapi.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Log4j2
@Component
public class JwtUtils {

    private final Algorithm algorithm;

    public JwtUtils(@Value("${security.token.jwt.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(String email) {
        log.info("[start] JwtUtils - generateToken");

        String token = JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(algorithm);

        log.debug("Generated token: {}", token);
        log.debug("[finish] JwtUtils - generateToken");
        return token;
    }

    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);

            log.info("Valid token for: {}", decodedJWT.getSubject());
            return decodedJWT;
        } catch (JWTVerificationException e) {
            log.error("Invalid token: {}", e.getMessage());
            throw new RuntimeException("Token verification failed", e);
        }
    }
}

