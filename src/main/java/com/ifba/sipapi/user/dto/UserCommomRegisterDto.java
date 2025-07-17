package com.ifba.sipapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@EqualsAndHashCode
public class UserCommomRegisterDto {
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Schema(example = "João da Silva")
    private String name;

    @NotBlank(message = "O CPF é obrigatório")
    @CPF
    @Schema(example = "17421413073")
    private String cpf;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Schema(example = "joao.silva@example.com")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>\\[\\]\\\\/~`_+=;'\\-]).{8,}$",
            message = "A senha deve conter ao menos uma letra maiúscula, uma letra minúscula, um caractere especial e ter no mínimo 8 caracteres"
    )
    @Schema(example = "SenhaSegura@123")
    private String password;


    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter 10 ou 11 dígitos numéricos")
    @Schema(example = "71999998888")
    private String phone;

    public void updateHasedPassword(String hashedPassword) {
        this.password = hashedPassword;
    }
}
