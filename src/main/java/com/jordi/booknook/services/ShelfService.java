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
import com.jordi.booknook.security.CurrentUserResolver;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ShelfService {
    private final ShelfRepository shelfRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CurrentUserResolver currentUser;

    public ShelfService(ShelfRepository shelfRepository, UserRepository userRepository, BookRepository bookRepository, CurrentUserResolver currentUserResolver) {
        this.shelfRepository = shelfRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.currentUser = currentUserResolver;
    }

    public NewShelfResponse addNewShelf(NewShelfRequest request) {
        UserEntity user = currentUser.requireCurrentUser();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(user.getUsername());

        ShelfEntity newShelf = shelfRepository.saveAndFlush(new ShelfEntity(authenticatedUser.orElseThrow(),
                request.name(), request.image(), request.description(), request.public_shelf()));

        return new NewShelfResponse(newShelf.getUser().getUsername(),newShelf.getShelf_id(),
                newShelf.getName(), newShelf.getImage(), newShelf.getDescription(),
                newShelf.getPublic_shelf());
    }

    public UpdateShelfResponse updateShelfById(Long shelf_id, UpdateShelfRequest request){
        UserEntity user = currentUser.requireCurrentUser();

        String username = user.getUsername();
        Optional<ShelfEntity> shelf = shelfRepository.findById(shelf_id);

        if (shelf.isEmpty()) {
            throw new EntityNotFoundException("Shelf Not found.");
        }

        String shelfUsername = shelf.get().getUser().getUsername();

        if (!Objects.equals(username, shelfUsername)){
            throw new AccessDeniedException("Not allowed to update that shelf.");
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
        UserEntity user = currentUser.requireCurrentUser();

        String username = user.getUsername();
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
        UserEntity user = currentUser.requireCurrentUser();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(user.getUsername());

        return shelfRepository.findAllByUser(authenticatedUser.orElseThrow());
    }

    public ShelfEntity getOneUserShelf(Long shelves_id){
        UserEntity user = currentUser.requireCurrentUser();

        Optional<UserEntity> authenticatedUser = userRepository.findByUsername(user.getUsername());
        Optional<ShelfEntity> shelf = shelfRepository.findOneByUserAndShelfId(authenticatedUser.orElseThrow(),
                shelves_id);

        if (shelf.isEmpty()){
            throw new EntityNotFoundException("Shelf Not Found.");
        }

        return shelf.get();
    }

    public List<ShelfEntity> getAllUserPrivateShelves(){
        UserEntity user = currentUser.requireCurrentUser();

        return shelfRepository.findAllByUserAndPublicShelfIsFalse(user);
    }

    public List<ShelfEntity> getAllUserPublicShelves(Long user_id){
        Optional<UserEntity> user = userRepository.findById(user_id);

        return shelfRepository.findAllByUserAndPublicShelfIsTrue(user.orElseThrow());
    }
}