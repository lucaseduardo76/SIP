package com.ifba.sipapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UserLoginDto {

    @NotBlank(message = "Email não pode ser nulo")
    @Schema(example = "999999999999@ifba.edu.br")
    private String email;

    @NotBlank(message = "Senha não pode ser nulo")
    @Schema(example = "SenhaSegura@123")
    private String password;
}
