SET NAMES utf8mb4;

CREATE TABLE users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    created_at DATETIME    NULL,
    updated_at DATETIME    NULL,
    uuid       CHAR(36)     NOT NULL,
    email      VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_uuid (uuid),
    UNIQUE KEY uq_users_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE recipes
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    recipe_json TEXT        NOT NULL,
    created_at DATETIME     NULL,
    updated_at DATETIME     NULL,

    PRIMARY KEY (id),
    KEY idx_recipes_user_id (user_id),
    CONSTRAINT fk_recipes_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_settings
(
    id              BIGINT                                    NOT NULL AUTO_INCREMENT,
    user_id         BIGINT                                    NOT NULL,
    uuid            CHAR(36)                                  NOT NULL,
    units_mode      ENUM ('METRIC', 'IMPERIAL', 'ORIGINAL')  NOT NULL DEFAULT 'ORIGINAL',
    target_language ENUM ('ENGLISH', 'GERMAN', 'RUSSIAN', 'ORIGINAL') NOT NULL DEFAULT 'ORIGINAL',

    PRIMARY KEY (id),
    UNIQUE KEY uq_user_settings_uuid (uuid),
    UNIQUE KEY uq_user_settings_user_id (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
