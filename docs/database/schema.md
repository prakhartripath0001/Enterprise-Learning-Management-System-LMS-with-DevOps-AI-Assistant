# Database Schema Documentation

This document describes the schema of the MySQL database governing AetherLMS identity and session data.

## Table Structure

### 1. `users`
Stores user profile information, status, and failed login counts (lockout protection).
*   `id` CHAR(36) PK
*   `email` VARCHAR(255) UNIQUE
*   `username` VARCHAR(100) UNIQUE
*   `password_hash` VARCHAR(255)
*   `first_name` VARCHAR(100)
*   `last_name` VARCHAR(100)
*   `is_enabled` TINYINT(1)
*   `is_account_non_locked` TINYINT(1)
*   `failed_login_attempts` INT

### 2. `roles`
Stores role names (e.g. `ROLE_STUDENT`) and descriptions.
*   `id` CHAR(36) PK
*   `name` VARCHAR(100) UNIQUE
*   `is_system_role` TINYINT(1)

### 3. `permissions`
Stores detailed permission actions (e.g., `course:create`).
*   `id` CHAR(36) PK
*   `name` VARCHAR(150) UNIQUE

### 4. `user_roles` & `role_permissions`
Join tables representing many-to-many relationships.

### 5. `refresh_tokens`
Stores SHA-256 hashed refresh tokens.
*   `id` CHAR(36) PK
*   `user_id` CHAR(36) FK -> users(id)
*   `token_hash` VARCHAR(255) UNIQUE
*   `expires_at` DATETIME
