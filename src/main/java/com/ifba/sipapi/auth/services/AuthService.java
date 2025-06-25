package com.ifba.sipapi.auth.services;

import com.ifba.sipapi.auth.DTO.UserLoginRequest;
import com.ifba.sipapi.auth.DTO.UserRegisterRequest;
import com.ifba.sipapi.auth.AuthMapper;
import com.ifba.sipapi.user.UserModel;
import com.ifba.sipapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public String save(UserRegisterRequest request) {
        UserModel user = AuthMapper.toUserModel(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Verifique seu e-mail para acessar a sua conta";
    }

    public String verify(UserLoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            return jwtService.generateToken(request.email());

        } catch (Exception e) {
            return "E-mail ou senha inválidos.";
        }
    }
}
