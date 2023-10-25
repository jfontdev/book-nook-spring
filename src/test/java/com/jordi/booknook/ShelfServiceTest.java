package com.jordi.booknook;

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
import com.jordi.booknook.services.ShelfService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ShelfServiceTest {
    ShelfService service;
    static UserEntity user1;
    static ShelfEntity shelf;
    static BookEntity book1;

   @Mock
   ShelfRepository shelfRepository;
   @Mock
   UserRepository userRepository;
   @Mock
   BookRepository bookRepository;
   @Mock
   Authentication auth;
   @Mock
   UserDetailsImplementation userDetails;

   @BeforeAll
   static void setUpCommonEntities() {
       // Create common entities that can be reused in multiple tests.
       BigDecimal price = new BigDecimal("12.50");
       LocalDateTime date = LocalDateTime.now();

       user1 = new UserEntity("jordi", "jordi@email.com", "password");

       shelf = new ShelfEntity(
               user1,
               "Nueva estanteria",
               "imagen.jpg",
               "Mi nueva estanteria",
               true);
       shelf.setShelf_id(2L);

       book1 = new BookEntity(
               "Portada","Nuevo libro", "Un gran libro",price,date,date);
       book1.setBook_id(5L);
   }

   @BeforeEach
    void setUp(){
       this.service = new ShelfService(shelfRepository,userRepository,bookRepository);
   }

   @Test
   void addNewShelfShouldAddANewShelfAndShouldReturn() {
       // Given: A valid request with a new shelf and a logged user.
       when(auth.getPrincipal())
               .thenReturn(userDetails);
       when(userDetails.getUsername())
               .thenReturn(user1.getUsername());
       when(userRepository.findByUsername(user1.getUsername()))
               .thenReturn(Optional.of(user1));

       SecurityContextHolder.getContext().setAuthentication(auth);

       NewShelfRequest request = new NewShelfRequest(
               "Nueva estanteria",
               "imagen.jpg",
               "Mi nueva estanteria",
               true
       );

       ShelfEntity shelf1 = new ShelfEntity(
               user1,
               request.name(),
               request.image(),
               request.description(),
               request.public_shelf());


       when(shelfRepository.saveAndFlush(any(ShelfEntity.class)))
               .thenReturn(shelf1);

       // When: We call the addNewShelf service method with the valid request.
       NewShelfResponse response = service.addNewShelf(request);

       //Then: We assert that the response we get back is the same as the expectedResponse.
       NewShelfResponse expectedResponse = new NewShelfResponse(
               user1.getUsername(),
               shelf1.getShelf_id(),
               shelf1.getName(),
               shelf1.getImage(),
               shelf1.getDescription(),
               shelf1.getPublic_shelf()
       );

       assertThat(response).isEqualTo(expectedResponse);
   }

   @Test
   void updateShelfByIdShouldUpdateTheShelfAndShouldReturn() {
       // Given: A valid request with a valid shelf that has all the properties by the owner of that shelf.
       UpdateShelfRequest request = new UpdateShelfRequest(
               "Nueva estanteria editada",
               "imagen-editada.jpg",
               "Mi nueva estanteria editada",
               false
       );

       when(auth.getPrincipal())
               .thenReturn(userDetails);
       when(userDetails.getUsername())
               .thenReturn(user1.getUsername());

       SecurityContextHolder.getContext().setAuthentication(auth);

       when(shelfRepository.findById(2L))
               .thenReturn(Optional.of(shelf));

       shelf.setName(request.name());
       shelf.setImage(request.image());
       shelf.setDescription(request.description());
       shelf.setPublic_shelf(request.public_shelf());

       when(shelfRepository.save(shelf))
               .thenReturn(shelf);

       // When: We call the updateByShelfId service method with a valid shelf id and the valid request.
       UpdateShelfResponse response = service.updateShelfById(shelf.getShelf_id(), request);

       // Then: We assert that the response we get back is the same as the expectedResponse.
       UpdateShelfResponse expectedResponse = new UpdateShelfResponse(
               user1.getUsername(),
               shelf.getShelf_id(),
               shelf.getName(),
               shelf.getImage(),
               shelf.getDescription(),
               shelf.getPublic_shelf()
       );

       assertThat(response).isEqualTo(expectedResponse);
   }

   @Test
   void updateShelfByIdShouldReturnErrorWhenShelfIsNotFound() {
       // Given: A bad request with an invalid shelf id and a logged user.
       Long invalidId = 1L;

       UpdateShelfRequest request = new UpdateShelfRequest(
               "Nueva estanteria editada",
               "imagen-editada.jpg",
               "Mi nueva estanteria editada",
               false
       );

       when(auth.getPrincipal())
               .thenReturn(userDetails);
       when(userDetails.getUsername())
               .thenReturn(user1.getUsername());

       SecurityContextHolder.getContext().setAuthentication(auth);

       // When: We call the updateShelfById with an invalid shelf id and a valid request.
       Executable action = () -> service.updateShelfById(invalidId, request);

       // Then: We assert that it throws a EntityNotFoundException.
       EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
               action);

       // And: That the error message thrown by the exception is equal to the expected error message.
       String expectedErrorMessage = "Shelf Not found.";
       assertThat(exception.getMessage()).isEqualTo(expectedErrorMessage);
   }

   @Test
   void updateShelfByIdShouldUpdateTheShelfWithOnlyANameAndShouldReturn(){
       // Given: A valid request with a valid shelf that has only a name by the owner of that shelf.
       UpdateShelfRequest request = new UpdateShelfRequest(
               "Nueva estanteria editada",
               null,
               null,
               null
       );

       when(auth.getPrincipal())
               .thenReturn(userDetails);
       when(userDetails.getUsername())
               .thenReturn(user1.getUsername());

       SecurityContextHolder.getContext().setAuthentication(auth);

       when(shelfRepository.findById(2L))
               .thenReturn(Optional.of(shelf));

       ShelfEntity initialShelf = new ShelfEntity(
               shelf.getUser(),
               shelf.getName(),
               shelf.getImage(),
               shelf.getDescription(),
               shelf.getPublic_shelf()
       );

       ShelfEntity updatedShelf = shelf;

       updatedShelf.setName(request.name());

       when(shelfRepository.save(updatedShelf))
               .thenReturn(updatedShelf);

       // When: We call the updateByShelfId method with a valid shelf id and valid request with a name.
       UpdateShelfResponse response = service.updateShelfById(updatedShelf.getShelf_id(), request);

       // Then: We assert that the response we get back is the same as the expectedResponse.
       UpdateShelfResponse expectedResponse = new UpdateShelfResponse(
               user1.getUsername(),
               updatedShelf.getShelf_id(),
               updatedShelf.getName(),
               updatedShelf.getImage(),
               updatedShelf.getDescription(),
               updatedShelf.getPublic_shelf()
       );

       assertThat(response).isEqualTo(expectedResponse);

       /* And: We assert that the rest of the properties not present on the update request
               are not changed in the initialShelf.
       */
       assertThat(response)
               .extracting(
                       UpdateShelfResponse::shelf_image,
                       UpdateShelfResponse::shelf_description,
                       UpdateShelfResponse::public_shelf
               )
               .containsExactly(initialShelf.getImage(), initialShelf.getDescription(), initialShelf.getPublic_shelf());
   }

    @Test
    void updateShelfByIdShouldUpdateTheShelfWithOnlyAnImageAndShouldReturn(){
        // Given: A valid request with a valid shelf that has only an image by the owner of that shelf.
        UpdateShelfRequest request = new UpdateShelfRequest(
                null,
                "imagen-editada.jpg",
                null,
                null
        );

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(user1.getUsername());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(shelfRepository.findById(2L))
                .thenReturn(Optional.of(shelf));

        ShelfEntity initialShelf = new ShelfEntity(
                shelf.getUser(),
                shelf.getName(),
                shelf.getImage(),
                shelf.getDescription(),
                shelf.getPublic_shelf()
        );

        ShelfEntity updatedShelf = shelf;

        updatedShelf.setImage(request.image());

        when(shelfRepository.save(updatedShelf))
                .thenReturn(updatedShelf);

        // When: We call the updateByShelfId method with a valid shelf id and valid request with an image.
        UpdateShelfResponse response = service.updateShelfById(updatedShelf.getShelf_id(), request);

        // Then: We assert that the response we get back is the same as the expectedResponse.
        UpdateShelfResponse expectedResponse = new UpdateShelfResponse(
                user1.getUsername(),
                updatedShelf.getShelf_id(),
                updatedShelf.getName(),
                updatedShelf.getImage(),
                updatedShelf.getDescription(),
                updatedShelf.getPublic_shelf()
        );

        assertThat(response).isEqualTo(expectedResponse);

        /* And: We assert that the rest of the properties not present on the update request
                are not changed in the initialShelf.
        */
        assertThat(response)
                .extracting(
                        UpdateShelfResponse::shelf_name,
                        UpdateShelfResponse::shelf_description,
                        UpdateShelfResponse::public_shelf
                )
                .containsExactly(initialShelf.getName(), initialShelf.getDescription(), initialShelf.getPublic_shelf());
    }

    @Test
    void updateShelfByIdShouldUpdateTheShelfWithOnlyADescriptionAndShouldReturn(){
        // Given: A valid request with a valid shelf that has only a description by the owner of that shelf.
        UpdateShelfRequest request = new UpdateShelfRequest(
                null,
                null,
                "Nueva descripción.",
                null
        );

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(user1.getUsername());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(shelfRepository.findById(2L))
                .thenReturn(Optional.of(shelf));

        ShelfEntity initialShelf = new ShelfEntity(
                shelf.getUser(),
                shelf.getName(),
                shelf.getImage(),
                shelf.getDescription(),
                shelf.getPublic_shelf()
        );

        ShelfEntity updatedShelf = shelf;

        updatedShelf.setDescription(request.description());

        when(shelfRepository.save(updatedShelf))
                .thenReturn(updatedShelf);

        // When: We call the updateByShelfId method with a valid shelf id and valid request with a description.
        UpdateShelfResponse response = service.updateShelfById(updatedShelf.getShelf_id(), request);

        // Then: We assert that the response we get back is the same as the expectedResponse.
        UpdateShelfResponse expectedResponse = new UpdateShelfResponse(
                user1.getUsername(),
                updatedShelf.getShelf_id(),
                updatedShelf.getName(),
                updatedShelf.getImage(),
                updatedShelf.getDescription(),
                updatedShelf.getPublic_shelf()
        );

        assertThat(response).isEqualTo(expectedResponse);

        /* And: We assert that the rest of the properties not present on the update request
                are not changed in the initialShelf.
        */
        assertThat(response)
                .extracting(
                        UpdateShelfResponse::shelf_name,
                        UpdateShelfResponse::shelf_image,
                        UpdateShelfResponse::public_shelf
                )
                .containsExactly(initialShelf.getName(), initialShelf.getImage(), initialShelf.getPublic_shelf());
    }

    @Test
    void updateShelfByIdShouldUpdateTheShelfWithOnlyAPrivacyModifierAndShouldReturn(){
        // Given: A valid request with a valid shelf that has only a privacy modifier by the owner of that shelf.
        UpdateShelfRequest request = new UpdateShelfRequest(
                null,
                null,
                null,
                false
        );

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(user1.getUsername());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(shelfRepository.findById(2L))
                .thenReturn(Optional.of(shelf));

        ShelfEntity initialShelf = new ShelfEntity(
                shelf.getUser(),
                shelf.getName(),
                shelf.getImage(),
                shelf.getDescription(),
                shelf.getPublic_shelf()
        );

        ShelfEntity updatedShelf = shelf;

        updatedShelf.setPublic_shelf(request.public_shelf());

        when(shelfRepository.save(updatedShelf))
                .thenReturn(updatedShelf);

        // When: We call the updateByShelfId method with a valid shelf id and valid request with a privacy modifier.
        UpdateShelfResponse response = service.updateShelfById(updatedShelf.getShelf_id(), request);

        // Then: We assert that the response we get back is the same as the expectedResponse.
        UpdateShelfResponse expectedResponse = new UpdateShelfResponse(
                user1.getUsername(),
                updatedShelf.getShelf_id(),
                updatedShelf.getName(),
                updatedShelf.getImage(),
                updatedShelf.getDescription(),
                updatedShelf.getPublic_shelf()
        );

        assertThat(response).isEqualTo(expectedResponse);

        /* And: We assert that the rest of the properties not present on the update request
                are not changed in the initialShelf.
        */
        assertThat(response)
                .extracting(
                        UpdateShelfResponse::shelf_name,
                        UpdateShelfResponse::shelf_image,
                        UpdateShelfResponse::shelf_description
                )
                .containsExactly(initialShelf.getName(), initialShelf.getImage(), initialShelf.getDescription());
    }

    @Test
    void updateShelfByIdShouldReturnErrorWhenUserIsNotAllowed() {
        // Given: A valid request with a valid shelf but with a user that doesn't own that shelf.
        UpdateShelfRequest request = new UpdateShelfRequest(
                "Nueva estanteria editada",
                "imagen-editada.jpg",
                "Mi nueva estanteria editada",
                false
        );

        UserEntity nonAllowedUser = new UserEntity(
                "Tamara", "tamara@gmail.com", "asdasda");

        when(shelfRepository.findById(2L))
                .thenReturn(Optional.of(shelf));

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(nonAllowedUser.getUsername());

        SecurityContextHolder.getContext().setAuthentication(auth);

        /* When: We call the updateShelfById with a valid shelf id and a valid request,
           but a user that doesn't own that shelf.
         */
        Executable action = () -> service.updateShelfById(shelf.getShelf_id(),request);

        // Then: We assert that it throws a AccessDeniedException.
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                action);

        // And: That the error message thrown by the exception is equal to the expected error message.
        String expectedErrorMessage = "Not allowed to update that shelf.";
        assertEquals(expectedErrorMessage ,exception.getMessage());
    }

    @Test
    void addBookToShelfShouldAddTheBookAndShouldReturn() {
        // Given: A valid request with a valid shelf id, book id and a logged user that owns the shelf
        AddBookToShelfRequest request = new AddBookToShelfRequest(5L,2L);

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(user1.getUsername());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(bookRepository.findById(request.book_id()))
                .thenReturn(Optional.of(book1));

        when(shelfRepository.findById(request.shelf_id()))
                .thenReturn(Optional.of(shelf));

        ShelfEntity updatedShelf = shelf;

        updatedShelf.getBooks().add(book1);

        // When: We call the method addBookToShelf with the valid request.
        AddBookToShelfResponse response = service.addBookToShelf(request);

        /* Then: We assert that the response we get back is the same as the expectedResponse.
                 extracting the last book from the updated shelf and comparing it with the
                 response we get back from the service method
         */
        BookEntity lastBook = updatedShelf.getBooks()
                .stream()
                .reduce((one, two) -> two)
                .orElseThrow();

        AddBookToShelfResponse expectedResponse = new AddBookToShelfResponse(lastBook,updatedShelf);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void addBookToShelfShouldReturnErrorWhenTheBookIsNotFound() {
        // Given: A bad request with a valid shelf with correct owner but an invalid book.
        AddBookToShelfRequest request = new AddBookToShelfRequest(1L,2L);

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(user1.getUsername());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(shelfRepository.findById(request.shelf_id()))
                .thenReturn(Optional.of(shelf));

        // When: We call the service method addBookToShelf with the bad request.
        Executable action = () -> service.addBookToShelf(request);

        // Then: We assert that it throws a EntityNotFoundException.
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                action);

        // And: That the error message thrown by the exception is equal to the expected error message.
        String expectedMessage = "Book Not Found.";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void addBookToShelfShouldReturnErrorWhenTheShelfIsNotFound() {
        // Given: A bad request with a valid book, an invalid shelf and a logged user.
        AddBookToShelfRequest request = new AddBookToShelfRequest(5L,1L);

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(user1.getUsername());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(bookRepository.findById(request.book_id()))
                .thenReturn(Optional.of(book1));

        // When: We call the service method addBookToShelf with the bad request.
        Executable action = () -> service.addBookToShelf(request);

        // Then: We assert that it throws a EntityNotFoundException.
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                action);

        // And: That the error message thrown by the exception is equal to the expected error message.
        String expectedMessage = "Shelf Not Found.";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void addBookToShelfShouldReturnErrorWhenTheUserIsNotTheOwner() {
        /* Given: A bad request with a valid book, a valid shelf and a logged user.
                  that is not the owner of that shelf.
         */
        AddBookToShelfRequest request = new AddBookToShelfRequest(5L,2L);

        UserEntity nonAllowedUser = new UserEntity(
                "Tamara", "tamara@gmail.com", "asdasda");

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(nonAllowedUser.getUsername());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(bookRepository.findById(request.book_id()))
                .thenReturn(Optional.of(book1));

        when(shelfRepository.findById(request.shelf_id()))
                .thenReturn(Optional.of(shelf));

        // When: We call the service method addBookToShelf with the bad request.
        Executable action = () -> service.addBookToShelf(request);

        // Then: We assert that it throws a AccessDeniedException.
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                action);

        // And: That the error message thrown by the exception is equal to the expected error message.
        String expectedMessage = "Not allowed to add a book to that shelf.";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void getAllUserShelvesShouldReturnAll(){
        // Given: A list of 3 shelves assigned to the user1 entity.
        ShelfEntity shelf1 = new ShelfEntity(
                user1,
                "Nueva estanteria 1",
                "imagen1.jpg",
                "Mi nueva estanteria 1",
                true);
        ShelfEntity shelf2 = new ShelfEntity(
                user1,
                "Nueva estanteria 2",
                "imagen2.jpg",
                "Mi nueva estanteria 2",
                false);
        ShelfEntity shelf3 = new ShelfEntity(
                user1,
                "Nueva estanteria 3",
                "imagen3.jpg",
                "Mi nueva estanteria 3",
                true);

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(user1.getUsername());
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(shelfRepository.findAllByUser(user1))
                .thenReturn(List.of(shelf1,shelf2,shelf3));

        // When: The service method getAllUserShelves is called with auth user1.
        List<ShelfEntity> shelves = service.getAllUserShelves();

        // Then: We get the exact 3 shelves we added for this test.
        assertThat(shelves).containsExactly(shelf1,shelf2,shelf3);
    }

    @Test
    void getOneUserShelfShouldReturn() {
       // Given: A valid request with a shelf id owned by the logged user.
       when(auth.getPrincipal())
               .thenReturn(userDetails);
       when(userDetails.getUsername())
               .thenReturn(user1.getUsername());
       when(userRepository.findByUsername(user1.getUsername()))
               .thenReturn(Optional.of(user1));

       SecurityContextHolder.getContext().setAuthentication(auth);

       when(shelfRepository.findOneByUserAndShelfId(user1,shelf.getShelf_id()))
               .thenReturn(Optional.of(shelf));

       // When: We call the getOneUserShelf service method with the valid shelf id.
       ShelfEntity shelfByUser = service.getOneUserShelf(shelf.getShelf_id());

       // Then: We assert that we get back that shelf owned by the user.
       assertThat(shelfByUser).isEqualTo(shelf);
    }

    @Test
    void getOneUserShelfShouldReturnErrorWhenTheShelfIsNotFound() {
       // Given: A bad request with an invalid shelf id and a logged user.
       Long nonValidShelfId = 1L;

       when(auth.getPrincipal())
               .thenReturn(userDetails);
       when(userDetails.getUsername())
               .thenReturn(user1.getUsername());
       when(userRepository.findByUsername(user1.getUsername()))
               .thenReturn(Optional.of(user1));

       SecurityContextHolder.getContext().setAuthentication(auth);

       // When: We call the service method getOneUserShelf with the invalid shelf id.
       Executable action = () -> service.getOneUserShelf(nonValidShelfId);

        // Then: We assert that it throws a EntityNotFoundException.
       EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
               action);

       // And: That the error message thrown by the exception is equal to the expected error message.
       String expectedErrorMessage = "Shelf Not Found.";
       assertThat(expectedErrorMessage).isEqualTo(exception.getMessage());
    }

    @Test
    void getAllUserPrivateShelvesShouldReturnAllUserPrivateShelves() {
        // Given: A valid request with a logged user that has 3 shelves assigned.
        ShelfEntity shelf1 = new ShelfEntity(
                user1,
                "Nueva estanteria 1",
                "imagen1.jpg",
                "Mi nueva estanteria 1",
                false);
        ShelfEntity shelf2 = new ShelfEntity(
                user1,
                "Nueva estanteria 2",
                "imagen2.jpg",
                "Mi nueva estanteria 2",
                false);
        ShelfEntity shelf3 = new ShelfEntity(
                user1,
                "Nueva estanteria 3",
                "imagen3.jpg",
                "Mi nueva estanteria 3",
                true);

        when(auth.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getUsername())
                .thenReturn(user1.getUsername());
        when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));

        SecurityContextHolder.getContext().setAuthentication(auth);

        // We mock the filtering logic of the repository method, returning only the private shelves.
        List<ShelfEntity> allShelves = List.of(shelf1, shelf2, shelf3);
        when(shelfRepository.findAllByUserAndPublicShelfIsFalse(user1))
                .thenAnswer(invocation -> {
                    return allShelves
                            .stream()
                            .filter(shelf -> !shelf.getPublic_shelf())
                            .collect(Collectors.toList());
                });

        // When: We call the getAllUserPrivateShelves service method with the valid request.
        List<ShelfEntity> privateShelves = service.getAllUserPrivateShelves();

        // Then: We verify that shelfRepository.findAllByUserAndPublicShelfIsFalse(user1) was called.
        verify(shelfRepository).findAllByUserAndPublicShelfIsFalse(user1);

        // And: We assert that we get the exact 2 private shelves that the current user has assigned.
        assertThat(privateShelves).containsExactly(shelf1, shelf2);
    }
}
