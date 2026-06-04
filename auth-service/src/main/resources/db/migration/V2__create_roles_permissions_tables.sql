-- ==============================================================================
-- V2: Create roles, permissions, user_roles, role_permissions tables
-- Auth Service - Enterprise LMS
-- ==============================================================================

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id              CHAR(36)        NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255)    NULL,
    is_system_role  TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(255)    NULL,
    updated_by      VARCHAR(255)    NULL,

    CONSTRAINT pk_roles         PRIMARY KEY (id),
    CONSTRAINT uq_roles_name    UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id              CHAR(36)        NOT NULL,
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(255)    NULL,
    resource        VARCHAR(100)    NOT NULL,
    action          VARCHAR(50)     NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(255)    NULL,
    updated_by      VARCHAR(255)    NULL,

    CONSTRAINT pk_permissions       PRIMARY KEY (id),
    CONSTRAINT uq_permissions_name  UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User-to-Role mapping (many-to-many)
CREATE TABLE IF NOT EXISTS user_roles (
    id              CHAR(36)        NOT NULL,
    user_id         CHAR(36)        NOT NULL,
    role_id         CHAR(36)        NOT NULL,
    assigned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      DATETIME        NULL,
    assigned_by     CHAR(36)        NULL,

    CONSTRAINT pk_user_roles                PRIMARY KEY (id),
    CONSTRAINT uq_user_roles_user_role      UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user_id        FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role_id        FOREIGN KEY (role_id)   REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Role-to-Permission mapping (many-to-many)
CREATE TABLE IF NOT EXISTS role_permissions (
    id              CHAR(36)        NOT NULL,
    role_id         CHAR(36)        NOT NULL,
    permission_id   CHAR(36)        NOT NULL,
    assigned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by     CHAR(36)        NULL,

    CONSTRAINT pk_role_permissions              PRIMARY KEY (id),
    CONSTRAINT uq_role_permissions_role_perm    UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role_id      FOREIGN KEY (role_id)       REFERENCES roles(id)       ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_perm_id      FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for join performance
CREATE INDEX idx_user_roles_user_id    ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id    ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);

-- ==============================================================================
-- Seed Default System Roles
-- ==============================================================================
INSERT INTO roles (id, name, description, is_system_role, created_by)
VALUES
    (UUID(), 'ROLE_SUPER_ADMIN', 'Full system access including tenant management.', 1, 'SYSTEM'),
    (UUID(), 'ROLE_ADMIN',       'Administrative access to manage users, courses, and content.', 1, 'SYSTEM'),
    (UUID(), 'ROLE_INSTRUCTOR',  'Can create and manage their own courses and assessments.', 1, 'SYSTEM'),
    (UUID(), 'ROLE_STUDENT',     'Can enroll in courses and track own learning progress.', 1, 'SYSTEM');
