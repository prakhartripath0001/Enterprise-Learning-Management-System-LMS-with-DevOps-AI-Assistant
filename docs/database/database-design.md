# Relational Database Schema Design Spec

This document describes the schema design, tables structure, indices, constraints, and security considerations for the database-per-service topology.

---

## 1. Database Topology
AetherLMS enforces the **database-per-service** pattern. The `auth-service` owns its dedicated schema `lms_db` (containing identity, auditing, and credentials tables). Other services, such as `course-service`, utilize independent, isolated databases (e.g. `lms_courses_db`) to prevent shared DB coupling.

---

## 2. Table Schemas (`auth-service`)

### Table: `users`
Tracks individual user identities and status flags.
*   **Columns**:
    *   `id`: `VARCHAR(36)` - Primary Key (UUID).
    *   `first_name`: `VARCHAR(50)` - NOT NULL.
    *   `last_name`: `VARCHAR(50)` - NOT NULL.
    *   `email`: `VARCHAR(100)` - NOT NULL, UNIQUE.
    *   `username`: `VARCHAR(50)` - NOT NULL, UNIQUE.
    *   `password_hash`: `VARCHAR(255)` - NOT NULL (Bcrypt hashed password).
    *   `enabled`: `BOOLEAN` - DEFAULT TRUE (Soft deletion and deactivation flag).
    *   `created_at`: `TIMESTAMP` - DEFAULT CURRENT_TIMESTAMP.
    *   `updated_at`: `TIMESTAMP` - DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP.

### Table: `roles`
Registers security workspace contexts.
*   **Columns**:
    *   `id`: `BIGINT` - Primary Key, Auto Increment.
    *   `name`: `VARCHAR(50)` - NOT NULL, UNIQUE (`ROLE_STUDENT`, `ROLE_INSTRUCTOR`, `ROLE_ADMIN`).

### Table: `permissions`
Registers atomic security authorities.
*   **Columns**:
    *   `id`: `BIGINT` - Primary Key, Auto Increment.
    *   `name`: `VARCHAR(50)` - NOT NULL, UNIQUE (`READ_COURSES`, `WRITE_COURSES`, `DELETE_USERS`).

### Table: `role_permissions`
Many-to-Many join table mapping permissions to roles.
*   **Columns**:
    *   `role_id`: `BIGINT` - Foreign Key (`roles.id`), NOT NULL.
    *   `permission_id`: `BIGINT` - Foreign Key (`permissions.id`), NOT NULL.
*   **Constraints**:
    *   Primary Key on (`role_id`, `permission_id`).

### Table: `user_roles`
Many-to-Many join table mapping users to roles.
*   **Columns**:
    *   `user_id`: `VARCHAR(36)` - Foreign Key (`users.id`), NOT NULL.
    *   `role_id`: `BIGINT` - Foreign Key (`roles.id`), NOT NULL.
*   **Constraints**:
    *   Primary Key on (`user_id`, `role_id`).

### Table: `refresh_tokens`
Tracks active sessions for security refresh rotations.
*   **Columns**:
    *   `id`: `BIGINT` - Primary Key, Auto Increment.
    *   `token`: `VARCHAR(255)` - NOT NULL, UNIQUE.
    *   `user_id`: `VARCHAR(36)` - Foreign Key (`users.id`), NOT NULL.
    *   `expiry_date`: `TIMESTAMP` - NOT NULL.

### Table: `password_reset_tokens`
Tracks single-use password reset verification.
*   **Columns**:
    *   `id`: `BIGINT` - Primary Key, Auto Increment.
    *   `token`: `VARCHAR(255)` - NOT NULL, UNIQUE.
    *   `user_id`: `VARCHAR(36)` - Foreign Key (`users.id`), NOT NULL.
    *   `expiry_date`: `TIMESTAMP` - NOT NULL.

### Table: `login_audit_logs`
Audits login attempts for security reviews.
*   **Columns**:
    *   `id`: `BIGINT` - Primary Key, Auto Increment.
    *   `email`: `VARCHAR(100)` - NOT NULL.
    *   `status`: `VARCHAR(20)` - NOT NULL (`SUCCESS`, `FAILED`).
    *   `ip_address`: `VARCHAR(45)` - NOT NULL.
    *   `timestamp`: `TIMESTAMP` - DEFAULT CURRENT_TIMESTAMP.

---

## 3. Database Constraints & Indexing Strategy
To ensure maximum read/write performance and relational integrity, we configure the following:

### Foreign Keys
All join tables and detail tables (`user_roles`, `role_permissions`, `refresh_tokens`, etc.) enforce referential constraints with `ON DELETE CASCADE` actions to maintain cleanup integrity.

### Performance Indexes
We create indexes on columns frequently used in query filters and joins:
*   `idx_users_email`: B-Tree index on `users(email)` for login verification checks.
*   `idx_users_username`: B-Tree index on `users(username)` for profile checks.
*   `idx_refresh_tokens_token`: B-Tree index on `refresh_tokens(token)` for session validation.
*   `idx_login_audit_logs_email`: B-Tree index on `login_audit_logs(email)` for fraud auditing.

---

## 4. Security Considerations
1.  **Restricted Credentials**: Never write credentials or JWT secret keys directly inside migration SQL.
2.  **No Raw Passwords**: Passwords must be hashed via BCrypt before writing to database tables.
3.  **Data Isolation**: Inter-service operations must query data via API requests (never direct cross-database SQL queries).
