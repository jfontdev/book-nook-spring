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
import java.util.Objects;
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

        ShelfEntity newShelf = shelfRepository.saveAndFlush(new ShelfEntity(authenticatedUser.orElseThrow(),
                request.name(), request.image(), request.description(), request.public_shelf()));

        return new NewShelfResponse(newShelf.getUser().getUsername(),newShelf.getShelf_id(),
                newShelf.getName(), newShelf.getImage(), newShelf.getDescription(),
                newShelf.getPublic_shelf());
    }

    public UpdateShelfResponse updateShelfById(Long shelf_id, UpdateShelfRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        String username = userDetails.getUsername();
        Optional<ShelfEntity> shelf = shelfRepository.findById(shelf_id);

        if (shelf.isEmpty()) {
            throw new EntityNotFoundException("Shelf Not found.");
        }

        ShelfEntity updatedShelf = shelf.get();

        if (request.name() != null){
            updatedShelf.setName(request.name());
        }
        if (request.image() != null){
            updatedShelf.setImage(request.image());
        }
        if (request.description() != null){
            updatedShelf.setDescription(request.description());
        }
        if (request.public_shelf() != null){
            updatedShelf.setPublic_shelf(request.public_shelf());
        }

        shelfRepository.save(updatedShelf);

        return new UpdateShelfResponse(username,
                    updatedShelf.getShelf_id(),
                    updatedShelf.getName(),
                    updatedShelf.getImage(),
                    updatedShelf.getDescription(),
                    updatedShelf.getPublic_shelf());

    }

    public AddBookToShelfResponse addBookToShelf(AddBookToShelfRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        String username = userDetails.getUsername();
        Optional<BookEntity> bookToAdd = bookRepository.findById(request.book_id());
        Optional<ShelfEntity> shelf = shelfRepository.findById(request.shelf_id());

        if (bookToAdd.isEmpty()){
            throw new EntityNotFoundException("Book Not Found.");
        }

        if (shelf.isEmpty()){
            throw new EntityNotFoundException("Shelf Not Found.");
        }

        String shelfUsername = shelf.get().getUser().getUsername();

        if (!Objects.equals(username, shelfUsername)){
            throw new AccessDeniedException("Not allowed to add a book to that shelf.");
        }

        ShelfEntity updatedShelf = shelf.get();
        updatedShelf.getBooks().add(bookToAdd.get());
        BookEntity lastBook = updatedShelf.getBooks()
                .stream()
                .reduce((one, two) -> two)
                .orElseThrow();

        shelfRepository.save(updatedShelf);

        return new AddBookToShelfResponse(lastBook,updatedShelf);
    }

    public List<ShelfEntity> getAllUserShelves(){
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

    public List<ShelfEntity> getAllUserPrivateShelves(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(userDetails.getUsername());

        return shelfRepository.findAllByUserAndPublicShelfIsFalse(authenticatedUser.orElseThrow());
    }

    public List<ShelfEntity> getAllUserPublicShelves(Long user_id){
        Optional<UserEntity> user = userRepository.findById(user_id);

        return shelfRepository.findAllByUserAndPublicShelfIsTrue(user.orElseThrow());
    }
}