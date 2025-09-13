package com.jordi.booknook.repositories;

import com.jordi.booknook.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByAuthSub(String authSub);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByAuthSub(String authSub);
}
