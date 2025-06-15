package com.ifba.sipapi.auth.services;

import com.ifba.sipapi.auth.DTO.UserRegisterRequest;
import com.ifba.sipapi.auth.AuthMapper;
import com.ifba.sipapi.user.UserModel;
import com.ifba.sipapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserModel save(UserRegisterRequest request) {
        UserModel user = AuthMapper.toUserModel(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        System.out.println(request.toString());
        System.out.println(user.toString());
        return userRepository.save(user);
    }

}
