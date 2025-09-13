package com.jordi.booknook.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jordi.booknook.events.UserEntityListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(UserEntityListener.class)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_auth_sub", columnNames = "auth_sub")
        },
        indexes = {
                @Index(name = "ix_users_auth_provider", columnList = "auth_provider"),
                @Index(name = "ix_users_auth_sub", columnList = "auth_sub")
        }
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    // ⬇️ now nullable because Entra users won't have it
    @Size(max = 120)
    @JsonIgnore
    private String password;

    // ⬇️ NEW: identity provider ("entra") and the stable subject from that provider
    @Column(name = "auth_provider", length = 32)
    private String authProvider; // e.g., "entra"

    @Column(name = "auth_sub", length = 128) // Entra object id (GUID) or JWT sub
    private String authSub;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    public UserEntity() {}

    public UserEntity(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Convenience ctor for Entra-linked profiles
    public static UserEntity fromEntra(String username, String email, String entraId) {
        UserEntity u = new UserEntity(username, email, null);
        u.setAuthProvider("entra");
        u.setAuthSub(entraId);
        return u;
    }

    // getters/setters …

    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getAuthSub() { return authSub; }
    public void setAuthSub(String authSub) { this.authSub = authSub; }

    public Set<RoleEntity> getRoles() { return roles; }
    public void setRoles(Set<RoleEntity> roles) { this.roles = roles; }
}
