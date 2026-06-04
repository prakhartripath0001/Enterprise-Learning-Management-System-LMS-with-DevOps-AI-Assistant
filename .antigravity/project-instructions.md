# Project Instructions for AI Assistants

This document serves as the master specification for all AI coding assistants working on the Enterprise Learning Management System (LMS) codebase. 

---

## 1. System Technology Stack

All modifications and new developments must use:
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.x
- **Security**: Spring Security 6.x + Stateless JWT Authentication
- **Database**: MySQL 8.0 + Flyway
- **Architecture**: Microservices Architecture (Auth, Course, User, Enrollment, API Gateway)
- **Dockerization**: Containerized deployments via Docker and Docker Compose
- **Testing**: JUnit 5 + Mockito

---

## 2. Core Architecture Rules

1. **Follow SOLID Principles**:
   - Write single-responsibility classes.
   - Design cleanly decoupled interfaces.
2. **Constructor Injection Only**:
   - Dependencies must be marked `final`.
   - Never inject using `@Autowired` on variables.
   - Use `@RequiredArgsConstructor` (Lombok) to generate constructor methods.
3. **Layered Architecture Pattern**:
   - Follow strict flow: `Controller` ➔ `Service` ➔ `Repository`.
   - Controllers handle request/response mapping and validation.
   - Services implement business logic and mapping.
   - Repositories interact directly with the database.
4. **Data Transfer Objects (DTOs)**:
   - Use Java 21 `record` types for Request and Response objects.
   - Never return raw JPA entities directly from Controllers.
5. **Jakarta Bean Validation**:
   - Validate incoming DTO parameters in Controller endpoints using annotations (e.g. `@NotBlank`, `@Size`, `@Min`).
   - Use `@Valid` on `@RequestBody` parameters.
6. **Global Exception Handling**:
   - Use `@RestControllerAdvice` to trap and translate exceptions into standard, secure JSON envelopes.
   - Prevent database detail leakage or stack traces from reaching clients.
7. **REST Naming Conventions**:
   - Use plural nouns in paths (`/api/v1/courses`).
   - Use kebab-case for URL segments.
   - Map actions strictly to the correct HTTP verbs (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
8. **Secure Password Management**:
   - Passwords must be hashed using the **BCrypt** algorithm (minimum work factor 12) before being saved.
9. **Never Hardcode Secrets**:
   - System keys, passwords, credentials, and tokens must always be read from configuration property variables (`@ConfigurationProperties`) bound to environmental variables.

---

## 3. Code Style & Maintainability

- **Composition over Inheritance**: Favor class orchestration/composition over inheritance hierarchies.
- **Short Methods**: Keep methods compact and highly focused on a single task.
- **Self-Documenting Code**: Use clean naming patterns. Comments should only explain *why* something is done, not *what* the code does.

---

## 4. Testing & Quality Assurance

- **JUnit 5 & Mockito**: Write unit tests for all service implementations.
- **Mock Repositories**: Stub database and network client calls to run tests in isolation.
- **High Coverage**: Maintain a minimum line coverage threshold of 80% (95% for security/authentication flows).

---

## 5. Git Workflow

- Prefix branch names based on scope:
  - `feature_fe/*` - Frontend modifications
  - `feature_be/*` - Backend service modifications
  - `feature_db/*` - Database schema migrations
- Use `git rebase` to keep branch histories linear.
- Enforce squash merging for clean production history.
