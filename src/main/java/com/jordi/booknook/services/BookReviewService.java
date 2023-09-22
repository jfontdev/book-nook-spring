package com.jordi.booknook.services;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.repositories.BookReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookReviewService {
    private final BookReviewRepository bookReviewRepository;

    public BookReviewService(BookReviewRepository bookReviewRepository) {
        this.bookReviewRepository = bookReviewRepository;
    }

    public List<BookEntity> getReviewsByBook (BookEntity book){
        return bookReviewRepository.findBookReviewEntitiesByBook(book.getBook_id());
    }


}
