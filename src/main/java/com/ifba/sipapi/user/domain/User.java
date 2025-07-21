package com.ifba.sipapi.user.domain;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.user.api.authentication.service.LoginType;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import com.ifba.sipapi.user.dto.UserUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.ifba.sipapi.util.GenerateNumber;

import java.util.*;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_user")
public class User extends Auditable implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMember statusMember;

    @Column(nullable = false)
    private Integer failedLoginAttempts;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    private String accountVerificationCode;

    @OneToMany(mappedBy = "owner")
    private List<Item> items;

    public User(UserCommomRegisterDto userCommomRegisterDto) {
        this.name = userCommomRegisterDto.getName();
        this.cpf = userCommomRegisterDto.getCpf();
        this.email = userCommomRegisterDto.getEmail();
        this.password = userCommomRegisterDto.getPassword();
        this.phone = userCommomRegisterDto.getPhone();
        this.role = Role.COMMOM;
        this.statusMember = StatusMember.NOT_VERIFIED;
        this.failedLoginAttempts = 0;
        this.accountVerificationCode = GenerateNumber.generateVerificationCode();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        switch (this.role) {
            case COMMOM:
                authorities.add(new SimpleGrantedAuthority("ROLE_COMMOM"));
                break;

            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_COMMOM"));
                break;

            case ROOT:
                authorities.add(new SimpleGrantedAuthority("ROLE_ROOT"));
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_COMMOM"));
                break;
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return this.statusMember == StatusMember.ACTIVE;
    }

    public void checkLoginType(LoginType loginType) {
        if (loginType == LoginType.FAILED) {
            incrementFailedLoginAttempts();
        } else {
            resetFailedLoginAttempts();
        }

        if (hasExceededLoginAttempts()) {
            blockUser();
        }
    }

    public void handleAccountVerification(String verificationCode) {
        checkCode(verificationCode);
        this.statusMember = StatusMember.ACTIVE;
        updateVerificationCode();
    }

    public void handleAccountReactivation(String verificationCode) {
        checkCode(verificationCode);
        this.statusMember = StatusMember.ACTIVE;
        updateVerificationCode();
    }

    public void updateUser(UserUpdateDto dto, String email) {
        this.name = applyIfFilled(dto.getName(), this.name);
        this.phone = applyIfFilled(dto.getPhone(), this.phone);
    }

    private String applyIfFilled(String newValue, String currentValue) {
        return isFilled(newValue) ? newValue : currentValue;
    }

    private boolean isFilled(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void checkCode(String verificationCode) {
        if (!verificationCode.equals(this.accountVerificationCode)) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Token Inválido ou expirado!");
        }
    }

    private void updateVerificationCode() {
        this.accountVerificationCode = GenerateNumber.generateVerificationCode();
    }

    private void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    private void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    private boolean hasExceededLoginAttempts() {
        return this.failedLoginAttempts >= 5;
    }

    private void blockUser() {
        this.statusMember = StatusMember.BLOCKED;
    }

}
