package com.ifba.sipapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountDeletionDto {
    @NotBlank(message = "A senha atual é obrigatória para excluir a conta")
    @Schema(example = "SenhaSegura@123")
    private String password;
}
