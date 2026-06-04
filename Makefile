# ==============================================================================
# Makefile — Enterprise LMS Quality Gate Targets
# Principal DevOps Engineer Grade
# ==============================================================================
# Usage: make <target>
# Targets: setup, build, test, coverage, quality, security, docker-up,
#          docker-down, clean, ci, help
# ==============================================================================

.PHONY: setup build test coverage quality security \
        docker-up docker-down docker-build clean ci help

SHELL := /bin/bash
ROOT_DIR := $(shell pwd)
AUTH_SERVICE := $(ROOT_DIR)/auth-service

# ── Colors ────────────────────────────────────────────────────────────────────
GREEN  := \033[0;32m
YELLOW := \033[1;33m
RED    := \033[0;31m
NC     := \033[0m

# ── Default Target ─────────────────────────────────────────────────────────────
.DEFAULT_GOAL := help

## setup: Install git hooks and make scripts executable
setup:
	@echo -e "$(YELLOW)▶ Setting up developer environment...$(NC)"
	@chmod +x scripts/*.sh scripts/hooks/*
	@./scripts/setup.sh
	@echo -e "$(GREEN)✔ Setup complete$(NC)"

## build: Compile all services
build:
	@echo -e "$(YELLOW)▶ Compiling all services...$(NC)"
	@cd $(AUTH_SERVICE) && ./mvnw clean compile -q
	@echo -e "$(GREEN)✔ Build successful$(NC)"

## test: Run all unit tests
test:
	@echo -e "$(YELLOW)▶ Running unit tests...$(NC)"
	@cd $(AUTH_SERVICE) && ./mvnw clean test
	@echo -e "$(GREEN)✔ Tests passed$(NC)"

## coverage: Run tests with JaCoCo coverage report and threshold check
coverage:
	@echo -e "$(YELLOW)▶ Running tests with coverage check...$(NC)"
	@cd $(AUTH_SERVICE) && ./mvnw clean verify
	@echo -e "$(GREEN)✔ Coverage check passed$(NC)"
	@echo -e "  Report: file://$(AUTH_SERVICE)/target/site/jacoco/index.html"

## quality: Run Checkstyle, PMD, SpotBugs
quality:
	@echo -e "$(YELLOW)▶ Running static analysis...$(NC)"
	@./scripts/quality-check.sh
	@echo -e "$(GREEN)✔ Quality checks passed$(NC)"

## security: Run security scan for secrets, CORS, @Autowired violations
security:
	@echo -e "$(YELLOW)▶ Running security scan...$(NC)"
	@./scripts/security-scan.sh
	@echo -e "$(GREEN)✔ Security scan passed$(NC)"

## docker-build: Build all Docker images
docker-build:
	@echo -e "$(YELLOW)▶ Building Docker images...$(NC)"
	@docker compose build --no-cache
	@echo -e "$(GREEN)✔ Docker images built$(NC)"

## docker-up: Start all Docker Compose services
docker-up:
	@echo -e "$(YELLOW)▶ Starting Docker Compose stack...$(NC)"
	@docker compose up -d
	@echo -e "$(GREEN)✔ Stack running — auth-service on :8081, MySQL on :3306$(NC)"

## docker-down: Stop and remove all Docker Compose services and volumes
docker-down:
	@echo -e "$(YELLOW)▶ Stopping Docker Compose stack...$(NC)"
	@docker compose down -v --remove-orphans
	@echo -e "$(GREEN)✔ Stack stopped and volumes removed$(NC)"

## docker-validate: Build, start, and validate Docker stack health
docker-validate:
	@echo -e "$(YELLOW)▶ Validating Docker stack...$(NC)"
	@./scripts/docker-validate.sh
	@echo -e "$(GREEN)✔ Docker validation passed$(NC)"

## clean: Remove build artifacts
clean:
	@echo -e "$(YELLOW)▶ Cleaning build artifacts...$(NC)"
	@cd $(AUTH_SERVICE) && ./mvnw clean -q
	@echo -e "$(GREEN)✔ Clean complete$(NC)"

## package: Build production JAR
package:
	@echo -e "$(YELLOW)▶ Packaging production JARs...$(NC)"
	@cd $(AUTH_SERVICE) && ./mvnw package -DskipTests -q
	@echo -e "$(GREEN)✔ Packages built$(NC)"

## ci: Run the full quality gate (all checks)
ci:
	@echo -e "$(YELLOW)▶ Running full quality gate...$(NC)"
	@./scripts/test-all.sh
	@echo -e "$(GREEN)✔ Full quality gate passed$(NC)"

## help: Display all available targets
help:
	@echo ""
	@echo "  Enterprise LMS — Available Make Targets"
	@echo "  ──────────────────────────────────────────────"
	@grep -E '^## ' $(MAKEFILE_LIST) | sed 's/## /  make /g' | awk -F':' '{printf "  %-22s %s\n", $$1, $$2}'
	@echo ""
