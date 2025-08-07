package com.ifba.sipapi.user.dto;

import com.ifba.sipapi.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisterDto {
    private String email;
    private String cpf;
    private String password;
    private Role role;

    public UserRegisterDto(UserCommomRegisterDto userCommomRegisterDto) {
        this(
                userCommomRegisterDto.getEmail(),
                userCommomRegisterDto.getCpf(),
                userCommomRegisterDto.getPassword(),
                Role.COMMOM
        );
    }

    public UserRegisterDto(UserAdminRegisterDto userAdminRegisterDto) {
        this(
                userAdminRegisterDto.getEmail(),
                userAdminRegisterDto.getCpf(),
                userAdminRegisterDto.getPassword(),
                Role.ADMIN
        );
    }

    public void updateHashedPassword(String hashedPassword) {
        this.password = hashedPassword;
    }
}
