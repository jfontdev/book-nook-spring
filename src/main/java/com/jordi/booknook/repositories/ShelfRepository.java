package com.jordi.booknook.repositories;

import com.jordi.booknook.models.ShelfEntity;
import com.jordi.booknook.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShelfRepository extends JpaRepository<ShelfEntity, Long> {

    List<ShelfEntity> findAllByUser(UserEntity user);
}
