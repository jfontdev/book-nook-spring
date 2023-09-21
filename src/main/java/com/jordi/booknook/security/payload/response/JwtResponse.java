package com.jordi.booknook.security.payload.response;

import java.util.List;

public record JwtResponse(
        String Token,
        Long user_id,
        String type,
        String username,
        String email,
        List<String> roles
) {
    public JwtResponse(String Token, Long user_id, String username, String email, List<String> roles) {
        this(Token, user_id, "Bearer", username, email, roles);
    }
}
