package com.ifba.sipapi.user.dto;

public interface UserRegisterDto {
    void updateHashedPassword(String hashedPassword);
    String getPassword();
    String getEmail();
    String getCpf();
    String getName();
    String getPhone();
}
