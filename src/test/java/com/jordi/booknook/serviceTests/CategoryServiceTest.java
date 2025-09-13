package com.jordi.booknook.serviceTests;

import com.jordi.booknook.models.CategoryEntity;
import com.jordi.booknook.repositories.CategoryRepository;
import com.jordi.booknook.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    CategoryService service;

    @Mock
    CategoryRepository categoryRepository;

    @BeforeEach
    void setUp(){
        this.service = new CategoryService(categoryRepository);
    }

    @Test
    void getCategoriesShouldReturnAll() {
        // Given: A valid request.
        LocalDateTime date = LocalDateTime.now();

        CategoryEntity category1 = new CategoryEntity("Categoria 1", date,date);
        CategoryEntity category2 = new CategoryEntity("Categoria 2", date,date);
        CategoryEntity category3 = new CategoryEntity("Categoria 3", date,date);

        when(categoryRepository.findAll())
                .thenReturn(List.of(category1, category2, category3));

        // When: We call the service method getCategories.
        List<CategoryEntity> categories = service.getCategories();

        List<CategoryEntity> expectedCategories = List.of(category1, category2, category3);

        // Then: We assert that the list we get back is equal to the expectedCategories list.
        assertThat(categories).isEqualTo(expectedCategories);
    }
}
