package com.jordi.booknook.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookReviewEntity;

import java.util.List;

public record ReviewsByBookResponse(
        BookEntity book,
        List<BookReviewEntity> reviews
) {
}
