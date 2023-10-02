package com.jordi.booknook.payload.request;


public record NewReviewResponse(
        Long book_reviews_id,
        Long book_id,
        String title,
        Integer rating,
        String review) {
}
