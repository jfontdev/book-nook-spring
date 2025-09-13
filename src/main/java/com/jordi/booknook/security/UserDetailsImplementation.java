package com.jordi.booknook.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jordi.booknook.models.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class UserDetailsImplementation implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final String subject;  // Entra stable ID (oid or sub)
    private final String username; // preferred_username or name
    private final String email;
    @JsonIgnore
    private final String password; // will always be null for Entra users
    private final Collection<? extends GrantedAuthority> authorities;

    private final Long localUserId; // Optional: from DB lookup

    public UserDetailsImplementation(
            String subject,
            String username,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Long localUserId
    ) {
        this.subject = subject;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.localUserId = localUserId;
    }

    // Build from our local DB user
    public static UserDetailsImplementation fromUserEntity(UserEntity user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImplementation(
                user.getAuthSub() != null ? user.getAuthSub() : user.getUser_id().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getUser_id()
        );
    }

    // Build from a JWT token (for private endpoints)
    public static UserDetailsImplementation fromJWT(Jwt jwt, Optional<UserEntity> localUser) {
        String oid = jwt.getClaimAsString("oid");
        String sub = jwt.getSubject();
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String name = jwt.getClaimAsString("name");
        String email = jwt.getClaimAsString("email");

        // Roles or scopes -> Spring authorities
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) roles = Collections.emptyList();

        String scp = jwt.getClaimAsString("scp");
        if (scp != null && !scp.isBlank()) {
            roles = new ArrayList<>(roles);
            roles.addAll(Arrays.asList(scp.split("\\s+")));
        }

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        Long localId = localUser.map(UserEntity::getUser_id).orElse(null);

        return new UserDetailsImplementation(
                oid != null ? oid : sub,
                preferredUsername != null ? preferredUsername : name,
                email,
                null, // no password for Entra users
                authorities,
                localId
        );
    }

    public Long getLocalUserId() {
        return localUserId;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImplementation user = (UserDetailsImplementation) o;
        return Objects.equals(subject, user.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject);
    }
}

