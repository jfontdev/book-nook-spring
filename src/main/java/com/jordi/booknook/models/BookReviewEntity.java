package com.jordi.booknook.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Entity
@Table(name = "book_reviews")
public class BookReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long book_reviews_id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    @JsonIgnore
    private BookEntity book;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;

    @Min(1)
    @Max(5)
    @NotNull
    private Integer rating;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String review;

    @CreationTimestamp
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime updated_at;

    public BookReviewEntity() {
    }

    public BookReviewEntity(BookEntity book, UserEntity user, Integer rating) {
        this.book = book;
        this.user = user;
        this.rating = rating;
    }

    public BookReviewEntity(BookEntity book, UserEntity user, Integer rating, String review) {
        this.book = book;
        this.user = user;
        this.rating = rating;
        this.review = review;
    }

    public Long getBook_reviews_id() {
        return book_reviews_id;
    }

    public void setBook_reviews_id(Long book_reviews_id) {
        this.book_reviews_id = book_reviews_id;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
