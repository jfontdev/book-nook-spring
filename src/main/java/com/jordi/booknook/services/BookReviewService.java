package com.jordi.booknook.services;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookReviewEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.payload.request.NewReviewRequest;
import com.jordi.booknook.payload.request.UpdateReviewRequest;
import com.jordi.booknook.payload.response.NewReviewResponse;
import com.jordi.booknook.payload.response.ReviewsByBookResponse;
import com.jordi.booknook.payload.response.ReviewsByUserResponse;
import com.jordi.booknook.payload.response.UpdateReviewResponse;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.repositories.BookReviewRepository;
import com.jordi.booknook.repositories.UserRepository;
import com.jordi.booknook.security.UserDetailsImplementation;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public ReviewsByBookResponse getReviewsByBook(Long book_id) {
        Optional<BookEntity> book = bookRepository.findById(book_id);

        if (book.isEmpty()){
            throw new EntityNotFoundException("Book not found.");
        }

        List<BookReviewEntity> reviews = bookReviewRepository.findBookReviewEntitiesByBook(book.get());

        return new ReviewsByBookResponse(book.get(), reviews);
    }

    public ReviewsByUserResponse getReviewsByUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());

        List<BookReviewEntity> reviews = bookReviewRepository.findBookReviewEntitiesByUser(authenticatedUser.get());
        return new ReviewsByUserResponse(reviews);
    }

    public NewReviewResponse addReviewByUser(NewReviewRequest newReview) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());
        Optional<BookEntity> book = bookRepository.findById(newReview.book_id());

        if (book.isEmpty()) {
            throw new EntityNotFoundException("Book not found.");
        }

        BookReviewEntity newBookReview = bookReviewRepository.saveAndFlush(
                new BookReviewEntity(book.get(), authenticatedUser.get(), newReview.rating(), newReview.review()));

        return new NewReviewResponse(newBookReview.getBook_reviews_id(),newBookReview.getBook().getBook_id(),newBookReview.getBook().getTitle(), newBookReview.getRating(), newBookReview.getReview());
    }

    public UpdateReviewResponse updateReviewById(Long book_reviews_id, UpdateReviewRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());
        Optional<BookReviewEntity> review = bookReviewRepository.findById(book_reviews_id);

        if (review.isEmpty()){
            throw new EntityNotFoundException("Review not found.");
        }

        BookReviewEntity updatedBookReview = review.get();

        if (updatedBookReview.getUser() != authenticatedUser.get()){
            throw new AccessDeniedException("Not allowed to update that Book review.");
        }

        if (request.rating() != null){
            updatedBookReview.setRating(request.rating());
        }
        if (request.review() != null){
            updatedBookReview.setReview(request.review());
        }

        bookReviewRepository.save(updatedBookReview);

        return new UpdateReviewResponse(
                updatedBookReview.getBook_reviews_id(),
                updatedBookReview.getBook().getBook_id(),
                updatedBookReview.getBook().getTitle(),
                updatedBookReview.getRating(),
                updatedBookReview.getReview()
            );
    }
}
