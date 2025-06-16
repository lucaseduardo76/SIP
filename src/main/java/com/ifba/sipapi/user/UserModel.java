package com.ifba.sipapi.user;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.ItemModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "sip_user")
public class UserModel extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "O nome não pode ser vazio")
    private String name;

    @NotBlank(message = "O CPF não pode estar vazio ou em branco")
    @Pattern(
            regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$",
            message = "O CPF deve estar no formato 000.000.000-00"
    )
    @Column(nullable = false)
    private String cpf;

    @NotBlank(message = "O e-mail não pode estar vazio ou em branco")
    @Email(message = "Formato de e-mail inválido")
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "A função deve ser vazia")
    private Role role;

    @NotBlank(message = "A senha não pode ser vazia")
    @Size(min = 8, message = "A senha tem que ter pelo 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "A senha deve conter: letras maiúsculas, minusculas, número e simbolo"
    )
    private String password;

    @Pattern(
            regexp = "^\\(\\d{2}\\) \\d{5}-\\d{4}$",
            message = "O número de telefone deve ser no formato (99) 99999-9999"
    )
    private String phone;

    @OneToMany(mappedBy = "owner")
    private List<ItemModel> items;
}
