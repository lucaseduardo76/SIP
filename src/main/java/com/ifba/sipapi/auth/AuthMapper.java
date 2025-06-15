package com.ifba.sipapi.auth;

import com.ifba.sipapi.auth.DTO.UserRegisterRequest;
import com.ifba.sipapi.user.UserModel;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public static UserModel toUserModel(UserRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        UserModel user = new UserModel();
        user.setName(request.name());
        user.setCpf(request.cpf());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setPassword(request.password());
        user.setPhone(request.phone());

        return user;
    }
}
