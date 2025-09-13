package com.jordi.booknook.serviceTests;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookReviewEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.payload.request.NewReviewRequest;
import com.jordi.booknook.payload.request.UpdateReviewRequest;
import com.jordi.booknook.payload.response.NewReviewResponse;
import com.jordi.booknook.payload.response.UpdateReviewResponse;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.repositories.BookReviewRepository;
import com.jordi.booknook.repositories.UserRepository;
import com.jordi.booknook.security.CurrentUserResolver;
import com.jordi.booknook.security.UserDetailsImplementation;
import com.jordi.booknook.services.BookReviewService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookReviewServiceTest {
    BookReviewService service;
    static UserEntity user1;
    static BookEntity book1;
    static BookReviewEntity review;

    @Mock
    BookReviewRepository reviewRepository;
    @Mock
    BookRepository bookRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CurrentUserResolver currentUserResolver;
    @Mock
    Authentication auth;
    @Mock
    UserDetailsImplementation userDetails;

    @BeforeAll
    static void setUpCommonEntities() {
        // Create common entities that can be reused in multiple tests.
        LocalDateTime date = LocalDateTime.now();
        BigDecimal price = new BigDecimal("12.50");

        user1 = new UserEntity("jordi", "jordi@email.com", "password");

        book1 = new BookEntity("cover1", "title1", "description1",
                price, date, date);
        book1.setBook_id(1L);

        review = new BookReviewEntity(book1, user1,5,"El mejor libro de la historia.");
        review.setBook_reviews_id(2L);
    }

    @BeforeEach
    void setUp(){
        this.service = new BookReviewService(reviewRepository,bookRepository,userRepository,currentUserResolver );
    }

    @Test
    void getReviewsByBookShouldReturnAll(){
        // Given: A list of 3 reviews assigned to the book1 entity.
        BookReviewEntity review1 = new BookReviewEntity(book1,user1,5);
        BookReviewEntity review2 = new BookReviewEntity(book1,user1,2);
        BookReviewEntity review3 = new BookReviewEntity(book1,user1,3);

        when(bookRepository.findById(book1.getBook_id())).thenReturn(Optional.of(book1));

        when(reviewRepository.findBookReviewEntitiesByBook(book1))
                .thenReturn(List.of(review1,review2,review3));

        // When: The service method getReviewsByBook is called with the book1 entity id.
        List<BookReviewEntity> reviews = service.getReviewsByBook(book1.getBook_id()).reviews();

        // Then: We get the exact 3 reviews we added for this test.
        assertThat(reviews).containsExactly(review1,review2,review3);
    }

    @Test
    void getReviewsByBookShouldReturnErrorWhenNoBookIsFound(){
        // Given: A non-existent book id.
        Long nonExistentId = 3L;

        // When: The service method getReviewsByBook is called with the non-existent book id.
        Executable action = () -> service.getReviewsByBook(nonExistentId);

        // Then: We assert that it throws a EntityNotFoundException.
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                action);

        // And: That the error message thrown by the exception is equal to the expected error message.
        String expectedErrorMessage = "Book not found.";
        assertEquals(expectedErrorMessage ,exception.getMessage());
    }

    @Test
    void getReviewsByUserShouldReturnTheCurrentUserReviews(){
        // Given: A logged user "jordi" with 2 assigned reviews.
        when(currentUserResolver.requireCurrentUser()).thenReturn(user1);
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        BookReviewEntity review1 = new BookReviewEntity(book1,user1,5);
        BookReviewEntity review2 = new BookReviewEntity(book1,user1,2);

        when(reviewRepository.findBookReviewEntitiesByUser(user1))
                .thenReturn(List.of(review1,review2));

        // When: The getReviewsByUser method is called with the current logged user.
        List<BookReviewEntity> reviews = service.getReviewsByUser().reviews();

        // Then: We assert that we get the exact 2 reviews that the current user has assigned.
        assertThat(reviews).containsExactly(review1,review2);
    }

    @Test
    void addReviewByUserShouldAddTheReviewsAndShouldReturn(){
        // Given: A request with a valid book and a review by a logged user.
        NewReviewRequest request = new NewReviewRequest(book1.getBook_id(), 3,"Not bad.");

        when(bookRepository.findById(book1.getBook_id())).thenReturn(Optional.of(book1));

        when(currentUserResolver.requireCurrentUser()).thenReturn(user1);
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        BookReviewEntity review1 = new BookReviewEntity(book1,user1,request.rating(),request.review());
        review1.setBook_reviews_id(1L);

        when(reviewRepository.saveAndFlush(any(BookReviewEntity.class))).thenReturn(review1);

        // When: We call the addReviewByUser with the valid request and the mocked new review1.
        NewReviewResponse response = service.addReviewByUser(request);


       // Then: We assert that the response we get back is the same as the expectedResponse.
        NewReviewResponse expectedResponse = new NewReviewResponse(
                review1.getBook_reviews_id(),
                review1.getBook().getBook_id(),
                review1.getBook().getTitle(),
                review1.getRating(),
                review1.getReview());

       assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void addReviewByUserShouldReturnErrorWhenNoBookIsFound(){
        // Given: A bad request with a non-existent book with a logged user.
        Long nonExistentId = 3L;

        NewReviewRequest noBookReviewRequest = new NewReviewRequest(nonExistentId,4,"Great");

        when(currentUserResolver.requireCurrentUser()).thenReturn(user1);
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        // When: The service method addReviewsByUser is called with the bad request.
        Executable action = () -> service.addReviewByUser(noBookReviewRequest);

        // Then: We assert that it throws a EntityNotFoundException.
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                action);

        // And: That the error message thrown by the exception is equal to the expected error message.
        String expectedErrorMessage = "Book not found.";
        assertEquals(expectedErrorMessage ,exception.getMessage());
    }

    @Test
    void updateReviewShouldUpdateTheReviewAndShouldReturn() {
        // Given: A request with a valid review that has both rating and review by the owner of that review.
        when(currentUserResolver.requireCurrentUser()).thenReturn(user1);
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(reviewRepository.findById(2L))
                .thenReturn(Optional.of(review));

        UpdateReviewRequest request = new UpdateReviewRequest(3, "No esta mal.");
        review.setRating(request.rating());
        review.setReview(request.review());

        when(reviewRepository.save(review))
                .thenReturn(review);

        // When: We call the updateReviewById with a valid review id and a valid request.
        UpdateReviewResponse response = service.updateReviewById(
                review.getBook_reviews_id(),
                request);

        // Then: We assert that the response we get back is the same as the expectedResponse.
        UpdateReviewResponse expectedResponse = new UpdateReviewResponse(
                review.getBook_reviews_id(),
                review.getBook().getBook_id(),
                review.getBook().getTitle(),
                review.getRating(),
                review.getReview());

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void updateReviewShouldReturnErrorWhenReviewIsNotFound(){
        // Given: A bad request with an invalid review id and a logged user.
        Long nonExistentId = 3L;

        UpdateReviewRequest request = new UpdateReviewRequest(3, "No esta mal.");

        when(currentUserResolver.requireCurrentUser()).thenReturn(user1);
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        // When: We call the updateReviewById with an invalid review id and a valid request.
        Executable action = () -> service.updateReviewById(nonExistentId, request);

        // Then: We assert that it throws a EntityNotFoundException.
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                action);

        // And: That the error message thrown by the exception is equal to the expected error message.
        String expectedErrorMessage = "Review not found.";
        assertEquals(expectedErrorMessage ,exception.getMessage());
    }

    @Test
    void updateReviewShouldReturnErrorWhenUserIsNotAllowed() {
        // Given: A valid request with a valid review but with a user that doesn't own that review.
        UpdateReviewRequest request = new UpdateReviewRequest(3, "No esta mal.");

        UserEntity nonAllowedUser = new UserEntity(
                "Tamara", "tamara@gmail.com", "asdasda");

        when(reviewRepository.findById(2L))
                .thenReturn(Optional.of(review));

        when(currentUserResolver.requireCurrentUser()).thenReturn(user1);
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(nonAllowedUser));

        SecurityContextHolder.getContext().setAuthentication(auth);

        /* When: We call the updateReviewById with a valid review id and a valid request,
           but a user that doesn't own that review.
         */
        Executable action = () -> service.updateReviewById(review.getBook_reviews_id(), request);

        // Then: We assert that it throws a AccessDeniedException.
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                action);

        // And: That the error message thrown by the exception is equal to the expected error message.
        String expectedErrorMessage = "Not allowed to update that Book review.";
        assertEquals(expectedErrorMessage ,exception.getMessage());
    }

    @Test
    void updateReviewShouldUpdateTheReviewWithOnlyARatingAndShouldReturn() {
        // Given: A request with a valid review that has only a rating by the owner of that review.
        when(currentUserResolver.requireCurrentUser()).thenReturn(user1);
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(reviewRepository.findById(2L))
                .thenReturn(Optional.of(review));

        UpdateReviewRequest request = new UpdateReviewRequest(4, null);


        when(reviewRepository.save(review))
                .thenReturn(review);

        BookReviewEntity initialReview = new BookReviewEntity(
                review.getBook(),
                review.getUser(),
                review.getRating(),
                review.getReview()
        );

        BookReviewEntity updatedBookReview = review;
        updatedBookReview.setRating(request.rating());

        // When: We call the updateReviewById with a valid review id and a valid request with only a rating.
        UpdateReviewResponse response = service.updateReviewById(
                updatedBookReview.getBook_reviews_id(),
                request);

        // Then: We assert that the response we get back is the same as the expectedResponse.
        UpdateReviewResponse expectedResponse = new UpdateReviewResponse(
                updatedBookReview.getBook_reviews_id(),
                updatedBookReview.getBook().getBook_id(),
                updatedBookReview.getBook().getTitle(),
                updatedBookReview.getRating(),
                updatedBookReview.getReview());

        assertThat(response).isEqualTo(expectedResponse);

        // And: That the review text has not changed comparing the initialReview with the updated one.
        assertThat(response.review()).isEqualTo(initialReview.getReview());
    }

    @Test
    void updateReviewShouldUpdateTheReviewWithOnlyAReviewTextAndShouldReturn() {
        // Given: A request with a valid review that has only a review text by the owner of that review.
        when(currentUserResolver.requireCurrentUser()).thenReturn(user1);
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(reviewRepository.findById(2L))
                .thenReturn(Optional.of(review));

        UpdateReviewRequest request = new UpdateReviewRequest(null, "No esta mal.");


        when(reviewRepository.save(review))
                .thenReturn(review);

        BookReviewEntity initialReview = new BookReviewEntity(
                review.getBook(),
                review.getUser(),
                review.getRating(),
                review.getReview()
        );

        BookReviewEntity updatedBookReview = review;
        updatedBookReview.setReview(request.review());

        /* When: We call the updateReviewById with a valid review id and a valid request
                 with only a review text.
         */
        UpdateReviewResponse response = service.updateReviewById(
                updatedBookReview.getBook_reviews_id(),
                request);

        // Then: We assert that the response we get back is the same as the expectedResponse.
        UpdateReviewResponse expectedResponse = new UpdateReviewResponse(
                updatedBookReview.getBook_reviews_id(),
                updatedBookReview.getBook().getBook_id(),
                updatedBookReview.getBook().getTitle(),
                updatedBookReview.getRating(),
                updatedBookReview.getReview());

        assertThat(response).isEqualTo(expectedResponse);

        // And: That the rating has not changed comparing the initialReview with the updated one.
        assertThat(response.rating()).isEqualTo(initialReview.getRating());
    }
}
