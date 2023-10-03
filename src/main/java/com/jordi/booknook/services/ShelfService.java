package com.jordi.booknook.services;

import com.jordi.booknook.models.ShelfEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.payload.request.NewShelfRequest;
import com.jordi.booknook.payload.response.NewShelfResponse;
import com.jordi.booknook.repositories.ShelfRepository;
import com.jordi.booknook.repositories.UserRepository;
import com.jordi.booknook.security.UserDetailsImplementation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShelfService {
    private final ShelfRepository shelfRepository;
    private final UserRepository userRepository;

    public ShelfService(ShelfRepository shelfRepository, UserRepository userRepository) {
        this.shelfRepository = shelfRepository;
        this.userRepository = userRepository;
    }

    public NewShelfResponse addNewShelf(NewShelfRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());

        if (authenticatedUser.isPresent()) {
            ShelfEntity newShelf = shelfRepository.saveAndFlush(new ShelfEntity(authenticatedUser.get(), request.name(),
                    request.image(), request.description(), request.public_shelf()));

            return new NewShelfResponse(newShelf.getUser().getUsername(),newShelf.getShelf_id(),
                    newShelf.getName(), newShelf.getImage(), newShelf.getDescription(),
                    newShelf.getPublic_shelf());
        } else {
            return new NewShelfResponse(null,null,null,null,null,null);
        }
    }
}