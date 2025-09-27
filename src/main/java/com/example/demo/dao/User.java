package com.example.demo.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
@Data                   // generates getters, setters, toString, equals, hashCode
@Builder                // enables builder pattern
@NoArgsConstructor      // generates no-args constructor
@AllArgsConstructor     // generates all-args constructor
public class User {


    @Id
    private String id;// MongoDB _id
    private String username;
    private String password;
    private String email;


    @CreatedDate
    private Instant createdAt;


    @LastModifiedDate
    private Instant lastLogin;


    // @Builder.Default
    // private LocalDateTime createdAt = LocalDateTime.now(); // default value when building
}



