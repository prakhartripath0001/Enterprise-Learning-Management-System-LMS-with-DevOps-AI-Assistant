# Setup and Deployment Guide

This guide details running the AetherLMS stack locally and containerized using Docker.

## 1. Local Development Setup

### Frontend Requirements:
*   Node.js v20+
*   Run commands inside the `frontend` folder:
    ```bash
    npm install
    npm run dev
    ```

### Backend Requirements:
*   JDK 21 (Maven Wrapper included)
*   Configure DB variables in environment or shell:
    ```bash
    export DB_HOST=localhost
    export DB_PORT=3306
    export DB_NAME=lms_db
    export DB_USER=lms_user
    export DB_PASSWORD=lms_password
    ```
*   Run the service inside the `auth-service` folder:
    ```bash
    ./mvnw spring-boot:run
    ```

## 2. Docker Compose Deployment (Recommended)

Build and run the entire development stack with hot-reloading (React HMR and Spring Boot DevTools compilation) using a single command:
```bash
docker compose up --build
```

### Stack Components:
1.  **MySQL Database**: `localhost:3306`
2.  **Auth Service Backend**: `localhost:8081` (Swagger: `localhost:8081/swagger-ui.html`)
3.  **React Frontend**: `localhost:5173`
