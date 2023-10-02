package com.jordi.booknook.repositories;

import com.jordi.booknook.models.ShelfEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelfRepository extends JpaRepository<ShelfEntity, Long> {
}
