package com.jordi.booknook.repositories;

import com.jordi.booknook.models.ShelfEntity;
import com.jordi.booknook.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ShelfRepository extends JpaRepository<ShelfEntity, Long> {

    List<ShelfEntity> findAllByUser(UserEntity user);

    @Query("SELECT shelve FROM ShelfEntity shelve WHERE shelve.user = :user AND shelve.shelf_id = :shelfId")
    Optional<ShelfEntity> findOneByUserAndShelfId(@Param("user") UserEntity user, @Param("shelfId") Long shelf_id);

    @Query("SELECT shelves from ShelfEntity shelves WHERE shelves.user = :user AND shelves.public_shelf = false")
    List<ShelfEntity> findAllByUserAndPublicShelfIsFalse(@Param("user") UserEntity user);
}
