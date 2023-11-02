package com.jordi.booknook;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.models.BookReviewEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.repositories.BookRepository;
import com.jordi.booknook.repositories.BookReviewRepository;
import com.jordi.booknook.repositories.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testcontainers.containers.MySQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookReviewControllerTestContainerTest {
    static MySQLContainer<?> database =
            new MySQLContainer<>("mysql:8.0.34");

    private static boolean userRegistered = false;

    @Autowired
    BookReviewRepository repository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @LocalServerPort
    private Integer port;

    private String token;


    @BeforeAll
    static void beforeAll() {
        database.start();
    }

    @AfterAll
    static void afterAll() {
        database.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }


    protected void clearAndResetBookReviewsTable() {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            repository.deleteAll();
            repository.flush();
            bookRepository.deleteAll();
            bookRepository.flush();
            entityManager.createNativeQuery("ALTER TABLE book_reviews AUTO_INCREMENT=1").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE books AUTO_INCREMENT=1").executeUpdate();
            transactionManager.commit(status);
        } catch (Exception exception) {
            transactionManager.rollback(status);
            throw exception;
        }
    }


    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        clearAndResetBookReviewsTable();
        // We register a user to use it in auth protected routes.
        if (!userRegistered) {
            String registerBody = "{\"username\": \"jordi\", \"email\": \"jordi@gmail.com\", \"password\": \"123456\"}";
            given()
                    .contentType(ContentType.JSON)
                    .body(registerBody)
                    .when()
                    .post("/api/v1/auth/register")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK);
            userRegistered = true;
        }

        // We log in with that recent created user to get the token for auth protected routes.
        String loginBody = "{\"username\": \"jordi\", \"password\": \"123456\"}";
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/v1/auth/login")
                .then().assertThat().statusCode(200);

        token = response.extract().path("Token");
    }

    @Test
    void getReviewByBookShouldReturn(){

        // Given: A valid request with a logged-in user to the GET /api/v1/reviews/{book-id}/get endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price = new BigDecimal("12.50");
        LocalDateTime date = LocalDateTime.now();

        BookEntity book = new BookEntity(
                "Portada", "Nuevo libro test 2", "Un gran libro", price, date, date);

        bookRepository.save(book);

        List<UserEntity> users = List.of(
                new UserEntity("Usuario 2","usuario2@gmail.com","123456"),
                new UserEntity("Usuario 3","usuario3@gmail.com","123456"),
                new UserEntity("Usuario 4","usuario4@gmail.com","123456")
        );
        userRepository.saveAll(users);

        Optional<UserEntity> user = userRepository.findById(1L);

        List<BookReviewEntity> reviews = List.of(
                new BookReviewEntity(book,user.get(),1,"No me gusto"),
                new BookReviewEntity (book,users.get(0),2,"No me gusto"),
                new BookReviewEntity(book,users.get(1),3,"No esta mal"),
                new BookReviewEntity(book,users.get(2),5,"Me encanto")
        );

        repository.saveAll(reviews);

        // When: The GET request to retrieve all reviews with a valid book is made.
        Response response = given()
                .headers(headers)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/reviews/1/get");

        // Then: We assert that we get back a status code 200.
        response.then()
                .statusCode(200)
                .log()
                .body(true);

        /* And: That the rating of the first review in the JSON response
                is the same as the rating of the first review on the reviews list.
         */
        response.then()
                .body("reviews[0].rating",equalTo(reviews.get(0).getRating()));
    }

    @Test
    void getReviewByBookShouldReturnErrorWhenBookDoesNotExist(){

        // Given: A bad request with an invalid book id to the GET /api/v1/reviews/{book-id}/get endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        // When: The GET request to retrieve all reviews with an invalid book id is made.
        Response response = given()
                .headers(headers)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/reviews/1/get");

        // Then: We assert that we get back a status code 404.
        response.then()
                .statusCode(404)
                .log()
                .body(true);

        // And: We assert that the response body as a string contains the message error "Book not found"
        String responseBody = response.body().asString();
        assertThat(responseBody,containsString("Book not found"));
    }

    @Test
    void getReviewsByUserShouldReturn(){

        // Given: A valid request with a logged-in user to the GET /api/v1/reviews/get endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price = new BigDecimal("12.50");
        LocalDateTime date = LocalDateTime.now();

        BookEntity book = new BookEntity(
                "Portada", "Nuevo libro test 2", "Un gran libro", price, date, date);

        bookRepository.save(book);

        List<UserEntity> users = List.of(
                new UserEntity("Usuario 2","usuario2@gmail.com","123456"),
                new UserEntity("Usuario 3","usuario3@gmail.com","123456"),
                new UserEntity("Usuario 4","usuario4@gmail.com","123456")
        );
        userRepository.saveAll(users);

        Optional<UserEntity> user = userRepository.findById(1L);

        List<BookReviewEntity> reviews = List.of(
                new BookReviewEntity(book,user.get(),1,"No me gusto"),
                new BookReviewEntity (book,users.get(0),2,"No me gusto"),
                new BookReviewEntity(book,users.get(1),3,"No esta mal"),
                new BookReviewEntity(book,users.get(2),5,"Me encanto")
        );

        repository.saveAll(reviews);

        // When: The GET request to retrieve all reviews with a valid logged user is made.
        Response response = given()
                .headers(headers)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/reviews/get");

        // Then: We assert that we get back a status code 200.
        response.then()
                .statusCode(200)
                .log()
                .body(true);

        /* And: That the review text of the first review in the JSON response
                is the same as the review text of the first review on the reviews list.
         */
        response.then()
                .body("reviews[0].review",equalTo(reviews.get(0).getReview()));
    }
}
