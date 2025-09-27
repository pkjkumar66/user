package com.example.demo.controller;


import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // --- Traditional Signup ---
    @PostMapping("/signup")
    public ApiResponse signup(@RequestBody UserRequest req) {
        UserResponse signup = userService.signup(req);
        return ApiResponse.builder()
                .data(signup)
                .build();
    }

    // --- Traditional Login ---
    @PostMapping("/login")
    public ApiResponse login(@RequestBody UserRequest req) {
        UserResponse login = userService.login(req);
        return ApiResponse.builder()
                .data(login)
                .build();
    }

    // --- Logout (stateless JWT, just discard token at frontend) ---
    @PostMapping("/logout")
    public ApiResponse logout() {
        return ApiResponse.builder().build();
    }

    // --- SSO Login Redirect (Google) ---
    @GetMapping("/sso/login")
    public Map<String, String> ssoLogin() {
        return Map.of("url", "/oauth2/authorization/google");
    }

    // --- SSO Callback (handled by Spring Security automatically) ---
    @GetMapping("/sso/success")
    public Map<String, String> ssoSuccess(@RequestParam String token) {
        return Map.of("message", "SSO Login success", "token", token);
    }
}
