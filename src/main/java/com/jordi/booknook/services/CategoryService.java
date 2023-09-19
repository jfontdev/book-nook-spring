package com.jordi.booknook.services;

import com.jordi.booknook.models.CategoryEntity;
import com.jordi.booknook.models.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    List<CategoryEntity> getCategories(){
        return categoryRepository.findAll();
    }
}
