package com.ifba.sipapi.user.dto;

import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import lombok.*;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponseDto {
    private String name;
    private String cpf;
    private String email;
    private Role role;
    private StatusMember statusMember;
    private String phone;

    public UserDetailsResponseDto(User user) {
        this.name = user.getName();
        this.cpf = user.getCpf();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.statusMember = user.getStatusMember();
        this.phone = user.getPhone();
    }
}