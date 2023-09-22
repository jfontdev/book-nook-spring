package com.jordi.booknook.controllers;

import com.jordi.booknook.payload.response.ReviewsByBookResponse;
import com.jordi.booknook.payload.response.ReviewsByUserResponse;
import com.jordi.booknook.services.BookReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/v1/reviews")
public class BookReviewController {
    private final BookReviewService bookReviewService;

    public BookReviewController(BookReviewService bookReviewService) {
        this.bookReviewService = bookReviewService;
    }

    @GetMapping("/{book_id}/get")
    public ResponseEntity<ReviewsByBookResponse> getReviewsByBook (@PathVariable Long book_id){
        ReviewsByBookResponse response = bookReviewService.getReviewsByBook(book_id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<ReviewsByUserResponse> getReviewsByUser (){
        ReviewsByUserResponse response = bookReviewService.getReviewsByUser();

        return ResponseEntity.ok(response);
    }
}
