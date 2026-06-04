# Deployment & Operations Guide

This guide details the deployment of the Enterprise LMS platform across local, containerized, CI/CD, and production environments.

---

## 1. Local Development Lifecycle

### Prerequisites
Ensure the following development environments are available:
*   Java 21 JDK
*   Maven 3.9+
*   Node.js 20+
*   Docker & Docker Compose

### Run Database Locally
To spin up a local instance of the database:
```bash
docker compose up -d db
```
This launches MySQL 8 on port `3306`. Flyway migrations inside the backend service automatically run when the spring boot application boots.

### Run Backend Locally
Navigate to the `auth-service` directory:
```bash
cd auth-service
./mvnw spring-boot:run
```
Starts backend API servers on port `8081`.

### Run Frontend Locally
Navigate to the `frontend` directory:
```bash
cd frontend
npm install
npm run dev
```
Launches the Vite dev server on port `5173`.

---

## 2. Docker Compose Deployment (Recommended for Local Dev)
To build and launch the entire application stack in a single command, execute the following from the root directory:
```bash
docker compose up --build
```

### Docker Services configuration
*   **db**: Runs MySQL 8, exposes port `3306`. Includes a `healthcheck` ping verifying readiness.
*   **auth-service**: Java container built via a development Dockerfile. It maps the local source tree into the container and runs the application using an automated watch script to rebuild on file changes. Depends on the database container being `service_healthy`.
*   **frontend**: Node container built via `Dockerfile.dev`. Maps local source code files to `/app`, exposes port `5173`, and runs the Vite server with filesystem polling enabled to support Hot Module Replacement (HMR).

---

## 3. CI/CD Pipeline Flow
The platform utilizes GitHub Actions for continuous integration. Workflows are defined in `.github/workflows/`:
1.  **Frontend CI (`frontend-ci.yml`)**: Installs dependencies, runs Prettier code formatting checks, lints code via ESLint, runs Vitest unit tests, checks test coverage bounds (minimum 80%), and builds production static bundles.
2.  **Backend CI (`backend-ci.yml`)**: Compiles Java files, runs Checkstyle rules validation, runs PMD rules validation, runs SpotBugs bug patterns checks, and runs JUnit test suites with JaCoCo coverage validation (minimum 80% line coverage).
3.  **Docker Build CI (`docker-build.yml`)**: Validates the syntax of the Docker Compose configuration and checks if the service containers build successfully.

---

## 4. Production Deployment Strategy
For staging and production deployments, we follow these best practices:

### 1. Multi-stage Container Builds
Our production Dockerfiles compile source code inside a Maven or Node builder stage, and copy only the compiled binaries (`.jar` files or built static assets) into lightweight runtime containers (e.g. Alpine Linux, Eclipse Temurin Alpine JRE, or Nginx).

### 2. Microservice Isolation
*   Deploy each microservice as an independent deployment unit in Kubernetes (or AWS ECS/Fargate).
*   Use an API Gateway (e.g. Spring Cloud Gateway or Kong) to route traffic, handle SSL termination, and terminate CORS requests.

### 3. Database Cluster Strategy
*   Migrate from local Docker volumes to managed database instances (e.g. AWS RDS MySQL or Cloud SQL).
*   Enforce encryption at rest and transit.
*   Separate schemas into dedicated database servers or clusters.

### 4. Logging & Monitoring
*   Send application logs in JSON format to centralized collectors (ELK Stack or Grafana Loki).
*   Expose metrics endpoints via Spring Boot Actuator and Prometheus to set up Grafana alert dashboards.
