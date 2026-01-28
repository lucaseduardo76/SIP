package com.ifba.sipapi.user.domain;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.item.domain.recoveryRequest.Recovery;
import com.ifba.sipapi.notification.domain.Notification;
import com.ifba.sipapi.user.api.authentication.service.LoginType;
import com.ifba.sipapi.user.dto.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.ifba.sipapi.util.GenerateNumber;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
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

    private String password;

    private String phone;

    @Column(nullable = false)
    private String accountCode;

    private String profileImageUrl;

    @OneToMany(mappedBy = "owner")
    private List<Item> items;

    @OneToMany(mappedBy = "user")
    private List<Recovery> recoveries;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Notification> notifications;

    public User(UserCommomRegisterDto userCommomRegisterDto) {
        this.createBasicUser(userCommomRegisterDto);
        this.role = Role.COMMON;
    }

    public User(UserRootRegisterDto userRootRegisterDto) {
        this.createBasicUser(userRootRegisterDto);
        this.role = Role.ROOT;
        this.statusMember = StatusMember.ACTIVE;
    }

    public User(UserAdminRegisterDto userAdminRegisterDto) {
        this.createBasicUser(userAdminRegisterDto);
        this.role = Role.ADMIN;
    }

    private void createBasicUser(UserRegisterDto dto) {
        this.name = dto.getName();
        this.cpf = dto.getCpf();
        this.email = dto.getEmail();
        this.password = dto.getPassword();
        this.phone = dto.getPhone();
        this.failedLoginAttempts = 0;
        this.accountCode = GenerateNumber.generateCode();
        this.statusMember = StatusMember.NOT_VERIFIED;
        this.profileImageUrl = "";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        switch (this.role) {
            case COMMON:
                authorities.add(new SimpleGrantedAuthority("ROLE_COMMON"));
                break;

            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_COMMON"));
                break;

            case ROOT:
                authorities.add(new SimpleGrantedAuthority("ROLE_ROOT"));
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_COMMON"));
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
        if (loginType == LoginType.FAILED)
            incrementFailedLoginAttempts();
         else
            resetFailedLoginAttempts();

        if (hasExceededLoginAttempts())
            blockUser();
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

    public void updatePasswordRecoveryCode() { this.updateCode(GenerateNumber.generateCode()); }

    public void updateAccountReactivationCode() { this.updateCode(GenerateNumber.generateCode()); }

    public void resetPassword(String code, String password) {
        if(!code.equals(this.accountCode))
            throw APIException.build(HttpStatus.BAD_REQUEST, "Token Inválido ou malformado.");

        this.changePassword(password);
    }

    public void updateUser(UserUpdateDto dto) {
        this.name = applyIfFilled(dto.getName(), this.name);
        this.phone = applyIfFilled(dto.getPhone(), this.phone);
    }

    public void updatePassword(UserPasswordUpdateDto userPasswordUpdateDto, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(userPasswordUpdateDto.getPassword(), this.password))
            throw APIException.build(HttpStatus.BAD_REQUEST, "Senha atual informada inválida.");

        this.changePassword(userPasswordUpdateDto.getNewPassword());
    }

    public void updateProfileImage(String imageUrl) {
        if(!imageUrl.isEmpty())
            this.profileImageUrl = imageUrl;
    }

    public void requireAdminRole() {
        if(this.role != Role.ADMIN){
            throw APIException.build(HttpStatus.FORBIDDEN, "Usuario não possui permissão necessária!");
        }
    }

    private void checkCode(String verificationCode) {
        if (!verificationCode.equals(this.accountCode)) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Token Inválido ou expirado!");
        }
    }

    private String applyIfFilled(String newValue, String currentValue) { return isFilled(newValue) ? newValue : currentValue; }

    private boolean isFilled(String value) { return value != null && !value.trim().isEmpty(); }

    private void changePassword(String password) { this.password = password; }

    private void updateCode(String code) { this.accountCode = code; }

    private void updateVerificationCode() { this.updateCode(GenerateNumber.generateCode()); }

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
