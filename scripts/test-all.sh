#!/bin/bash

# ==============================================================================
# test-all.sh — Enterprise LMS Master Quality Gate
# Principal DevOps / Backend Architect Grade
# ==============================================================================
# Usage: ./scripts/test-all.sh [--skip-docker] [--skip-api] [--skip-static]
# ==============================================================================

set -eo pipefail

# ── Colors ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

# ── Argument Flags ─────────────────────────────────────────────────────────────
SKIP_DOCKER=false; SKIP_API=false; SKIP_STATIC=false
for arg in "$@"; do
  case $arg in
    --skip-docker) SKIP_DOCKER=true ;;
    --skip-api)    SKIP_API=true    ;;
    --skip-static) SKIP_STATIC=true ;;
  esac
done

# ── Utilities ─────────────────────────────────────────────────────────────────
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCRIPTS_DIR="$ROOT_DIR/scripts"
SERVICES=("auth-service")
PASS=0; FAIL=0; SKIP=0

log_header()  { echo -e "\n${BLUE}${BOLD}══════════════════════════════════════${NC}"; echo -e "${CYAN}${BOLD}  $1${NC}"; echo -e "${BLUE}${BOLD}══════════════════════════════════════${NC}"; }
log_step()    { echo -e "${YELLOW}▶ $1${NC}"; }
log_ok()      { echo -e "${GREEN}✔ $1${NC}"; PASS=$((PASS+1)); }
log_fail()    { echo -e "${RED}✘ $1${NC}"; FAIL=$((FAIL+1)); }
log_skip()    { echo -e "${YELLOW}⊘ $1 (skipped)${NC}"; SKIP=$((SKIP+1)); }
fail_fast()   { log_fail "$1"; echo -e "${RED}${BOLD}Build aborted.${NC}"; print_summary; exit 1; }

print_summary() {
  echo -e "\n${BOLD}══════════════ QUALITY GATE SUMMARY ══════════════${NC}"
  echo -e "${GREEN}  Passed : $PASS${NC}"
  echo -e "${RED}  Failed : $FAIL${NC}"
  echo -e "${YELLOW}  Skipped: $SKIP${NC}"
  echo -e "${BOLD}══════════════════════════════════════════════════${NC}\n"
}

# ── Step 1: Pre-flight Checks ─────────────────────────────────────────────────
log_header "STEP 1: PRE-FLIGHT CHECKS"

check_tool() {
  if command -v "$1" &>/dev/null; then log_ok "$1 found";
  else log_fail "$1 not found — install it before continuing"; FAIL=$((FAIL+1)); fi
}

check_tool java; check_tool mvn; check_tool docker; check_tool git
if [ "$SKIP_API" = false ]; then check_tool newman || log_skip "Newman (npm install -g newman)"; fi

java_version=$(java -version 2>&1 | head -n1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
if [ "$java_version" -ge 21 ]; then log_ok "Java $java_version (>= 21)";
else fail_fast "Java 21+ required — found Java $java_version"; fi

[ "$FAIL" -gt 0 ] && fail_fast "Pre-flight checks failed. Fix missing tools and retry."

# ── Step 2: Compile All Services ──────────────────────────────────────────────
log_header "STEP 2: COMPILATION"
for service in "${SERVICES[@]}"; do
  log_step "Compiling $service..."
  if (cd "$ROOT_DIR/$service" && ./mvnw clean compile -q); then log_ok "$service compiled successfully";
  else fail_fast "$service compilation failed"; fi
done

# ── Step 3: Unit Tests ────────────────────────────────────────────────────────
log_header "STEP 3: UNIT TESTS"
for service in "${SERVICES[@]}"; do
  log_step "Running unit tests for $service..."
  if (cd "$ROOT_DIR/$service" && ./mvnw test -q 2>&1); then log_ok "$service unit tests passed";
  else
    log_fail "$service unit tests FAILED"
    echo -e "${RED}Test failure details:${NC}"
    find "$ROOT_DIR/$service/target/surefire-reports" -name "*.txt" -exec cat {} \; 2>/dev/null || true
    fail_fast "Unit tests failed in $service"
  fi
done

# ── Step 4: JaCoCo Coverage Check ─────────────────────────────────────────────
log_header "STEP 4: CODE COVERAGE (JaCoCo)"
for service in "${SERVICES[@]}"; do
  log_step "Running coverage check for $service..."
  if (cd "$ROOT_DIR/$service" && ./mvnw verify -DskipTests=false -q 2>&1); then
    log_ok "$service coverage thresholds met (≥85% line, ≥70% branch)"
    REPORT="$ROOT_DIR/$service/target/site/jacoco/index.html"
    [ -f "$REPORT" ] && echo -e "  ${CYAN}Report: file://$REPORT${NC}"
  else
    fail_fast "$service coverage check FAILED — below minimum threshold"
  fi
done

# ── Step 5: Static Analysis ────────────────────────────────────────────────────
log_header "STEP 5: STATIC ANALYSIS"
if [ "$SKIP_STATIC" = true ]; then log_skip "Static analysis"; else
  "$SCRIPTS_DIR/quality-check.sh" && log_ok "All static analysis checks passed" || fail_fast "Static analysis FAILED"
fi

# ── Step 6: Security Scan ─────────────────────────────────────────────────────
log_header "STEP 6: SECURITY SCAN"
"$SCRIPTS_DIR/security-scan.sh" && log_ok "Security scan passed" || fail_fast "Security scan FAILED"

# ── Step 7: Docker Validation ─────────────────────────────────────────────────
log_header "STEP 7: DOCKER VALIDATION"
if [ "$SKIP_DOCKER" = true ]; then log_skip "Docker validation"; else
  "$SCRIPTS_DIR/docker-validate.sh" && log_ok "Docker stack healthy" || fail_fast "Docker validation FAILED"
fi

# ── Step 8: API Tests (Newman) ────────────────────────────────────────────────
log_header "STEP 8: API TESTS (Newman / Postman)"
if [ "$SKIP_API" = true ]; then log_skip "API tests"; else
  COLLECTION="$ROOT_DIR/auth-service/docs/postman/LMS_Auth_Service.postman_collection.json"
  if command -v newman &>/dev/null && [ -f "$COLLECTION" ]; then
    if newman run "$COLLECTION" \
        --env-var "baseUrl=http://localhost:8081" \
        --reporters cli,json \
        --reporter-json-export "$ROOT_DIR/target/newman-results.json" \
        --bail; then
      log_ok "All API tests passed"
    else fail_fast "API tests FAILED"; fi
  else log_skip "Newman or collection not found — skipping API tests"; fi
fi

# ── Step 9: Maven Package (Production Build) ──────────────────────────────────
log_header "STEP 9: PRODUCTION BUILD (mvn package)"
for service in "${SERVICES[@]}"; do
  log_step "Packaging $service..."
  if (cd "$ROOT_DIR/$service" && ./mvnw package -DskipTests -q); then
    JAR=$(ls "$ROOT_DIR/$service/target/"*.jar 2>/dev/null | head -1)
    log_ok "$service packaged: $(basename "$JAR")"
  else fail_fast "$service packaging FAILED"; fi
done

# ── Final Summary ─────────────────────────────────────────────────────────────
log_header "QUALITY GATE RESULT"
print_summary

if [ "$FAIL" -eq 0 ]; then
  echo -e "${GREEN}${BOLD}✔ ALL QUALITY GATES PASSED — SAFE TO COMMIT AND PUSH${NC}\n"
  exit 0
else
  echo -e "${RED}${BOLD}✘ QUALITY GATE FAILED — DO NOT PUSH${NC}\n"
  exit 1
fi
