package com.jordi.booknook;

import com.jordi.booknook.models.ShelfEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.payload.request.NewShelfRequest;
import com.jordi.booknook.payload.request.UpdateShelfRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ShelfServiceTest {
    ShelfService service;
    static UserEntity user1;
    static ShelfEntity shelf;

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
       user1 = new UserEntity("jordi", "jordi@email.com", "password");
       shelf = new ShelfEntity(
               user1,
               "Nueva estanteria",
               "imagen.jpg",
               "Mi nueva estanteria",
               true);
       shelf.setShelf_id(2L);
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
}
