CREATE DATABASE IF NOT EXISTS `book_nook`;

-- book_nook.books definition

CREATE TABLE `books`
(
    `book_id`     bigint NOT NULL AUTO_INCREMENT,
    `cover`       varchar(255)   DEFAULT NULL,
    `created_at`  datetime(6)    DEFAULT NULL,
    `description` varchar(255)   DEFAULT NULL,
    `price`       decimal(38, 2) DEFAULT NULL,
    `title`       varchar(255)   DEFAULT NULL,
    `updated_at`  datetime(6)    DEFAULT NULL,
    PRIMARY KEY (`book_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;