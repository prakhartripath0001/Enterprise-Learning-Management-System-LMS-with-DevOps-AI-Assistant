-- ==============================================================================
-- V1: Create users table
-- Auth Service - Enterprise LMS
-- ==============================================================================

CREATE TABLE IF NOT EXISTS users (
    id                          CHAR(36)        NOT NULL,
    email                       VARCHAR(255)    NOT NULL,
    username                    VARCHAR(100)    NOT NULL,
    password_hash               VARCHAR(255)    NOT NULL,
    first_name                  VARCHAR(100)    NOT NULL,
    last_name                   VARCHAR(100)    NOT NULL,
    is_enabled                  TINYINT(1)      NOT NULL DEFAULT 1,
    is_account_non_locked       TINYINT(1)      NOT NULL DEFAULT 1,
    is_credentials_non_expired  TINYINT(1)      NOT NULL DEFAULT 1,
    failed_login_attempts       INT             NOT NULL DEFAULT 0,
    account_locked_until        DATETIME        NULL,
    last_login_at               DATETIME        NULL,
    email_verified_at           DATETIME        NULL,
    is_deleted                  TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at                  DATETIME        NULL,
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                  VARCHAR(255)    NULL,
    updated_by                  VARCHAR(255)    NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_users_email      ON users(email);
CREATE INDEX idx_users_username   ON users(username);
CREATE INDEX idx_users_is_deleted ON users(is_deleted);
