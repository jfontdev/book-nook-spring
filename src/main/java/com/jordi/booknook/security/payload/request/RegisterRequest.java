package com.jordi.booknook.security.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 20) String username,
        @NotBlank @Size(max = 50) @Email String email,
        Set<String> role,
        @NotBlank @Size(min = 6, max = 30) String password
) {
}
