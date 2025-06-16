package com.ifba.sipapi.user;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.ItemModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sip_user")
public class UserModel extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Setter
    private Role role;

    @Column(nullable = false)
    @Setter
    private String password;

    private String phone;

    @OneToMany(mappedBy = "owner")
    private List<ItemModel> items;
}
