package com.jordi.booknook;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.UniversalSearch;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.services.BookService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    BookService service;
    static BookEntity book1;

    @Mock
    BookRepository bookRepository;

    @BeforeAll
    static void setUpCommonEntities(){
        // Create common entities that can be reused in multiple tests.
        BigDecimal price = new BigDecimal("12.50");
        LocalDateTime date = LocalDateTime.now();

        book1 = new BookEntity(
                "Portada","Nuevo libro", "Un gran libro",price,date,date);
        book1.setBook_id(5L);
    }

    @BeforeEach
    void setUp(){
        this.service =  new BookService(bookRepository);
    }

    @Test
    void searchShouldReturn() {
        // Given: A valid request with a search string.
        UniversalSearch search = new UniversalSearch("gran");

        when(bookRepository.findAll(any(Example.class)))
                .thenReturn(List.of(book1));

        // When: We call the search service method with the valid search.
        List<BookEntity> books = service.search(search);

        List<BookEntity> expectedBooks = List.of(book1);

        // Then: We assert that the list returned has exactly one book.
        assertThat(books).hasSize(1);

        // And: We assert that the list is equal to the expectedBooks list
        assertThat(books).isEqualTo(expectedBooks);
    }

    @Test
    void searchShouldReturnEmptyListWhenEmptyStringSearch() {
        // Given: A valid request with an empty string search value.
        UniversalSearch search = new UniversalSearch("");

        // When: We call the search service method with the valid empty string search.
        List<BookEntity> books = service.search(search);

        List<BookEntity> expectedEmptyList = List.of();

        // Then: We assert that the list returned has exactly zero books.
        assertThat(books).hasSize(0);

        // And: We assert that the list is equal to the expectedBooks list
        assertThat(books).isEqualTo(expectedEmptyList);
    }
}
