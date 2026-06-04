-- ==============================================================================
-- V5: Create login_audit_logs table
-- Auth Service - Enterprise LMS
-- Append-only: no UPDATE or DELETE permitted. For SOC2 / GDPR compliance.
-- ==============================================================================

CREATE TABLE IF NOT EXISTS login_audit_logs (
    id                  CHAR(36)        NOT NULL,
    -- NULL when user is not found (track failed attempts by unknown email)
    user_id             CHAR(36)        NULL,
    email_attempted     VARCHAR(255)    NULL,
    event_type          VARCHAR(50)     NOT NULL COMMENT 'LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, PASSWORD_RESET_REQUESTED, PASSWORD_RESET_COMPLETED, EMAIL_VERIFIED, TOKEN_REFRESHED, ACCOUNT_LOCKED, ACCOUNT_UNLOCKED',
    is_success          TINYINT(1)      NOT NULL DEFAULT 0,
    ip_address          VARCHAR(100)    NULL,
    user_agent          VARCHAR(500)    NULL,
    device_fingerprint  VARCHAR(255)    NULL,
    failure_reason      VARCHAR(255)    NULL COMMENT 'INVALID_CREDENTIALS, ACCOUNT_LOCKED, ACCOUNT_DISABLED, TOKEN_EXPIRED',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_login_audit_logs PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes: support security queries, admin dashboards, anomaly detection
CREATE INDEX idx_audit_user_id    ON login_audit_logs(user_id);
CREATE INDEX idx_audit_event_type ON login_audit_logs(event_type);
CREATE INDEX idx_audit_created_at ON login_audit_logs(created_at);
CREATE INDEX idx_audit_ip_address ON login_audit_logs(ip_address);
CREATE INDEX idx_audit_is_success ON login_audit_logs(is_success, created_at);
