package com.ifba.sipapi.user.dto;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.ifba.sipapi.config.handler.APIException;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserDto implements UserRegisterDto {

    private String email;
    private String name;
    private String picture;
    private boolean emailVerified;

    private static final String IFBA_EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@ifba\\.edu\\.br$";

    public GoogleUserDto(GoogleIdToken.Payload payload) {
        this.email = handleEmailVerification(payload.getEmail());
        this.name = (String) payload.get("name");
        this.picture = (String) payload.get("picture");
        this.emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
    }

    private String handleEmailVerification(String email) {
        if (email == null || !email.matches(IFBA_EMAIL_REGEX))
            throw APIException.build(HttpStatus.BAD_REQUEST,"O e-mail deve pertencer ao domínio @ifba.edu.br");

        return email;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getCpf() {
        return null;
    }

    @Override
    public String getPhone() {
        return null;
    }

    @Override
    public void updateHashedPassword(String passwordHash) {
        //Not used for google authentication
    }
}
