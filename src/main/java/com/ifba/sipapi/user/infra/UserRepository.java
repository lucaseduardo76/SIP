package com.ifba.sipapi.user.infra;

import com.ifba.sipapi.user.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(@NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail inválido") String email);
    boolean existsByCpf(@NotBlank(message = "O CPF é obrigatório") @CPF String cpf);
}