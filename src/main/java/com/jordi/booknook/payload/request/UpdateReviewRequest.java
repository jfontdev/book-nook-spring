package com.jordi.booknook.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateReviewRequest(
        @Min(value = 1, message = "Minimum value is 1.")
        @Max(value = 5, message = "Must be less than or equal to 5.")
        Integer rating,
        String review
) {
}
