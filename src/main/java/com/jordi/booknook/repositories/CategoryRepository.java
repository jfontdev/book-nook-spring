package com.jordi.booknook.repositories;

import com.jordi.booknook.models.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> { }
