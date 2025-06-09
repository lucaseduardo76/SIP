package com.ifba.sipapi.user.DTO;

import com.ifba.sipapi.user.Role;
import jakarta.validation.constraints.*;

public record UserRegisterRequest(

        @NotBlank(message = "O nome não pode ser vazio")
        String name,

        @NotBlank(message = "O CPF não pode estar vazio ou em branco")
        @Pattern(
                regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$",
                message = "O CPF deve estar no formato 000.000.000-00"
        )
        String cpf,

        @NotBlank(message = "O e-mail não pode estar vazio ou em branco")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A função não deve ser vazia")
        Role role,

        @NotBlank(message = "A senha não pode ser vazia")
        @Size(min = 8, message = "A senha tem que ter pelo menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "A senha deve conter: letras maiúsculas, minúsculas, número e símbolo"
        )
        String password,

        @Pattern(
                regexp = "^\\(\\d{2}\\) \\d{5}-\\d{4}$",
                message = "O número de telefone deve ser no formato (99) 99999-9999"
        )
        String phone

) {}
