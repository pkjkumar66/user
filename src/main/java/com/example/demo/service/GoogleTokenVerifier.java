package com.example.demo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier {

    public boolean verify(String idToken, String expectedEmail) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList("YOUR_GOOGLE_CLIENT_ID"))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken != null) {
                String email = googleIdToken.getPayload().getEmail();
                return expectedEmail.equalsIgnoreCase(email);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}

