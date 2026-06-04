# Testing Standards

This document establishes the testing expectations, frameworks, and patterns required to build a reliable and robust Enterprise LMS platform.

## 1. Unit Testing with JUnit 5

JUnit 5 (Jupiter) is our standard unit testing framework.

### Best Practices
- **Naming Conventions**: Test method names must be descriptive and follow the `methodName_givenCondition_expectedBehavior` pattern.
  ```java
  @Test
  void findByEmail_givenExistingEmail_returnsUser() {
      // arrange, act, assert
  }
  ```
- **Structure (AAA Pattern)**: Structure your test logic using clear phases:
  - **Arrange**: Set up mocks, inputs, and database states.
  - **Act**: Call the method under test.
  - **Assert**: Verify outputs, assertions, and mock interactions.
- **Assertions**: Use JUnit 5 `Assertions` (or AssertJ's fluid `assertThat`) for expressing expectations.
  ```java
  // AssertJ style (recommended)
  assertThat(result).isNotNull();
  assertThat(result.getEmail()).isEqualTo("student@lms.com");
  ```

---

## 2. Mocking with Mockito

Mockito is the default framework for isolation in unit tests.

### Rules
- **Mock Dependencies**: Mock all database repositories, external HTTP clients, and secondary services inside Service unit tests.
- **Strict Stubbing**: Ensure Mockito's strict stubbing is enabled to find unused or incorrect mock setup issues early (default in modern Mockito/JUnit extension).
- **Avoid Over-Mocking**: Do not mock utility classes, simple DTOs, or domain collections. Construct them using constructors or builders.
- **Example Usage**:
  ```java
  @ExtendWith(MockitoExtension.class)
  class UserServiceTest {
  
      @Mock
      private UserRepository userRepository;
  
      @InjectMocks
      private UserServiceImpl userService;
  
      @Test
      void getUserById_existingId_returnsUser() {
          User user = new User(1L, "user@lms.com");
          when(userRepository.findById(1L)).thenReturn(Optional.of(user));
  
          UserDto result = userService.getUserById(1L);
  
          assertThat(result).isNotNull();
          verify(userRepository, times(1)).findById(1L);
      }
  }
  ```

---

## 3. Integration & API Testing

### Spring Boot Integration Tests
- Use `@SpringBootTest` alongside `@ActiveProfiles("test")` for tests requiring a running application context.
- Use **Testcontainers** to spin up real lightweight instances of MySQL/Kafka during integration tests rather than using H2 in-memory databases, avoiding vendor-specific SQL discrepancies.

### API Validation with Postman
- All microservices must include a Postman collection JSON file in their respective service directory (e.g., `auth-service/postman_collection.json`).
- Collections should contain environmental variables and automated tests validating:
  - Status codes (`200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`).
  - Response JSON structure and non-null values.

---

## 4. Minimum Test Coverage Expectations

We enforce automated test coverage gates during continuous integration (CI) pipelines using JaCoCo.

- **Line Coverage**: Minimum **80%**
- **Branch Coverage**: Minimum **70%**

> [!IMPORTANT]
> Business-critical pathways (Authentication/JWT generation, payment processing, enrollment validations) require **95%+** coverage. Exclude configuration classes, exception models, and DTOs from coverage reports to ensure metrics represent business logic.
