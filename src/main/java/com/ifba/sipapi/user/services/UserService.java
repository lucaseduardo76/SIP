package com.ifba.sipapi.user.services;

import com.ifba.sipapi.user.DTO.UserRegisterRequest;
import com.ifba.sipapi.user.UserMapper;
import com.ifba.sipapi.user.UserModel;
import com.ifba.sipapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserModel save(UserRegisterRequest request) {
        UserModel user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

}
