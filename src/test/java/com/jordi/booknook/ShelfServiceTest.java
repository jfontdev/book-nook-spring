package com.jordi.booknook;

import com.jordi.booknook.models.ShelfEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.payload.request.NewShelfRequest;
import com.jordi.booknook.payload.response.NewShelfResponse;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.repositories.ShelfRepository;
import com.jordi.booknook.repositories.UserRepository;
import com.jordi.booknook.security.UserDetailsImplementation;
import com.jordi.booknook.services.ShelfService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ShelfServiceTest {
    ShelfService service;
    static UserEntity user1;

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
}
