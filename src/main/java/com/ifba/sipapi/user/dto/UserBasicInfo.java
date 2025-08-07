package com.ifba.sipapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserBasicInfo {
    private String name;
    private String cpf;
    private String email;
    private String password;
    private String phone;

    public UserBasicInfo(UserCommomRegisterDto userCommomRegisterDto) {
        this(
                userCommomRegisterDto.getName(),
                userCommomRegisterDto.getCpf(),
                userCommomRegisterDto.getEmail(),
                userCommomRegisterDto.getPassword(),
                userCommomRegisterDto.getPhone()
        );
    }

    public UserBasicInfo(UserAdminRegisterDto userAdminRegisterDto) {
        this(
                userAdminRegisterDto.getName(),
                userAdminRegisterDto.getCpf(),
                userAdminRegisterDto.getEmail(),
                userAdminRegisterDto.getPassword(),
                userAdminRegisterDto.getPhone()
        );
    }

    public UserBasicInfo(UserRootRegisterDto userRootRegisterDto) {
        this(
                userRootRegisterDto.getName(),
                userRootRegisterDto.getCpf(),
                userRootRegisterDto.getEmail(),
                userRootRegisterDto.getPassword(),
                userRootRegisterDto.getPhone()
        );
    }
}
