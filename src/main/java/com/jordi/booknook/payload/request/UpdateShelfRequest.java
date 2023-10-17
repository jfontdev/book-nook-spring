package com.jordi.booknook.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateShelfRequest(
        String name,
        String image,
        String description,
        Boolean public_shelf
) {
}