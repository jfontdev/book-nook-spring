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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTestContainerTest {

    static MySQLContainer<?> database =
            new MySQLContainer<>("mysql:8.0.34");

    private static boolean userRegistered = false;

    @Autowired
    BookRepository repository;
    @Autowired
    BookReviewRepository reviewRepository;
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

    protected void clearAndResetBooksTable() {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            reviewRepository.deleteAll();
            reviewRepository.flush();
            repository.deleteAll();
            repository.flush();
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
        clearAndResetBooksTable();
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
    void getAllBooksShouldReturnAllBooks() {
        // Given: A valid request with a logged-in user to the GET /api/v1/books endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price = new BigDecimal("12.50");
        LocalDateTime date = LocalDateTime.now();

        List<BookEntity> books = List.of(
                new BookEntity(
                        "Portada", "Nuevo libro", "Un gran libro", price, date, date),
                new BookEntity(
                        "Portada 1", "Nuevo libro 2", "Un gran libro 2", price, date, date),
                new BookEntity(
                        "Portada 2", "Nuevo libro 3", "Un gran libro 3", price, date, date)
        );
        repository.saveAll(books);
        // When: The GET request to retrieve all books is made.
        Response response = given()
                .headers(headers)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/books");

        // Then: We assert that we get a status code 200 back.
        response.then()
                .statusCode(200);

        // And: We assert that the body has a size of 3 elements.
        response.then()
                .body(".", hasSize(3))
                .log()
                .body(true);

        // Finally: We assert that the titles of the JSON objects is the same as the titles of the "books" objects.
        response.then()
                .body("[0].title", equalTo(books.get(0).getTitle()))
                .body("[1].title", equalTo(books.get(1).getTitle()))
                .body("[2].title", equalTo(books.get(2).getTitle()));
    }

    @Test
    void getBookShouldReturnABook() {
        // Given: A valid request with a logged-in user to the GET /api/v1/books/{book-id}/get endpoint.
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
        repository.save(book);

        // When: The GET request to get a book by id is made.
        Response response = given()
                .headers(headers)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/books/1/get");

        // Then: We assert that we get a status code 200 back.
        response.then()
                .statusCode(200)
                .log()
                .body(true);


        // And: We assert that the title of the JSON object is the same as the title of the "book" object.
        response.then()
                .body("title", equalTo(book.getTitle()));
    }

    @Test
    void searchBookShouldReturnABook()  {
        // Given: A valid request with a logged-in user to the POST /api/v1/books/search endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price = new BigDecimal("12.50");
        LocalDateTime date = LocalDateTime.now();

        List<BookEntity> books = List.of(
                new BookEntity(
                        "Portada", "Nuevo libro z", "Un gran libro", price, date, date),
                new BookEntity(
                        "Portada 1", "Nuevo libro x", "Un gran libro 2", price, date, date),
                new BookEntity(
                        "Portada 2", "Nuevo libro c", "Un gran libro 3", price, date, date)
        );
        repository.saveAll(books);

        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("value", "x");

        // When: The POST request to search a book with the body that contains the search value.
        Response response = given()
                .headers(headers)
                .body(requestBody.toString())
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/books/search");

        // Then: We assert that we get a status code 200 back.
        response.then()
                .statusCode(200)
                .log()
                .body(true);


        // And: We assert that the title of the JSON object is the same as the title of the "book" object.
        response.then()
                .body("[0].title", equalTo(books.get(1).getTitle()));
    }

    @Test
    void sortedBooksShouldReturnABookListOrderedByPriceAscendant()  {
        // Given: A valid request with a logged-in user to the POST /api/v1/books/sorted endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price1 = new BigDecimal("12.50");
        BigDecimal price2 = new BigDecimal("21.50");
        BigDecimal price3 = new BigDecimal("15.50");
        LocalDateTime date = LocalDateTime.now();

        List<BookEntity> books = List.of(
                new BookEntity(
                        "Portada", "Nuevo libro z", "Un gran libro", price1, date, date),
                new BookEntity(
                        "Portada 1", "Nuevo libro x", "Un gran libro 2", price2, date, date),
                new BookEntity(
                        "Portada 2", "Nuevo libro c", "Un gran libro 3", price3, date, date)
        );
        repository.saveAll(books);

        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("sortBy", "priceAsc");

        // When: The POST request to sort a book with the body that contains the sorting value.
        Response response = given()
                .headers(headers)
                .body(requestBody.toString())
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/books/sorted");

        // Then: We assert that we get a status code 200 back.
        response.then()
                .statusCode(200)
                .log()
                .body(true);


        // And: We assert that the order of the JSON objects list are ordered by Price ascendant comparing the titles.
        response.then()
                .body("[0].title", equalTo(books.get(0).getTitle()))
                .body("[1].title", equalTo(books.get(2).getTitle()))
                .body("[2].title", equalTo(books.get(1).getTitle()));
    }

    @Test
    void sortedBooksShouldReturnABookListOrderedByPriceDescendant()  {
        // Given: A valid request with a logged-in user to the POST /api/v1/books/sorted endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price1 = new BigDecimal("12.50");
        BigDecimal price2 = new BigDecimal("21.50");
        BigDecimal price3 = new BigDecimal("15.50");
        LocalDateTime date = LocalDateTime.now();

        List<BookEntity> books = List.of(
                new BookEntity(
                        "Portada", "Nuevo libro z", "Un gran libro", price1, date, date),
                new BookEntity(
                        "Portada 1", "Nuevo libro x", "Un gran libro 2", price2, date, date),
                new BookEntity(
                        "Portada 2", "Nuevo libro c", "Un gran libro 3", price3, date, date)
        );
        repository.saveAll(books);

        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("sortBy", "priceDesc");

        // When: The POST request to sort a book with the body that contains the sorting value.
        Response response = given()
                .headers(headers)
                .body(requestBody.toString())
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/books/sorted");

        // Then: We assert that we get a status code 200 back.
        response.then()
                .statusCode(200)
                .log()
                .body(true);


        // And: We assert that the order of the JSON objects list are ordered by Price descendant comparing the titles.
        response.then()
                .body("[0].title", equalTo(books.get(1).getTitle()))
                .body("[1].title", equalTo(books.get(2).getTitle()))
                .body("[2].title", equalTo(books.get(0).getTitle()));
    }

    @Test
    void sortedBooksShouldReturnABookListOrderedByRatingsAscendant()  {
        // Given: A valid request with a logged-in user to the POST /api/v1/books/sorted endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price = new BigDecimal("12.50");

        LocalDateTime date = LocalDateTime.now();

        List<BookEntity> books = List.of(
                new BookEntity(
                        "Portada", "Nuevo libro z", "Un gran libro", price, date, date),
                new BookEntity(
                        "Portada 1", "Nuevo libro x", "Un gran libro 2", price, date, date),
                new BookEntity(
                        "Portada 2", "Nuevo libro c", "Un gran libro 3", price, date, date)
        );

        repository.saveAll(books);

        Optional<UserEntity> user = userRepository.findById(1L);

        List<BookReviewEntity> reviews = List.of(
                new BookReviewEntity(books.get(0),user.get(),1,"No me gusto"),
                new BookReviewEntity (books.get(0),user.get(),2,"No me gusto"),
                new BookReviewEntity(books.get(1),user.get(),3,"No esta mal"),
                new BookReviewEntity(books.get(2),user.get(),2,"No me gusto")
        );

        reviewRepository.saveAll(reviews);

        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("sortBy", "ratingsAsc");

        // When: The POST request to sort a book with the body that contains the sorting value.
        Response response = given()
                .headers(headers)
                .body(requestBody.toString())
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/books/sorted");

        // Then: We assert that we get a status code 200 back.
        response.then()
                .statusCode(200)
                .log()
                .body(true);


        // And: We assert that the order of the JSON objects list are ordered by Ratings ascendant comparing the titles.
        response.then()
                .body("[0].title", equalTo(books.get(0).getTitle()))
                .body("[1].title", equalTo(books.get(2).getTitle()))
                .body("[2].title", equalTo(books.get(1).getTitle()));
    }


    @Test
    void sortedBooksShouldReturnABookListOrderedByRatingsDescendant()  {
        // Given: A valid request with a logged-in user to the POST /api/v1/books/sorted endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price = new BigDecimal("12.50");

        LocalDateTime date = LocalDateTime.now();

        List<BookEntity> books = List.of(
                new BookEntity(
                        "Portada", "Nuevo libro z", "Un gran libro", price, date, date),
                new BookEntity(
                        "Portada 1", "Nuevo libro x", "Un gran libro 2", price, date, date),
                new BookEntity(
                        "Portada 2", "Nuevo libro c", "Un gran libro 3", price, date, date)
        );

        repository.saveAll(books);

        Optional<UserEntity> user = userRepository.findById(1L);

        List<BookReviewEntity> reviews = List.of(
                new BookReviewEntity(books.get(0),user.get(),1,"No me gusto"),
                new BookReviewEntity (books.get(0),user.get(),2,"No me gusto"),
                new BookReviewEntity(books.get(1),user.get(),3,"No esta mal"),
                new BookReviewEntity(books.get(2),user.get(),2,"No me gusto")
        );

        reviewRepository.saveAll(reviews);

        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("sortBy", "ratingsDesc");

        // When: The POST request to sort a book with the body that contains the sorting value.
        Response response = given()
                .headers(headers)
                .body(requestBody.toString())
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/books/sorted");

        // Then: We assert that we get a status code 200 back.
        response.then()
                .statusCode(200)
                .log()
                .body(true);


        // And: We assert that the order of the JSON objects list are ordered by Ratings descendant comparing the titles.
        response.then()
                .body("[0].title", equalTo(books.get(1).getTitle()))
                .body("[1].title", equalTo(books.get(2).getTitle()))
                .body("[2].title", equalTo(books.get(0).getTitle()));
    }


    @Test
    void sortedBooksShouldReturnABookListWithAllTheBooksWhenSortIsNotFound()  {
        // Given: A valid request with a logged-in user to the POST /api/v1/books/sorted endpoint.
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Accept", "application/json");
                put("Authorization", "Bearer " + token);
            }
        };

        BigDecimal price = new BigDecimal("12.50");

        LocalDateTime date = LocalDateTime.now();

        List<BookEntity> books = List.of(
                new BookEntity(
                        "Portada", "Nuevo libro z", "Un gran libro", price, date, date),
                new BookEntity(
                        "Portada 1", "Nuevo libro x", "Un gran libro 2", price, date, date),
                new BookEntity(
                        "Portada 2", "Nuevo libro c", "Un gran libro 3", price, date, date)
        );

        repository.saveAll(books);

        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("sortBy", "");

        // When: The POST request to sort a book with the body that contains no sorting value.
        Response response = given()
                .headers(headers)
                .body(requestBody.toString())
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/books/sorted");

        // Then: We assert that we get a status code 200 back.
        response.then()
                .statusCode(200)
                .log()
                .body(true);


        // And: We assert that the JSON objects list are returned as is without sort by comparing the titles.
        response.then()
                .body("[0].title", equalTo(books.get(0).getTitle()))
                .body("[1].title", equalTo(books.get(1).getTitle()))
                .body("[2].title", equalTo(books.get(2).getTitle()));
    }
}
