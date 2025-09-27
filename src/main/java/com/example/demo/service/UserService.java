package com.example.demo.service;

import com.example.demo.dao.User;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse signup(UserRequest request) {
        User user = addUser(request);
        return UserResponse.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .build();
    }

    public UserResponse login(UserRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail())
                .filter(u -> encoder.matches(request.getPassword(), u.getPassword()));

        return UserResponse.builder()
                .username(userOptional.get().getUsername())
                .password(userOptional.get().getPassword())
                .email(userOptional.get().getEmail())
                .build();
    }


    // GET all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    // GET user by ID
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }


    // ADD new user
    public User addUser(UserRequest request) {
        if (!request.isValid()) {
            throw new RuntimeException("invalid request");
        }

        // Check for duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use: " + request.getEmail());
        }


        // Check for duplicate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already in use: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();


        return userRepository.save(user);
    }


    // UPDATE user
    public User updateUser(String id, UserRequest updatedUser) {
        if (!updatedUser.isValid()) {
            throw new RuntimeException("invalid request");
        }

        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setPassword(updatedUser.getPassword());
                    user.setEmail(updatedUser.getEmail());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }


    // DELETE user
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}

