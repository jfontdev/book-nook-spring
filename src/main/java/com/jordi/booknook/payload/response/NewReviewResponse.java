package com.jordi.booknook.payload.response;


public record NewReviewResponse(
        Long book_reviews_id,
        Long book_id,
        String title,
        Integer rating,
        String review) {
}
