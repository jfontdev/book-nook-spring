package com.jordi.booknook.services;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookEntity> getBooks() {
        return bookRepository.findAll();
    }




}
