package com.jordi.booknook;

import com.jordi.booknook.models.BookEntity;
import com.jordi.booknook.repositories.BookRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.*;
import static com.jordi.booknook.BookRepositoryTestContainerTest.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace =Replace.NONE)
@ContextConfiguration(initializers = DataSourceInitializer.class)
public class BookRepositoryTestContainerTest {
    @Autowired
    BookRepository repository;

    @Container
    static final MySQLContainer<?> database =
            new MySQLContainer<>("mysql:8.0.34")
                    .withUsername("root");

    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext){
            TestPropertySourceUtils
                    .addInlinedPropertiesToEnvironment(
                            applicationContext,
                            "spring.datasource.url=" + database.getJdbcUrl(),
                            "spring.datasource.username=" + database.getUsername(),
                            "spring.datasource.password=" + database.getPassword(),
                            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect",
                            "spring.jpa.hibernate.ddl-auto=create-drop"
                    );
        }
    }

    @BeforeEach
    void setUp(){
        BigDecimal price = new BigDecimal("12.50");
        LocalDateTime date = LocalDateTime.now();

        repository.saveAll(
                List.of(
                        new BookEntity(
                                "Portada","Nuevo libro", "Un gran libro",price,date,date),
                        new BookEntity(
                "Portada 1","Nuevo libro 2", "Un gran libro 2",price,date,date),
                        new BookEntity(
                "Portada 2","Nuevo libro 3", "Un gran libro 3",price,date,date)
                )
        );
    }

    @AfterAll
    static void afterAll(){
        database.stop();
    }

    @Test
    void findAllShouldReturnAllBooks(){
        List<BookEntity> books = repository.findAll();
        assertThat(books).hasSize(3);
    }
}
