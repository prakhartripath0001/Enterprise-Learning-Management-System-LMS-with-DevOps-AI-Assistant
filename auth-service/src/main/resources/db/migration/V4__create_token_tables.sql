-- ==============================================================================
-- V4: Create password_reset_tokens and email_verification_tokens tables
-- Auth Service - Enterprise LMS
-- ==============================================================================

-- Password Reset Tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id              CHAR(36)        NOT NULL,
    user_id         CHAR(36)        NOT NULL,
    -- SHA-256 hash of the one-time raw token sent by email
    token_hash      VARCHAR(512)    NOT NULL,
    is_used         TINYINT(1)      NOT NULL DEFAULT 0,
    expires_at      DATETIME        NOT NULL,
    ip_address      VARCHAR(100)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_password_reset_tokens         PRIMARY KEY (id),
    CONSTRAINT uq_password_reset_tokens_hash    UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_prt_user_id    ON password_reset_tokens(user_id);
CREATE INDEX idx_prt_expires_at ON password_reset_tokens(expires_at);

-- Email Verification Tokens
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id              CHAR(36)        NOT NULL,
    user_id         CHAR(36)        NOT NULL,
    -- SHA-256 hash of the raw verification token sent by email
    token_hash      VARCHAR(512)    NOT NULL,
    is_used         TINYINT(1)      NOT NULL DEFAULT 0,
    expires_at      DATETIME        NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_email_verification_tokens         PRIMARY KEY (id),
    CONSTRAINT uq_email_verification_tokens_hash    UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_evt_user_id    ON email_verification_tokens(user_id);
CREATE INDEX idx_evt_expires_at ON email_verification_tokens(expires_at);
