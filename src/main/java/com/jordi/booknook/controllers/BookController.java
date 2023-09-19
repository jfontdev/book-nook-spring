package com.jordi.booknook.controllers;


import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.UniversalSearch;
import com.jordi.booknook.services.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/v1/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookEntity> getAllBooks(){
        return bookService.getBooks();
    }

    @GetMapping("/{book_id}/get")
    public ResponseEntity<BookEntity> getBook(@PathVariable Long book_id) {
        return bookService.getBookById(book_id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/search")
    public List<BookEntity> searchBooks(@RequestBody UniversalSearch search){
        return bookService.search(search);
    }
}
