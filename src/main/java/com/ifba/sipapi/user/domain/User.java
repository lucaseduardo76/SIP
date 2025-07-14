package com.ifba.sipapi.user.domain;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.user.dto.UserCommomRegisterDto;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@EqualsAndHashCode
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

    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private StatusMember statusMember;

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
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }


}
