# Database Guidelines

This document outlines the database conventions, migration practices, and performance standards for all relational database stores (principally MySQL 8.0) used in the LMS.

## 1. MySQL Naming Conventions

To ensure consistent database design across different services, the following naming patterns must be enforced:

| Object Type | Case / Pattern | Example |
| :--- | :--- | :--- |
| **Tables** | Lowercase Snake Case (plural) | `users`, `course_sections`, `enrollments` |
| **Columns** | Lowercase Snake Case | `first_name`, `created_at`, `course_id` |
| **Primary Keys** | Lowercase, singular | `id` (usually bigint auto-increment / uuid) |
| **Foreign Keys** | Singular table name + `_id` | `user_id` referencing `users.id` |
| **Foreign Key Constraint** | `fk_<source_table>_<target_table>` | `fk_enrollments_users` |
| **Unique Indexes** | `uq_<table_name>_<column_name>` | `uq_users_email` |
| **Indices** | `idx_<table_name>_<column_name(s)>` | `idx_courses_status_created` |

---

## 2. Database Migration Strategy (Flyway)

We use **Flyway** to manage database schema evolutions. Direct schema modifications (manual SQL execution on database servers) are strictly forbidden.

### Versioning Rules
- Migration files are stored under `src/main/resources/db/migration/`.
- File naming convention: `V<Year><Month><Day><Hour><Minute>__<ShortDescription>.sql` (e.g., `V202606051030__create_users_table.sql`).
- This timestamp-based versioning prevents conflict merges when multiple developers check in migrations simultaneously.

### Rules for Writing Migrations
- **Write Idempotent Scripts**: Ensure migrations run safely.
- **No Schema Degradation**: Avoid SQL scripts that drop columns or tables in a way that destroys production data. Standardize on database expansions first, then write clean deprecation migrations when features are phased out.
- **Flyway Baseline**: When initializing database schemas for the first time, use `spring.flyway.baseline-on-migrate=true` in `application.properties`.

---

## 3. Indexing Rules

Unindexed columns can lead to database table scans, locks, and low query performance. Follow these guidelines:

- **Foreign Keys**: Always create an index on foreign key columns. MySQL does this automatically when creating foreign key constraints, but do not drop them.
- **Query Filter Optimization**: Analyze typical query filters (e.g., `WHERE status = ? AND category = ?`) and create composite indexes matching the most frequent filters.
- **Left-Prefix Rule**: When creating compound indices (e.g., index on `(a, b)`), verify that your query structure filters on `a` first, otherwise the index cannot be utilized.
- **Avoid Excessive Indexing**: Do not index fields with low cardinality (e.g., boolean columns like `is_active` or `is_deleted`) unless they are part of a composite index. Over-indexing slows down `INSERT`, `UPDATE`, and `DELETE` operations.
- **Index Naming**: Always explicitly name indexes rather than letting the DBMS generate random hashes.
