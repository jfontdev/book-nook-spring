package com.jordi.booknook;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.UniversalSearch;
import com.jordi.booknook.payload.request.SortRequest;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
    void getAllBooksShouldReturn(){
        // Given: A valid request.
        BigDecimal price = new BigDecimal("12.50");
        LocalDateTime date = LocalDateTime.now();

        BookEntity book = new BookEntity(
                "Portada","Nuevo libro", "Un gran libro",price,date,date);
        BookEntity book2 = new BookEntity(
                "Portada 1","Nuevo libro 2", "Un gran libro 2",price,date,date);
        BookEntity book3 = new BookEntity(
                "Portada 2","Nuevo libro 3", "Un gran libro 3",price,date,date);

        when(bookRepository.findAll())
                .thenReturn(List.of(book,book2,book3));

        // When: We call the getBooks service method.
        List<BookEntity> books = service.getBooks();

        List<BookEntity> expectedBooks = List.of(book, book2, book3);

        // Then: We assert that the list returned has exactly three books.
        assertThat(books).hasSize(3);

        // And: We assert that the list is equal to the expectedBooks list
        assertThat(books).isEqualTo(expectedBooks);
    }

    @Test
    void getBookByIdShouldReturn(){
        // Given: A valid request with a valid book id.
        Long bookId = 5L;

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book1));

        // When: We call the getBookById service method with a valid book id.
        BookEntity book = service.getBookById(book1.getBook_id()).get();

        // Then: We assert that the book is equal to book1.
        assertThat(book).isEqualTo(book1);
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

    @Test
    void getAllBooksSortedByPriceOrReviewWithPriceDescShouldReturnAListWithBooksOrderedByPriceDesc(){
        /* Given: A valid request with sort param "PriceDesc" to get the list of books
                  ordered by Price descendent.
        */
        BigDecimal price = new BigDecimal("12.50");
        BigDecimal price2 = new BigDecimal("13.50");
        BigDecimal price3 = new BigDecimal("21.50");
        LocalDateTime date = LocalDateTime.now();

        BookEntity book = new BookEntity(
                "Portada","Nuevo libro", "Un gran libro",price,date,date);
        BookEntity book2 = new BookEntity(
                "Portada 1","Nuevo libro 2", "Un gran libro 2",price2,date,date);
        BookEntity book3 = new BookEntity(
                "Portada 2","Nuevo libro 3", "Un gran libro 3",price3,date,date);


        SortRequest request = new SortRequest("priceDesc");

        List<BookEntity> allBooks = List.of(book,book2,book3);

        // We mock the repository method call that filters the books and returns them ordered by Price descendent.
        when(bookRepository.findAllSorted(request.sortBy()))
                .thenAnswer( invocation -> {
                    return allBooks.stream()
                            .sorted((Comparator
                                    .comparing(BookEntity::getPrice)
                                    .reversed()))
                            .toList();
        });

        // When: We call the getAllBooksSortedByPriceOrReview service method with the valid request.
        List<BookEntity> books = service.getAllBooksSortedByPriceOrReview(request);

        List<BookEntity> expectedBooksOrder = List.of(book3,book2,book);

        // Then: We assert that the list returned has exactly 3 books.
        assertThat(books).hasSize(3);

        // And: We assert that the list is equal to the expectedBookOrder list.
        assertThat(books).isEqualTo(expectedBooksOrder);
    }
}
