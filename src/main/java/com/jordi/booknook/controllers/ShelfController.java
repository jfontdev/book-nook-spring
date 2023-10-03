package com.jordi.booknook.controllers;

import com.jordi.booknook.payload.request.NewShelfRequest;
import com.jordi.booknook.payload.response.NewShelfResponse;
import com.jordi.booknook.services.ShelfService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/shelves")
public class ShelfController {
    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @PostMapping("")
    public ResponseEntity<NewShelfResponse> addNewShelf(@Valid @RequestBody NewShelfRequest newShelf){
        NewShelfResponse response = shelfService.addNewShelf(newShelf);

        return ResponseEntity.ok(response);
    }
}
