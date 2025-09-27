package com.example.demo.dto;

import lombok.Data;

@Data
public class DeleteAccountRequest {
    private String password;  // optional: only for normal login users
    private String ssoToken;  // optional: only for SSO users (Google ID token)
}

