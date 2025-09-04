package com.ifba.sipapi.user.dto;

public interface UserRegisterDto {
    String getEmail();
    String getPassword();
    String getCpf();
    String getName();
    String getPhone();
    void updateHashedPassword(String passwordHash);
}
