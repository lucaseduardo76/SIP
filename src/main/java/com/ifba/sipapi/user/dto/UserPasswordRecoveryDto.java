package com.ifba.sipapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UserPasswordRecoveryDto {

    @NotBlank(message = "O token é obrigatório.")
    @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ3YWthbmRhLWFpIiwic3ViIjoie1widG9cIjpcImNvbnRhdG9wZWRyb2x1Y2FzY2dAcHJvdG9uLm1lXCIsXCJjb2RlXCI6XCI3NDA0MzdcIn0iLCJleHAiOjE3NTI4ODI5NDJ9.GBTtzabOTJCHFvIJtxMGAvNI01xNcnZ2SGWRRKllQas")
    private String token;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>\\[\\]\\\\/~`_+=;'\\-]).{8,}$",
            message = "A senha deve conter ao menos uma letra maiúscula, uma letra minúscula, um caractere especial e ter no mínimo 8 caracteres"
    )
    @Schema(example = "SenhaSegura@123")
    private String password;
}
