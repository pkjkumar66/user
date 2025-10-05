package com.example.demo.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;

@Document(collection = "users")
@Data                   // generates getters, setters, toString, equals, hashCode
@Builder                // enables builder pattern
@NoArgsConstructor      // generates no-args constructor
@AllArgsConstructor     // generates all-args constructor
@CompoundIndexes({
        @CompoundIndex(name = "provider_providerId_idx", def = "{'provider': 1, 'providerId': 1}", unique = true, sparse = true)
})public class User implements UserDetails {

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String username;

    @Indexed(unique = true, sparse = true)
    private String email;

    private String password;   // hashed for local users only
    private AuthProvider provider;   // "local", "google", "github"
    private String providerId; // OAuth provider user ID
    private Instant createdAt;
    private Instant updatedAt;

    private Instant lastLogin;

    @Builder.Default
    private boolean isDeleted = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted;
    }


    public enum AuthProvider {
        LOCAL, GOOGLE, GITHUB
    }

}



