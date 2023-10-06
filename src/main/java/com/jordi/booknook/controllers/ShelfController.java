package com.jordi.booknook.controllers;

import com.jordi.booknook.models.ShelfEntity;
import com.jordi.booknook.payload.request.AddBookToShelfRequest;
import com.jordi.booknook.payload.request.NewShelfRequest;
import com.jordi.booknook.payload.request.UpdateShelfRequest;
import com.jordi.booknook.payload.response.AddBookToShelfResponse;
import com.jordi.booknook.payload.response.NewShelfResponse;
import com.jordi.booknook.payload.response.UpdateShelfResponse;
import com.jordi.booknook.services.ShelfService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/add-book")
    public ResponseEntity<AddBookToShelfResponse> addBookToShelf(@Valid @RequestBody AddBookToShelfRequest request){
        AddBookToShelfResponse response = shelfService.addBookToShelf(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public List<ShelfEntity> getShelvesByUser(){
        return shelfService.getAllUserShelves();
    }

    @GetMapping("/{shelves_id}/show")
    public ShelfEntity getShelfByUserAndShelfId(@PathVariable Long shelves_id){
        return shelfService.getOneUserShelf(shelves_id);
    }

    @GetMapping("/private")
    public List<ShelfEntity> getPrivateShelvesByUser(){
        return shelfService.getAllUserPrivateShelves();
    }


    // TODO: Abstract Error handling to a general Error handler.
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleEntityNotFoundException(AccessDeniedException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
    }
}
