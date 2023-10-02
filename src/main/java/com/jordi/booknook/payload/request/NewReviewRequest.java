package com.jordi.booknook.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NewReviewRequest(
        @NotNull Long book_id,
        @NotNull @Min(1) @Max(5) Integer rating,
        String review) {
}
