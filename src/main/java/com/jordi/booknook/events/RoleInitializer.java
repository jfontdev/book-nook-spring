package com.jordi.booknook.events;

import com.jordi.booknook.enums.Role;
import com.jordi.booknook.models.RoleEntity;
import com.jordi.booknook.repositories.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleInitializer {
    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        if (roleRepository.findByName(Role.ROLE_USER).isEmpty()) {
            roleRepository.save(new RoleEntity(Role.ROLE_USER));
        }
        if (roleRepository.findByName(Role.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new RoleEntity(Role.ROLE_ADMIN));
        }
    }
}
