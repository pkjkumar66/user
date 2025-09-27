package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String username;
    private String password;
    private String email;


    public boolean isValid() {
        // Check that either username or email is non-empty, and if email is provided, it contains "@"
        if ((username == null || username.isEmpty()) && (email == null || email.isEmpty() || !email.contains("@"))) {
            return false;
        }

        // Check password
        if (password == null || password.length() == 0) {
            return false;
        }

        return true;
    }
}

