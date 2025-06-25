package com.ifba.sipapi.auth.services;

import com.ifba.sipapi.user.UserModel;
import com.ifba.sipapi.auth.UserPrincipal;
import com.ifba.sipapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SIPUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel user = userRepository.findByEmail(email);

        if (user.getPassword() == null) {
            throw new IllegalArgumentException("❌ user.getPassword() is null before encoding!");
        }

        System.out.println(user.getEmail());
        return new UserPrincipal(user);
    }
}
