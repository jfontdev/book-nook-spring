package com.jordi.booknook.payload.response;

import com.jordi.booknook.models.BookReviewEntity;

import java.util.List;

public record ReviewsByUserResponse(
        List<BookReviewEntity> reviews
) {
}
