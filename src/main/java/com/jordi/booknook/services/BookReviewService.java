package com.jordi.booknook.services;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookReviewEntity;
import com.jordi.booknook.payload.response.ReviewsByBookResponse;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.repositories.BookReviewRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BookReviewService {
    private final BookReviewRepository bookReviewRepository;
    private final BookRepository bookRepository;

    public BookReviewService(BookReviewRepository bookReviewRepository, BookRepository bookRepository) {
        this.bookReviewRepository = bookReviewRepository;
        this.bookRepository = bookRepository;
    }

    public ReviewsByBookResponse getReviewsByBook (Long book_id){
        Optional<BookEntity> book = bookRepository.findById(book_id);

        if (book.isPresent()){
            List<BookReviewEntity> reviews = bookReviewRepository.findBookReviewEntitiesByBook(book.get());
            return new ReviewsByBookResponse(book.get(),reviews);
        }else{
            return new ReviewsByBookResponse(null, Collections.emptyList());
        }
    }


}
