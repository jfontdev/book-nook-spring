package com.jordi.booknook.services;

import com.jordi.booknook.models.BookEntity;

import com.jordi.booknook.payload.request.SortRequest;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.models.UniversalSearch;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookEntity> getBooks() {
        return bookRepository.findAll();
    }

    public Optional<BookEntity> getBookById(Long book_id) {
        return bookRepository.findById(book_id);
    }

    public List<BookEntity> search(UniversalSearch search){
        BookEntity probe = new BookEntity();

        if (StringUtils.hasText(search.value())){
            probe.setTitle(search.value());
            probe.setDescription(search.value());
        }else {
            return List.of();
        }

        Example<BookEntity> example = Example.of(probe, ExampleMatcher
                .matchingAny()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        return bookRepository.findAll(example);
    }

    public List<BookEntity> getAllBooksSortedByPriceOrReview(SortRequest sortRequest){
        String sortBy = sortRequest.sortBy();

        return bookRepository.findAllSorted(sortBy);
    }
}
