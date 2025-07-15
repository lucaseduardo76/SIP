package com.ifba.sipapi.user.api.authentication.controller;

import java.time.LocalDateTime;


public record AuthenticationResponseDto(TokenType type, LocalDateTime expiracao, String token) {
}
