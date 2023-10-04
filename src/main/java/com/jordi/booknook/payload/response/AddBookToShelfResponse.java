package com.jordi.booknook.payload.response;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.ShelfEntity;

public record AddBookToShelfResponse(
        BookEntity book,
        ShelfEntity shelf
) {
}
