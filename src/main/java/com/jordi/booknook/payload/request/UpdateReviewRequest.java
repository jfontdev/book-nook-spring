package com.jordi.booknook.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateReviewRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
                                  String review) {
}
