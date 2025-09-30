package com.example.demo.service;

import com.example.demo.config.security.GoogleTokenVerifier;
import com.example.demo.dao.User;
import com.example.demo.dto.DeleteAccountRequest;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final GoogleTokenVerifier googleTokenVerifier;

    public UserResponse signup(UserRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid request");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User savedUser = userRepository.save(User.builder()
                .username(request.getUsername())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider("local")
                .build());

        String token = jwtUtil.generateToken(savedUser);
        return buildUserResponse(savedUser, token);
    }


    public UserResponse login(UserRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user);
        updateLastLogin(user.getUsername());

        return buildUserResponse(user, token);
    }

    public UserResponse oauthLogin(String provider, String providerId, String email) {
        Optional<User> userOpt = userRepository.findByProviderAndProviderId(provider, providerId);

        User user = userOpt.orElseGet(() -> {
            String normalizedEmail = email != null ? email.toLowerCase() : null;
            String username = null;
            if (normalizedEmail != null) {
                String base = normalizedEmail.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
                String candidate = base;
                int i = 0;
                while (candidate.isBlank() || userRepository.existsByUsername(candidate)) {
                    i++;
                    candidate = base + i;
                }
                username = candidate;
            }
            User newUser = User.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .email(normalizedEmail)
                    .username(username)
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtUtil.generateToken(user);
        // Prefer update by id for oauth users
        if (user.getId() != null) updateLastLoginById(user.getId());
        return buildUserResponse(user, token);
    }

    public void updateLastLoginById(String id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setLastLogin(Instant.now());
            userRepository.save(u);
        });
    }




    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void updateLastLogin(String username) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(Instant.now());
            userRepository.save(user);
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserResponse buildUserResponse(User user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .token(token)
                .build();
    }

    public boolean deleteMyAccount(String username, DeleteAccountRequest req) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            // Normal user → verify password
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                return false;
            }
        } else {
            // SSO user → verify Google token
            if (req.getSsoToken() == null || !googleTokenVerifier.verify(req.getSsoToken(), user.getEmail())) {
                return false;
            }
        }

        userRepository.delete(user);
        return true;
    }
}