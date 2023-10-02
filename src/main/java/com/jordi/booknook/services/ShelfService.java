package com.jordi.booknook.services;

import com.jordi.booknook.repositories.ShelfRepository;
import org.springframework.stereotype.Service;

@Service
public class ShelfService {
    private final ShelfRepository shelfRepository;

    public ShelfService(ShelfRepository shelfRepository) {
        this.shelfRepository = shelfRepository;
    }
}
