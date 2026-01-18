package com.ifba.sipapi.user.dto;

import com.ifba.sipapi.user.domain.Role;
import com.ifba.sipapi.user.domain.StatusMember;
import com.ifba.sipapi.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @Schema(example = "/profimage/6359de79-1934-4e6c-96cd-ca080bd80ed2_profile.jpg")
    private String profileImageUrl;
    @Schema(example = "04-29-2001T21:45:12.345")
    private LocalDateTime registrationDate;

    public UserDetailsResponseDto(User user) {
        this.name = user.getName();
        this.cpf = user.getCpf();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.statusMember = user.getStatusMember();
        this.phone = user.getPhone();
        this.profileImageUrl = user.getProfileImageUrl();
        this. registrationDate = user.getCreatedAt();
    }
}