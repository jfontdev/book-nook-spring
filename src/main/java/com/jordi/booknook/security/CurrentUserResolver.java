package com.jordi.booknook.security;

import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserResolver {
    private final UserRepository users;

    public CurrentUserResolver(UserRepository users) { this.users = users; }

    public UserEntity requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jat) {
            var jwt = (org.springframework.security.oauth2.jwt.Jwt) jat.getPrincipal();
            String key = Optional.ofNullable(jwt.getClaimAsString("oid")).orElse(jwt.getSubject());
            return users.findByAuthSub(key)
                    .orElseThrow(() -> new IllegalStateException("Local profile not found for authSub=" + key));
        }

        if (auth.getPrincipal() instanceof UserDetailsImplementation udi) {
            return users.findByUsername(udi.getUsername())
                    .orElseThrow(() -> new IllegalStateException("Local profile not found for username=" + udi.getUsername()));
        }

        throw new IllegalStateException("Unsupported authentication principal: " + auth.getPrincipal());
    }
}
