package com.jordi.booknook.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_images")
public class BookImagesEntity {
    @Id
    @GeneratedValue
    private Long book_images_id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookEntity book;

    private String media;

    @CreationTimestamp
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime updated_at;

    public BookImagesEntity(BookEntity book, String media, LocalDateTime created_at, LocalDateTime updated_at) {
        this.book_images_id = null;
        this.book = book;
        this.media = media;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    protected BookImagesEntity() {

    }

    public Long getBook_images_id() {
        return book_images_id;
    }

    public void setBook_images_id(Long book_images_id) {
        this.book_images_id = book_images_id;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
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
