SET NAMES utf8mb4;

CREATE TABLE users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    created_at DATETIME    NULL,
    updated_at DATETIME    NULL,
    uuid       CHAR(36)     NOT NULL,
    email      VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_uuid (uuid),
    UNIQUE KEY uq_users_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
