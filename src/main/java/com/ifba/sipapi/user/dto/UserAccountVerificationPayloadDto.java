package com.ifba.sipapi.user.dto;

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

    @NotBlank(message = "O email é obrigatório")
    @Email
    private String email;

    @NotBlank(message = "O código de verificação é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "O código de verificação deve conter exatamente 6 dígitos")
    private String verificationCode;
}