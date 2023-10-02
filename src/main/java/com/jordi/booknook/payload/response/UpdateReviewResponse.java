package com.jordi.booknook.payload.response;

public record UpdateReviewResponse(
        Long book_reviews_id,
        Long book_id,
        String title,
        Integer rating,
        String review
) {
}
