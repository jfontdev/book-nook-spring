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
        UniversalSearch search = new UniversalSearch("gran");

        when(bookRepository.findAll(any(Example.class)))
                .thenReturn(List.of(book1));

        List<BookEntity> books = service.search(search);

        List<BookEntity> expectedBooks = List.of(book1);

        assertThat(books).hasSize(1);
        assertThat(books).isEqualTo(expectedBooks);
    }

    @Test
    void searchShouldReturnEmptyListWhenEmptyStringSearch() {
        UniversalSearch search = new UniversalSearch("");

        List<BookEntity> books = service.search(search);

        List<BookEntity> expectedEmptyList = List.of();

        assertThat(books).hasSize(0);
        assertThat(books).isEqualTo(expectedEmptyList);
    }
}
