package com.ifba.sipapi.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ItemRecoveryRequestDto {

    @Schema(example = "ff2ce525-98f5-418e-84b0-b562ccbc4dba")
    @NotNull(message = "itemId não pode ser nulo")
    private UUID itemId;

    @Schema(example = "999999999999@ifba.edu.br")
    @Email(message = "Email deve ser válido")
    @NotBlank(message = "email não pode ser nulo")
    private String email;

    @Schema(example = "Descrição da solicitação")
    @NotBlank(message = "Descrição não pode ser nula")
    private String description;
}
