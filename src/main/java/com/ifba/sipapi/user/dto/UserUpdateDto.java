package com.ifba.sipapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UserUpdateDto {

    @Schema(example = "73999999999")
    @Pattern(regexp = "\\d{11}", message = "O telefone deve conter exatamente 11 dígitos numéricos (DDD + número)")
    private String phone;

    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Schema(example = "João da Silva")
    private String name;
}
