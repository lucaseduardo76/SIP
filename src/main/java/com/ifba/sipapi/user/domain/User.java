package com.ifba.sipapi.user.domain;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.user.api.authentication.service.LoginType;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
