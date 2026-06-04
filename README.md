# Enterprise Learning Management System (LMS) with DevOps AI Assistant

Welcome to the **Enterprise Learning Management System (LMS)** repository. This project features a robust Spring Boot backend coupled with a MySQL database, packaged and orchestrated entirely using Docker and Docker Compose.

---

## 🚀 Quick Start (One Command Run)

You can build and start both the database and the backend service with a single command. You do **not** need to install Java or Maven locally on your machine, as the backend uses a multi-stage Docker build to compile inside the container.

### Prerequisites
Make sure you have [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/) installed.

### Run the Application
From the root directory of the project, run:

```bash
docker compose up --build
```

This single command will:
1. **Compile & Package** the Spring Boot application using Maven inside a builder container.
2. **Start the MySQL Database** (`db`) and initialize the schema.
3. **Wait for DB Health**: Orchestrate the startup so that the backend waits for the database to be fully healthy and ready to accept connections before booting.
4. **Launch the Backend** (`lms-backend`) at `http://localhost:8080`.

---

## 🛠️ Docker Architecture & Boot Ordering

The services are orchestrated in [docker-compose.yml](file:///Users/prakhartripathi/Documents/src/Enterprise-Learning-Management-System-LMS-with-DevOps-AI-Assistant/docker-compose.yml):

- **Database (`db`)**: Uses `mysql:8.0` with healthcheck configuration testing database readiness via `mysqladmin ping`.
- **Backend (`backend`)**: Uses a multi-stage [Dockerfile](file:///Users/prakhartripathi/Documents/src/Enterprise-Learning-Management-System-LMS-with-DevOps-AI-Assistant/backend/Dockerfile). It depends on `db` with the `service_healthy` condition to prevent any connection failure issues on startup.

---

## 📖 API Documentation & Endpoints

Once the application is running, you can access:

- **Backend API Base**: `http://localhost:8080`
- **Swagger / OpenAPI UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) (interactive API documentation and testing client)
- **API docs JSON**: `http://localhost:8080/v3/api-docs`

---

## 📂 Project Structure

- `/backend` - Spring Boot source code and configuration.
- `/docs` - Markdown documentation including developer guidelines.
- `docker-compose.yml` - Multi-container docker compose setup.
