#!/bin/bash

# ==============================================================================
# quality-check.sh — Static Analysis Gate
# Runs: Checkstyle, PMD, SpotBugs
# ==============================================================================

set -eo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVICES=("auth-service")
FAILED=false

run_check() {
  local service="$1"
  local goal="$2"
  local label="$3"
  echo -e "${YELLOW}▶ Running $label for $service...${NC}"
  if (cd "$ROOT_DIR/$service" && ./mvnw $goal -q 2>&1); then
    echo -e "${GREEN}✔ $label passed for $service${NC}"
  else
    echo -e "${RED}✘ $label FAILED for $service${NC}"
    FAILED=true
  fi
}

for service in "${SERVICES[@]}"; do
  echo -e "\n${YELLOW}Checking: $service${NC}"

  # Checkstyle: enforce Google Java Style
  run_check "$service" "checkstyle:check" "Checkstyle"

  # PMD: detect code smells and bad patterns
  run_check "$service" "pmd:check" "PMD"

  # SpotBugs: bytecode static bug analysis
  run_check "$service" "spotbugs:check" "SpotBugs"
done

if [ "$FAILED" = true ]; then
  echo -e "\n${RED}✘ Static analysis FAILED. Fix all violations before committing.${NC}"
  exit 1
fi

echo -e "\n${GREEN}✔ All static analysis checks passed.${NC}"
exit 0
