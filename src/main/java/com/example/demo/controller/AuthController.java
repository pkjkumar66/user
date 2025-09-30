package com.example.demo.controller;

import com.example.demo.constant.ApiPaths;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.DeleteAccountRequest;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.AUTH_BASE)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // --- Traditional Signup ---
    @PostMapping(ApiPaths.SIGNUP)
    public ResponseEntity<ApiResponse<UserResponse>> signup(@RequestBody UserRequest req) {
        UserResponse signup = userService.signup(req);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .data(signup)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- Traditional Login ---
    @PostMapping(ApiPaths.LOGIN)
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody UserRequest req) {
        UserResponse login = userService.login(req);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .data(login)
                .build();
        return ResponseEntity.ok(response);
    }

    // --- Logout (stateless JWT, just discard token at frontend) ---
    @PostMapping(ApiPaths.LOGOUT)
    public ResponseEntity<ApiResponse<Void>> logout() {
        ApiResponse<Void> response = ApiResponse.<Void>builder().build();
        return ResponseEntity.ok(response);
    }

    // --- SSO Login Redirect (Google) ---
    @GetMapping(ApiPaths.SSO_LOGIN)
    public ResponseEntity<Map<String, String>> ssoLogin() {
        return ResponseEntity.ok(Map.of("url", ApiPaths.GOOGLE_AUTHORIZATION_URL));
    }

    // --- SSO Callback (handled by Spring Security automatically) ---
    @GetMapping(ApiPaths.SSO_SUCCESS)
    public ResponseEntity<Map<String, String>> ssoSuccess(@RequestParam String token) {
        return ResponseEntity.ok(Map.of(
                "message", "SSO Login success",
                "token", token
        ));
    }

    // --- Delete My Account (supports normal + SSO flow) ---
    @DeleteMapping(ApiPaths.DELETE_ME)
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(@RequestBody DeleteAccountRequest req,
                                                             Principal principal) {
        if (principal == null || principal.getName() == null) {
            ApiResponse<Void> unauthorized = ApiResponse.<Void>builder()
                    .errMessage("Unauthorized: missing authentication")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorized);
        }

        String username = principal.getName();
        boolean deleted = userService.deleteMyAccount(username, req);
        if (deleted) {
            ApiResponse<Void> response = ApiResponse.<Void>builder().build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .errMessage("Failed to delete account. Please re-authenticate.")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
