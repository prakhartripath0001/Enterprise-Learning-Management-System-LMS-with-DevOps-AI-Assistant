# Enterprise Learning Management System

## Overview
The Enterprise Learning Management System (LMS) is a production-grade, highly scalable, and secure platform designed for enterprise-level training, course delivery, and enrollment management. It is built using a microservices-based architecture with Java 21, Spring Boot 3, and Spring Security 6 on the backend, and a modern, responsive single-page React application powered by Vite and Tailwind CSS on the frontend. The entire application is orchestrated using Docker and Docker Compose, ensuring identical development, staging, and production environments.

### Business Goals
*   **Scalability**: Leverage containerized microservices to scale database and service boundaries independently.
*   **Security Compliance**: Adhere to OWASP Top 10 guidelines with secure authentication protocols and stateless JWT authorization.
*   **High Performance**: Implement fast Vite builds, optimized frontend assets, HMR file polling, and performance indexes on persistent relational tables.
*   **Continuous Governance**: Enforce code quality gates, automated static analysis check tasks, and robust CI/CD check gates.

---

## Features
*   **Authentication**: Secure user login and registration driven by stateless JWT access tokens and HttpOnly cookie-based refresh token rotation.
*   **Authorization**: Granular Role-Based Access Control (RBAC) separating student, instructor, and admin workspaces.
*   **Course Management**: Create, view, and organize courses. Dynamic listings utilizing search, filter, and table pagination components.
*   **Enrollment Management**: Seamless self-service enrollments and administrative override controls.
*   **Learning Dashboard**: Dedicated dashboard containing stats widgets, course progress indicators, and certification history.
*   **Microservices Pattern**: Database-per-service isolation separating identity, authorization, and course data.
*   **Docker Orchestration**: Complete developer orchestration with automated DB health checks and code volumes hot-reloads.

---

## Technology Stack

### Frontend
*   **Framework**: React 19
*   **Build System**: Vite 8
*   **Styling**: Tailwind CSS v3, PostCSS, Autoprefixer
*   **Routing**: React Router v6

### Backend
*   **Core**: Java 21 / Spring Boot 3
*   **Security**: Spring Security 6, JWT Authentication, BCrypt Hashing (Work Factor 12)
*   **Migration**: Flyway

### Database
*   **Engine**: MySQL 8.0
*   **In-Memory (Tests)**: H2 Database

### Infrastructure
*   **Containerization**: Docker
*   **Orchestration**: Docker Compose

### CI/CD & Static Analysis
*   **Pipeline**: GitHub Actions
*   **Backend Quality**: Checkstyle, PMD, SpotBugs
*   **Frontend Quality**: ESLint, Prettier

### Testing
*   **Frontend**: Vitest, React Testing Library, jsdom
*   **Backend**: JUnit 5, Mockito, MockMvc, JaCoCo

---

## Project Structure
```
.
├── .github/
│   ├── ISSUE_TEMPLATE/       # Automated GitHub Issue templates
│   ├── workflows/            # Multi-pipeline GitHub Actions CI yml workflows
│   ├── CODEOWNERS            # Pull request review assignment settings
│   └── PULL_REQUEST_TEMPLATE.md
├── auth-service/             # Identity & Access Management backend service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/         # Layered Controller ➔ Service ➔ Repository flow
│   │   │   └── resources/    # Properties and db Flyway migrations
│   │   └── test/             # Controller MockMvc and JPA repository slice tests
│   ├── checkstyle.xml        # Checkstyle rule specifications
│   ├── pmd.xml               # PMD static code analyzer rule specifications
│   └── pom.xml               # Maven configuration & plugins
├── docs/                     # Technical specifications and design documents
│   ├── api/                  # API Contracts and reference guide
│   ├── architecture/         # System topology and security breakdown
│   ├── database/             # Relational schema ER mappings
│   └── deployment/           # Environment setup and runbooks
├── frontend/                 # Single Page Application frontend client
│   ├── src/
│   │   ├── components/
│   │   │   └── ui/           # Custom Reusable UI design system components
│   │   ├── layouts/          # Application shell layout wrapper (Navbar, Sidebar)
│   │   ├── pages/            # Page views (Home, About, Courses, Auth, etc.)
│   │   ├── styles/           # CSS Variables, custom scrollbars, Tailwind layers
│   │   ├── utils/            # JWT decoders and input validator helpers
│   │   ├── App.jsx           # React Router route registry
│   │   └── main.jsx          # App entry mount
│   ├── postcss.config.js     # PostCSS loader settings
│   ├── tailwind.config.js    # Tailwind content and theme tokens mapping
│   ├── vite.config.js        # Vite configurations and Vitest coverage
│   └── package.json          # Node dependencies and execution scripts
├── postman/                  # Postman collection files for all service APIs
├── docker-compose.yml        # Multi-container local deployment orchestrator
└── README.md                 # Root read document
```

---

## Getting Started

