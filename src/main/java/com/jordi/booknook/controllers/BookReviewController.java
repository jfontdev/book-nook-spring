package com.jordi.booknook.controllers;

import com.jordi.booknook.payload.request.NewReviewRequest;
import com.jordi.booknook.payload.request.UpdateReviewRequest;
import com.jordi.booknook.payload.response.NewReviewResponse;
import com.jordi.booknook.payload.response.ReviewsByBookResponse;
import com.jordi.booknook.payload.response.ReviewsByUserResponse;
import com.jordi.booknook.payload.response.UpdateReviewResponse;
import com.jordi.booknook.services.BookReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @PostMapping("")
    public ResponseEntity<NewReviewResponse> addReviewByUser(@Valid @RequestBody NewReviewRequest newReview){
        NewReviewResponse response = bookReviewService.addReviewByUser(newReview);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{book_reviews_id}")
    public ResponseEntity<UpdateReviewResponse> updateReviewById(@PathVariable Long book_reviews_id,
                                                                 @Valid @RequestBody UpdateReviewRequest updatedReview){
        UpdateReviewResponse response = bookReviewService.updateReviewById(book_reviews_id,updatedReview);

        return ResponseEntity.ok(response);
    }

    // TODO: Abstract Error handling to a general Error handler.
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>("Review not found", HttpStatus.NOT_FOUND);
    }
}
