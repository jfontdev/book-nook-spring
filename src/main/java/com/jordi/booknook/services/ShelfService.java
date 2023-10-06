package com.jordi.booknook.services;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.ShelfEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.payload.request.AddBookToShelfRequest;
import com.jordi.booknook.payload.request.NewShelfRequest;
import com.jordi.booknook.payload.request.UpdateShelfRequest;
import com.jordi.booknook.payload.response.AddBookToShelfResponse;
import com.jordi.booknook.payload.response.NewShelfResponse;
import com.jordi.booknook.payload.response.UpdateShelfResponse;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.repositories.ShelfRepository;
import com.jordi.booknook.repositories.UserRepository;
import com.jordi.booknook.security.UserDetailsImplementation;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShelfService {
    private final ShelfRepository shelfRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ShelfService(ShelfRepository shelfRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.shelfRepository = shelfRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
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

    public UpdateShelfResponse updateShelfById(Long shelf_id, UpdateShelfRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());
        Optional<ShelfEntity> shelf = shelfRepository.findById(shelf_id);

        if (authenticatedUser.isPresent() && shelf.isPresent()) {
            ShelfEntity updatedShelf = shelf.get();
            updatedShelf.setName(request.name());
            updatedShelf.setImage(request.image());
            updatedShelf.setDescription(request.description());
            updatedShelf.setPublic_shelf(request.public_shelf());

            shelfRepository.save(updatedShelf);

            return new UpdateShelfResponse(authenticatedUser.get().getUsername(),
                    updatedShelf.getShelf_id(),
                    updatedShelf.getName(),
                    updatedShelf.getImage(),
                    updatedShelf.getDescription(),
                    updatedShelf.getPublic_shelf());
        } else {
            throw new EntityNotFoundException("Shelf Not found.");
        }
    }

    public AddBookToShelfResponse addBookToShelf(AddBookToShelfRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());
        Optional<BookEntity> bookToAdd = bookRepository.findById(request.book_id());
        Optional<ShelfEntity> shelf = shelfRepository.findById(request.shelf_id());

        if (bookToAdd.isEmpty()){
            throw new EntityNotFoundException("Book Not Found.");
        }

        if (shelf.isEmpty()){
            throw new EntityNotFoundException("Shelf Not Found.");
        }

        // Todo: Fix Servlet AuthException handling (AuthEntryPointJwt).
        if ((authenticatedUser.isPresent() && authenticatedUser.get() != shelf.get().getUser())){
            throw new AccessDeniedException("Not allowed to add a book to that shelf.");
        }

        ShelfEntity updatedShelf = shelf.get();
        updatedShelf.getBooks().add(bookToAdd.get());
        BookEntity lastBook = updatedShelf.getBooks().stream().reduce((one, two) -> two).get();

        shelfRepository.save(updatedShelf);

        return new AddBookToShelfResponse(lastBook,updatedShelf);
    }

    public List<ShelfEntity> getAllUserShelves() throws AuthenticationException{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());

        return shelfRepository.findAllByUser(authenticatedUser.orElseThrow());
    }

    public ShelfEntity getOneUserShelf(Long shelves_id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());
        Optional<ShelfEntity> shelf = shelfRepository.findOneByUserAndShelfId(authenticatedUser.orElseThrow(),
                shelves_id);

        if (shelf.isEmpty()){
            throw new EntityNotFoundException("Shelf Not Found.");
        }

        return shelf.get();
    }
}