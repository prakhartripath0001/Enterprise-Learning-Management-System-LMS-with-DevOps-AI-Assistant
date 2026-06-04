-- ==============================================================================
-- V3: Create refresh_tokens table
-- Auth Service - Enterprise LMS
-- Supports multi-device sessions and token rotation anti-replay controls
-- ==============================================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                  CHAR(36)        NOT NULL,
    user_id             CHAR(36)        NOT NULL,
    -- SHA-256 hash of the raw refresh token; never store plaintext
    token_hash          VARCHAR(512)    NOT NULL,
    device_fingerprint  VARCHAR(255)    NULL,
    device_type         VARCHAR(50)     NULL COMMENT 'WEB, MOBILE, DESKTOP, API',
    ip_address          VARCHAR(100)    NULL,
    user_agent          VARCHAR(500)    NULL,
    is_revoked          TINYINT(1)      NOT NULL DEFAULT 0,
    expires_at          DATETIME        NOT NULL,
    last_used_at        DATETIME        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_refresh_tokens            PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash       UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user_id    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes: support cleanup jobs, revocation lookups, and user session queries
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked    ON refresh_tokens(is_revoked, expires_at);
