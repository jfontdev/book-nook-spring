package com.jordi.booknook.repositories;

import com.jordi.booknook.enums.Role;
import com.jordi.booknook.models.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(Role name);
}
