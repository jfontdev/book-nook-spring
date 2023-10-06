package com.jordi.booknook.repositories;

import com.jordi.booknook.models.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Comparator;
import java.util.List;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

    default List<BookEntity> findAllSorted(String sortBy) {
        List<BookEntity> books = findAll();

        return switch (sortBy) {
            case "priceAsc" -> books.stream()
                    .sorted((Comparator.comparing(BookEntity::getPrice)))
                    .toList();
            case "priceDesc" -> books.stream()
                    .sorted((Comparator.comparing(BookEntity::getPrice).reversed()))
                    .toList();
            case "ratingsAsc" -> books.stream()
                    .sorted((Comparator.comparing(BookEntity::getAverageRating)))
                    .toList();
            case "ratingsDesc" -> books.stream()
                    .sorted((Comparator.comparing(BookEntity::getAverageRating).reversed()))
                    .toList();
            default -> books;
        };
    }
}
