package com.example.demo.repository;

import com.example.demo.dao.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query("{ 'email' : { $regex: ?0, $options: 'i' } }")
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}




