# Book Nook API

- This is a REST API of a project called "Book Nook" made with Spring Boot + MySQL.
- Authentication and Authorization is controlled via JWT.
- Includes Unit testing of services via Junit and Integration testing of controllers via RestAssured + TestContainers (Docker).
- Also, API documentation via the swagger and openAPI Springdocs package.
- This project is inspired by Good Reads. An app that let the user have custom shelves with their favorite books and be able to review them.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

```
- Java 17
- MySQL 8
- Maven
- Docker (for testing purposes)
```

### Installing

A step by step series of examples that tell you how to get a development env running

1 - Git clone the repository

```
$ git clone https://github.com/jfontdev/book-nook-spring.git
```

2 - Install the dependencies of the project

```
$ mvn clean install
```
3 - Create an empty MySQL DB

4 - Create an environment properties file called **env.properties** on the root of the project and fill the next list of properties from your MySQL DB also the JWT secret you want to use and the expiry time of the token in seconds.
```
DB_HOST=
DB_DATABASE=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRE=
```
5 - Run the project, the first time Spring will generate the tables in your DB according to the models.

6 - Visit "http://localhost:8080/swagger-ui/index.html" to get a ui representation of the endpoints and schemas of our application.
You can also get a JSON representation via "http://localhost:8080/v3/api-docs".

7 - Explore the different endpoints, register a user, log in and enjoy the app :)

## Running the tests

To run the tests use the maven test command to run them all.

```
$ mvn test
```
## Built With

* [Spring Boot](https://spring.io/projects/spring-boot) - The web framework used.
* [MySQL](https://dev.mysql.com/downloads/mysql/8.0.html) - The database used.
* [Maven](https://maven.apache.org/) - Dependency Management.
* [JJWT](https://github.com/jwtk/jjwt) - Used to auth and authorize via JWT implementation in Java.
* [Junit](https://junit.org/junit5/) - Used to do unit testing across the project.
* [RestAssured](https://rest-assured.io/) - Used to do integration testing of REST endpoints.
* [TestContainers](https://testcontainers.com/guides/getting-started-with-testcontainers-for-java/) - Used to containerize via docker our MySQL DB to run tests on it instead of our real DB. 
* [Springdoc](https://springdoc.org/) - Used to generate documentation of the REST API endpoints and schema.
