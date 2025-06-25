package com.ifba.sipapi.auth;

import com.ifba.sipapi.auth.DTO.UserRegisterRequest;
import com.ifba.sipapi.user.Role;
import com.ifba.sipapi.user.UserModel;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public static UserModel toUserModel(UserRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        return new UserModel(
                null,
                request.name(),
                request.cpf(),
                request.email(),
                request.role(),
                request.password(),
                request.phone(),
                null
        );
    }
}
