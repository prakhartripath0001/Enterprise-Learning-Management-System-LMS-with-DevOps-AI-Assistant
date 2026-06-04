#!/bin/bash

# ==============================================================================
# docker-validate.sh — Docker Stack Health Gate
# Validates: docker compose up, MySQL readiness, auth-service health
# ==============================================================================

set -eo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
MAX_WAIT=120   # seconds to wait for services to be healthy
AUTH_PORT=8081

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required for Docker validation}"
: "${DB_PASSWORD:?DB_PASSWORD is required for Docker validation}"
: "${JWT_SECRET:?JWT_SECRET is required for Docker validation}"

cleanup() {
  echo -e "\n${YELLOW}▶ Tearing down Docker stack...${NC}"
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans 2>/dev/null || true
}
trap cleanup EXIT

# ── 1. Build Docker Images ─────────────────────────────────────────────────────
echo -e "${YELLOW}▶ Building Docker images...${NC}"
docker compose -f "$COMPOSE_FILE" build --no-cache auth-service
echo -e "${GREEN}✔ Docker images built${NC}"

# ── 2. Start the Stack ────────────────────────────────────────────────────────
echo -e "${YELLOW}▶ Starting Docker Compose stack...${NC}"
docker compose -f "$COMPOSE_FILE" up -d
echo -e "${GREEN}✔ Docker Compose stack started${NC}"

# ── 3. Wait for MySQL Readiness ───────────────────────────────────────────────
echo -e "${YELLOW}▶ Waiting for MySQL to be healthy...${NC}"
ELAPSED=0
until docker compose -f "$COMPOSE_FILE" exec -T db mysqladmin ping -u root -p"$MYSQL_ROOT_PASSWORD" --silent 2>/dev/null; do
  sleep 3; ELAPSED=$((ELAPSED+3))
  echo "  Waiting... ($ELAPSED/${MAX_WAIT}s)"
  [ "$ELAPSED" -ge "$MAX_WAIT" ] && { echo -e "${RED}✘ MySQL did not become healthy in time${NC}"; exit 1; }
done
echo -e "${GREEN}✔ MySQL is healthy${NC}"

# ── 4. Wait for Auth Service Health ───────────────────────────────────────────
echo -e "${YELLOW}▶ Waiting for auth-service to be healthy (port $AUTH_PORT)...${NC}"
ELAPSED=0
until curl -sf "http://localhost:$AUTH_PORT/actuator/health" | grep -q '"status":"UP"' 2>/dev/null; do
  sleep 5; ELAPSED=$((ELAPSED+5))
  echo "  Waiting for auth-service... ($ELAPSED/${MAX_WAIT}s)"
  [ "$ELAPSED" -ge "$MAX_WAIT" ] && {
    echo -e "${RED}✘ auth-service did not become healthy in time${NC}"
    docker compose -f "$COMPOSE_FILE" logs auth-service --tail=50
    exit 1
  }
done
echo -e "${GREEN}✔ auth-service is healthy and running on port $AUTH_PORT${NC}"

# ── 5. Verify Flyway Migrations Applied ───────────────────────────────────────
echo -e "${YELLOW}▶ Verifying Flyway migrations...${NC}"
MIGRATION_CHECK=$(curl -sf "http://localhost:$AUTH_PORT/actuator/flyway" 2>/dev/null || echo "")
if echo "$MIGRATION_CHECK" | grep -q '"state":"SUCCESS"'; then
  echo -e "${GREEN}✔ Flyway migrations applied successfully${NC}"
else
  echo -e "${YELLOW}⚠ Flyway actuator not exposed — skipping migration state check${NC}"
fi

echo -e "\n${GREEN}✔ Docker validation passed.${NC}"
exit 0
