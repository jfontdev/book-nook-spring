package com.jordi.booknook.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NewShelfRequest(
        @NotBlank(message = "Name is required.") String name,
        @NotBlank(message = "Image URL is required.") String image,
        @NotBlank(message = "Description is required.") String description,
        @NotNull(message = "Public shelf is required.") Boolean public_shelf
) {
}