# Coding Standards

This document establishes the coding conventions, language standards, and project design patterns for the Enterprise LMS backend services. 

## 1. Technology Baseline

All services must use the following technology stack as their baseline:
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven 3.9+ or Gradle 8+

### Java 21 Standards
- **Use Records for DTOs**: Data Transfer Objects must be declared as `record` types to ensure immutability and reduce boilerplates.
  ```java
  public record UserResponseDto(Long id, String email, String fullName, Set<String> roles) {}
  ```
- **Pattern Matching**: Utilize pattern matching in `switch` expressions and `instanceof` statements to keep code clean and readable.
- **Virtual Threads**: For I/O-bound microservices, configure the application to utilize Virtual Threads (Loom) in Spring Boot by adding:
  ```properties
  spring.threads.virtual.enabled=true
  ```

---

## 2. Spring Boot 3 Guidelines

- **Configuration Properties**: Bind application properties to classes using `@ConfigurationProperties` instead of sprinkling `@Value` throughout the codebase.
- **Dependency Injection**: Always use **constructor injection** instead of field injection (`@Autowired` on variables). This makes testing easier and guarantees immutable dependencies.
  ```java
  @Service
  @RequiredArgsConstructor // Automatically generates constructor for final fields (Lombok)
  public class UserService {
      private final UserRepository userRepository;
  }
  ```
- **Lombok Usage**: Use Lombok annotations with care. Prefer `@Getter`, `@Setter`, and `@RequiredArgsConstructor` over `@Data` to avoid issues with JPA entity equals/hashCode methods.

---

## 3. Clean Code & SOLID Principles

### Clean Code Guidelines
- **Self-Documenting Code**: Choose descriptive variable, class, and method names. Minimize code comments that explain *what* the code does; write comments only to explain *why* non-obvious code exists.
- **Small Methods**: Keep methods under 20 lines where possible. A method should do one thing, and one thing only (Single Responsibility).
- **Avoid Deep Nesting**: Use guard clauses to exit early and avoid deep `if-else` branching.
  ```java
  // Good: Guard clause
  if (user == null) {
      throw new UserNotFoundException();
  }
  processUser(user);
  ```

### SOLID Principles in Action
- **Single Responsibility (SRP)**: Do not perform business logic inside Controller classes or Database repositories. Controllers handle request/response mapping; Services orchestrate business logic; Repositories handle database retrieval.
- **Interface Segregation (ISP)**: Create small, focused interfaces rather than single massive interfaces with dozens of methods.
- **Dependency Inversion (DIP)**: High-level modules should depend on abstractions, not concrete implementations. Program to interfaces (e.g., `PaymentService` interface implemented by `StripePaymentService`).

---

## 4. Package Structure Conventions

Every Spring Boot microservice must follow the package structure layout shown below:

```text
com.lms.service_name
│
├── config                 # Configuration classes (Security, Database, CORS, Swagger)
├── controller             # REST Controllers / Endpoints
├── service                # Business Logic interfaces and implementations
│   └── impl               # Service implementations
├── repository             # Spring Data Repositories / DAOs
├── model                  # JPA Entities / Domain Models
├── dto                    # Request/Response DTOs (using Records)
├── exception              # Custom domain exceptions and GlobalExceptionHandler
└── client                 # External API client integrations (Feign, WebClient)
```
