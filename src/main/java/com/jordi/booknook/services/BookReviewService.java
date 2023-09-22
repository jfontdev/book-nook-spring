package com.jordi.booknook.services;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookReviewEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.payload.response.ReviewsByBookResponse;
import com.jordi.booknook.payload.response.ReviewsByUserResponse;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.repositories.BookReviewRepository;
import com.jordi.booknook.repositories.UserRepository;
import com.jordi.booknook.security.UserDetailsImplementation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BookReviewService {
    private final BookReviewRepository bookReviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookReviewService(BookReviewRepository bookReviewRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.bookReviewRepository = bookReviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
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

    public ReviewsByUserResponse getReviewsByUser (){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());

        if (authenticatedUser.isPresent()){
            List<BookReviewEntity> reviews = bookReviewRepository.findBookReviewEntitiesByUser(authenticatedUser.get());
            return new ReviewsByUserResponse(reviews);
        }else {
            return new ReviewsByUserResponse( Collections.emptyList());
        }
    }
}
