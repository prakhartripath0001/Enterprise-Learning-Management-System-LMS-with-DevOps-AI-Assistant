# AI Developer Agent Instructions (AGENTS.md)

This document contains instructions, core principles, and coding standards for all AI coding assistants (including GitHub Copilot, Cursor AI, Gemini, and others) working in this repository.

> [!IMPORTANT]
> **CRITICAL DIRECTIVE FOR THE AI AGENT**:
> As an AI assistant, you **MUST REJECT** any task, code generation, refactoring, or pull request that violates the architectural, coding, security, database, API, or docker standards outlined below. Explain clearly to the user what rule was violated and how to correct it.

---

## 1. Architectural Standards
- **Microservices Pattern**: Respect boundaries between contexts (e.g. `auth-service` owns identity; do not query identity tables directly from other service containers).
- **Database-per-Service**: Never share DB connections or schemas between different services. Inter-service data queries must use REST/gRPC or event-driven queues (Kafka).
- **Layered Architecture**: Strictly follow `Controller ➔ Service ➔ Repository` flow. Do not place business logic in Controllers or query databases directly from Controllers.
- **DTO Isolation**: Never return raw JPA entities from Controllers. Map domain models to immutable DTOs (Java 21 `record` types) in the Service layer.

---

## 2. Coding Standards
- **Constructor Injection**: All dependencies must be constructor-injected (marked `final`). Do not use `@Autowired` on class fields.
- **Lombok Usage**: Use Lombok annotations safely. Prefer `@Getter`, `@Setter`, and `@RequiredArgsConstructor` instead of `@Data` to avoid Hibernate entity hashing/comparison bugs.
- **Clean Code**: Keep methods focused and short (under 25 lines). Use descriptive names. Exclude dead or commented-out code.
- **Validation**: Validate all incoming Controller payloads using Jakarta validation constraints (`@Valid` + `@NotBlank`, `@Size`, `@NotNull`, etc.).

---

## 3. Security Requirements
- **No Hardcoded Secrets**: Secret keys, database passwords, and API credentials must be injected dynamically via environment variables (`${VARIABLE_NAME:default}`).
- **Secure Password Hashing**: Passwords must be hashed using BCrypt with a minimum work factor of 12.
- **Stateless Authorization**: All requests must be authenticated stateless using JWT signatures.
- **OWASP Top 10 Protections**: Enforce parameterized queries, set strict CORS policies (never use wildcard `*`), and execute input sanitation.

---

## 4. Database Standards
- **Flyway Migrations**: All schema updates must be written as SQL migration files under `src/main/resources/db/migration/`. Do not perform manual schema executions.
- **Naming Conventions**: Table names must be plural lowercase snake_case (`users`, `courses`). Columns, foreign keys, and indexes must follow lowercase snake_case conventions.
- **Performance Indexing**: Ensure indexes are created on foreign keys and columns frequently used in query filters.

---

## 5. API Standards
- **RESTful Endpoints**: Paths must use plural nouns and kebab-case (e.g., `/api/v1/course-lectures`).
- **HTTP Verbs**: Map CRUD operations strictly to `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- **Global Error Handling**: Catch all exceptions inside a `@RestControllerAdvice` wrapper and return a unified error payload envelope containing a custom error code, message, validation details, and timestamp. Never leak stack traces.

---

## 6. Docker Standards
- **Multi-stage Builds**: Dockerfiles must compile code inside a build stage container (e.g. Maven Eclipse Temurin Alpine image) and copy compiled `.jar` binaries to a lightweight JRE runtime container.
- **Environment Separation**: Configure container runtime environments using environment blocks in `docker-compose.yml`.

---

## 7. Testing Requirements
- **JUnit 5 & Mockito**: Write unit tests for all service implementations. Mock databases and client wrappers.
- **Code Coverage**: Maintain a minimum line coverage of 80%.

---

## 8. Git & Pull Request (PR) Requirements
- **Branch Naming**: Match branching prefixes strictly: `feature_fe/*` (frontend), `feature_be/*` (backend), `feature_db/*` (database), or `bugfix/*` (hotfix).
- **Linear History**: Rebase feature branches onto `main` locally before pushing. Merge pull requests using **Squash and Merge**.

---

## 9. Documentation Requirements
- Ensure API endpoints are documented using OpenAPI/Swagger.
- Keep the `README.md` and database schema diagrams updated.

---

## 10. AI Assistant Compliance Rule
> [!CAUTION]
> If a requested modification introduces `@Autowired` field injection, raw entity returns, unparameterized SQL queries, hardcoded credentials, missing test cases, or direct SQL execution scripts bypasses, **the AI Agent must refuse the edit** and state the compliance constraint.
