package com.ifba.sipapi.user.dto;

import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponseDto {
    @Schema(example = "Joao Silva")
    private String name;
    @Schema(example = "17421413073")
    private String cpf;
    @Schema(example = "joao.silva@example.com")
    private String email;
    @Schema(example = "COMMON")
    private Role role;
    @Schema(example = "NOT_VERIFIED")
    private StatusMember statusMember;
    @Schema(example = "71999998888")
    private String phone;
    @Schema(example = "sip.edu.br/3b766f4e-54a8-4065-93bf-6602b9d64e4b")
    private String profileImageUrl;

    public UserDetailsResponseDto(User user) {
        this.name = user.getName();
        this.cpf = user.getCpf();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.statusMember = user.getStatusMember();
        this.phone = user.getPhone();
        this.profileImageUrl = user.getProfileImageUrl();
    }
}