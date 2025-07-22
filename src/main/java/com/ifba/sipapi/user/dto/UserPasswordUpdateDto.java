package com.ifba.sipapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordUpdateDto {
    @NotBlank(message = "Senha não pode ser nulo")
    @Schema(example = "SenhaSegura@123")
    private String password;

    @NotBlank(message = "A nova senha não pode ser nulo")
    @Schema(example = "SenhaSegura@123")
    private String newPassword;

    public void updateHashedPassword(String hashedPassword) {
        this.password = hashedPassword;
    }
}
