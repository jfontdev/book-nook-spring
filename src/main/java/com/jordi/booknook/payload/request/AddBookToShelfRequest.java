package com.jordi.booknook.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddBookToShelfRequest(
        @NotNull @Min(value = 0, message = "Value must be a valid book Id number") Long book_id,
        @NotNull @Min(value = 0, message = "Value must be a valid shelf Id number") Long shelf_id
) {
}
