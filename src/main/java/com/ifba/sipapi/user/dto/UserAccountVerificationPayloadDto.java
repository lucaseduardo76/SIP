package com.ifba.sipapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UserAccountVerificationPayloadDto {

    @Schema(example = "user@sip.edu.br")
    @NotBlank(message = "O email é obrigatório")
    @Email
    private String email;

    @Schema(example = "123456")
    @NotBlank(message = "O código de verificação é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "O código de verificação deve conter exatamente 6 dígitos")
    private String verificationCode;
}