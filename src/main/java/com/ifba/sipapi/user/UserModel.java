package com.ifba.sipapi.user;

import com.ifba.sipapi.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "sip_user")
public class UserModel extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome não pode ser vazio")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "O CPF não pode estar vazio ou em branco")
    @Pattern(
            regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$",
            message = "O CPF deve estar no formato 000.000.000-00"
    )
    @Column(nullable = false, unique = true)
    private String cpf;

    @NotBlank(message = "O e-mail não pode estar vazio ou em branco")
    @Email(message = "Formato de e-mail inválido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull(message = "A função não pode ser nula")
    @Column(nullable = false)
    private Role role;

    @NotBlank(message = "A senha não pode ser vazia")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "A senha deve conter: letras maiúsculas, minúsculas, número e símbolo"
    )
    @Column(nullable = false)
    @Setter
    private String password;

    @Pattern(
            regexp = "^\\(\\d{2}\\) \\d{5}-\\d{4}$",
            message = "O número de telefone deve estar no formato (99) 99999-9999"
    )
    private String phone;
}
