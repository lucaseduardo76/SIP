package com.ifba.sipapi.config.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ifba.sipapi.user.dto.GoogleUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleTokenVerifier {


    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public GoogleUserDto verify(String idTokenString) {

        GoogleIdToken idToken;
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            ).setAudience(Collections.singletonList(googleClientId))
                    .build();

            idToken = verifier.verify(idTokenString);

        } catch (Exception e) {
            return null;
        }

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            return new GoogleUserDto(payload);
        }

        return null;
    }
}
