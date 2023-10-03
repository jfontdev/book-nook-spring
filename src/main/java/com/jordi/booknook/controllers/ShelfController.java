package com.jordi.booknook.controllers;

import com.jordi.booknook.payload.request.NewShelfRequest;
import com.jordi.booknook.payload.request.UpdateShelfRequest;
import com.jordi.booknook.payload.response.NewShelfResponse;
import com.jordi.booknook.payload.response.UpdateShelfResponse;
import com.jordi.booknook.services.ShelfService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{shelf_id}/update")
    public ResponseEntity<UpdateShelfResponse> updateShelf(@PathVariable Long shelf_id,
                                                           @Valid @RequestBody UpdateShelfRequest updateShelf){
        UpdateShelfResponse response = shelfService.updateShelfById(shelf_id,updateShelf);

        return ResponseEntity.ok(response);
    }


    // TODO: Abstract Error handling to a general Error handler.
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>("Shelf Not Found.", HttpStatus.NOT_FOUND);
    }
}