### Prerequisites
Make sure you have the following installed on your machine:
*   [Java 21 JDK](https://adoptium.net/temurin/releases/)
*   [Maven 3.9+](https://maven.apache.org/download.cgi)
*   [Node.js 20+](https://nodejs.org/en/download/)
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Local Development

#### 1. Start Database Container
From the root directory:
```bash
docker compose up -d db
```
This spins up the MySQL 8 database container. Schema migrations will run automatically when you launch the backend service.

#### 2. Start Backend Service
Navigate to `auth-service` and boot the spring application:
```bash
cd auth-service
./mvnw spring-boot:run
```
The server will start on port `8081`.

#### 3. Start Frontend Client
Navigate to `frontend` and start the Vite dev server:
```bash
cd frontend
npm install
npm run dev
```
The client will launch on `http://localhost:5173`.

### Docker Setup
To compile, package, and orchestrate all services inside Docker containers, execute the following from the root directory:
```bash
docker compose up --build
```
This orchestrates:
1.  **db**: Starts MySQL 8 and waits for health checks to pass.
2.  **auth-service**: Compiles and runs the backend JAR on port `8081`.
3.  **frontend**: Installs dependencies and runs Vite on port `5173` with polling-based hot-reloading.

---

## Environment Variables

### Frontend (`frontend/.env`)
*   `VITE_API_BASE_URL`: Base HTTP address targeting backend gateway APIs (defaults to `http://localhost:8081/api/v1`).

### Backend (`auth-service/src/main/resources/application.properties`)
*   `DB_HOST`: Hostname of the database server.
*   `DB_PORT`: Port number of the database server.
*   `DB_NAME`: Target database name.
*   `DB_USER`: Database authentication username.
*   `DB_PASSWORD`: Database authentication password.
*   `JWT_SECRET`: Base64 encoded secret key signature (minimum 256 bits).

---

## API Documentation

### POST `/api/v1/auth/register`
Creates a new user record.
*   **Request Envelope**:
    ```json
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "username": "johndoe",
      "password": "SecureP@ss123",
      "confirmPassword": "SecureP@ss123"
    }
    ```
*   **Response Envelope (201 Created)**:
    ```json
    {
      "status": "success",
      "message": "Registration successful",
      "data": { "id": "uuid-v4-string" }
    }
    ```

### POST `/api/v1/auth/login`
Authenticates a user and returns authorization tokens.
*   **Request Envelope**:
    ```json
    {
      "email": "john.doe@example.com",
      "password": "SecureP@ss123"
    }
    ```
*   **Response Envelope (200 OK)**:
    ```json
    {
      "status": "success",
      "data": {
        "accessToken": "ey...",
        "user": {
          "email": "john.doe@example.com",
          "username": "johndoe"
        }
      }
    }
    ```

### GET `/api/v1/courses`
Returns a paginated list of courses (Available in the Postman mock collection).

### POST `/api/v1/enrollments`
Enrolls a student in a course (Available in the Postman mock collection).

---

## Testing

### Frontend
Navigate to `/frontend/` and run the tests:
*   **Execute unit/UI tests**: `npm run test`
*   **Verify coverage report (min 80%)**: `npm run test:coverage`

### Backend
Navigate to `/auth-service/` and run Maven verification:
*   **Execute tests, coverage, and style checks**: `./mvnw clean verify`

---

## GitHub Actions
GitHub Actions pipelines execute on every pull request and push to main:
*   `frontend-ci.yml`: Checks format with Prettier, lints using ESLint, validates Vitest coverage (80% minimum), and compiles the application.
*   `backend-ci.yml`: Performs clean compile, runs static analysis (Checkstyle, PMD, SpotBugs), and executes all JUnit tests.
*   `docker-build.yml`: Validates Docker Compose file structures and tests build commands.

---

## Docker Details
*   **Multi-Stage Build**: backend compilation builds are conducted inside builder containers, copying only JRE runtime layers to minimize final image footprint size.
*   **Hot Reloading**: Enabled on the frontend using Volume mounts and Vite HMR polling (`usePolling: true`), and on the backend using Spring Boot DevTools compilation.
*   **Persistent Volumes**: Relational storage files are mounted on volume `mysql_data` to ensure persistence across container lifecycles.

---

## Security
*   **JWT Authorization**: Stateless security context with short token expiration.
*   **Password Hashing**: BCrypt with a strength work factor of 12.
*   **Role-Based Access Control**: Strict endpoint route matches targeting specific roles.
*   **Inputs Validation**: Handled on incoming payloads via Jakarta validation annotations.

---

## Future Enhancements
1.  **Backend Course Microservice**: Establish the backend API services for Course Catalog management using the database-per-service pattern.
2.  **Enrollment Microservice**: Create enrollment management and notification webhooks.
3.  **Real-Time Live Classroom**: Enable WebRTC components inside layouts for interactive learning.

---

## Contributing

### Branch Strategy
All modifications must map to a specific GitHub issue. Implement features inside named branches matching standard prefixes:
*   `feature_fe/<issue-number>_<feature-name>` (frontend changes)
*   `feature_be/<issue-number>_<feature-name>` (backend changes)
*   `bugfix/<issue-number>_<bug-name>` (hotfixes)

### Pull Request Workflow
1.  Rebase feature branches onto `main` locally before pushing.
2.  Ensure local checks (`npm run test:coverage` and `./mvnw clean verify`) pass.
3.  Submit a Pull Request using the standard template.
4.  All merges onto `main` must use **Squash and Merge**.

---

## License
Licensed under the [MIT License](LICENSE).
