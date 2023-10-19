package com.jordi.booknook;


import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookReviewEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.repositories.BookReviewRepository;
import com.jordi.booknook.repositories.UserRepository;
import com.jordi.booknook.services.BookReviewService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookReviewServiceTest {
    BookReviewService service;
    static UserEntity user1;
    static BookEntity book1;

    @Mock
    BookReviewRepository reviewRepository;
    @Mock
    BookRepository bookRepository;
    @Mock
    UserRepository userRepository;

    @BeforeAll
    static void setUpCommonEntities() {
        // Create common entities that can be reused in multiple tests.
        LocalDateTime date = LocalDateTime.now();
        BigDecimal price = new BigDecimal("12.50");

        user1 = new UserEntity("jordi", "jordi@email.com", "password");

        book1 = new BookEntity("cover1", "title1", "description1",
                price, date, date);
        book1.setBook_id(1L);
    }

    @BeforeEach
    void setUp(){
        this.service = new BookReviewService(reviewRepository,bookRepository,userRepository);
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
}
