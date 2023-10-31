package com.jordi.booknook.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "books")
@JsonPropertyOrder({"book_id", "title", "description", "cover", "categories", "images", "price", "created_at", "updated_at"})
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long book_id;

    @ManyToMany
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CategoryEntity> categories = new HashSet<>();

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "book_id")
    @JsonIgnoreProperties("book") // Ignores serialization of images property on book
    @JsonProperty("images") // Gives it a custom name
    private List<BookImagesEntity> images = new ArrayList<>();

    // TODO: Find a better way to make use of the relationship.
    @OneToMany(mappedBy = "book")
    private List<BookReviewEntity> reviews;

    private String cover;
    private String title;
    private String description;
    private BigDecimal price;

    @CreationTimestamp
    @JsonIgnore
    private LocalDateTime created_at;

    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime updated_at;

    public BookEntity(String cover, String title, String description, BigDecimal price, LocalDateTime created_at, LocalDateTime updated_at) {
        this.book_id = null;
        this.cover = cover;
        this.title = title;
        this.description = description;
        this.price = price;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public BookEntity() {
        this(null,null,null,null,null,null);
    }

    public Long getBook_id() {
        return book_id;
    }

    public void setBook_id(Long book_id) {
        this.book_id = book_id;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public Set<CategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryEntity> categories) {
        this.categories = categories;
    }

    public List<BookImagesEntity> getImages() {
        return images;
    }

    public void setImages(List<BookImagesEntity> images) {
        this.images = images;
    }

    public void addCategories(CategoryEntity category){
        categories.add(category);
        category.getBooks().add(this);
    }

    public void removeCategories(CategoryEntity category){
        categories.remove(category);
        category.getBooks().remove(this);
    }

    public void addImage(BookImagesEntity image){
        images.add(image);
        image.setBook(this);
    }

    public void removeImage(BookImagesEntity image){
        images.remove(image);
        image.setBook(null);
    }

    public double getAverageRating(){
        if (reviews == null || reviews.isEmpty()){
            return 0.0;
        }

        double sum = 0.0;
        for (BookReviewEntity review : reviews){
            sum += review.getRating();
        }

        double average = sum / reviews.size();
        return Math.round(average * 10.0) / 10.0;
    }
}
