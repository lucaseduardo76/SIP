package com.ifba.sipapi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UserLoginDto {
    @NotBlank(message = "Email não pode ser nulo")
    private String email;
    @NotBlank(message = "Senha não pode ser nulo")
    private String password;
}
