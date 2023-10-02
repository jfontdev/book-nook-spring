package com.jordi.booknook.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shelves")
public class ShelfEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shelf_id;

    @ManyToMany
    @JoinTable(
            name = "book_shelf",
            joinColumns = @JoinColumn(name = "shelf_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private Set<BookEntity> books = new HashSet<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;

    @NotBlank
    private String name;

    @NotBlank
    private String image;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @NotBlank
    private String description;

    @NotNull
    private Boolean public_shelf;

    @CreationTimestamp
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime updated_at;

    public ShelfEntity(){
    };

    public ShelfEntity(UserEntity user, String name, String image, String description, Boolean public_shelf) {
        this.user = user;
        this.name = name;
        this.image = image;
        this.description = description;
        this.public_shelf = public_shelf;
    }

    public Long getShelf_id() {
        return shelf_id;
    }

    public void setShelf_id(Long shelf_id) {
        this.shelf_id = shelf_id;
    }

    public Set<BookEntity> getBooks() {
        return books;
    }

    public void setBooks(Set<BookEntity> books) {
        this.books = books;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPublic_shelf() {
        return public_shelf;
    }

    public void setPublic_shelf(Boolean public_shelf) {
        this.public_shelf = public_shelf;
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
