package com.jordi.booknook.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateShelfRequest(
        @NotBlank String name,
        @NotBlank String image,
        @NotBlank String description,
        @NotNull Boolean public_shelf
) {
}