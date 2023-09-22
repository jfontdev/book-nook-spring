package com.jordi.booknook.repositories;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookReviewRepository extends JpaRepository<BookReviewEntity, Long> {
    List<BookReviewEntity> findBookReviewEntitiesByBook(BookEntity book);
}
