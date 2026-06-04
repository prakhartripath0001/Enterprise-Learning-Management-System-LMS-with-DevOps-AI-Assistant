#!/bin/bash

# ==============================================================================
# security-scan.sh — Security Quality Gate
# Checks for: hardcoded secrets, TODO/FIXME, weak patterns, config leaks
# ==============================================================================

set -eo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VIOLATIONS=0

echo -e "\n${YELLOW}▶ Running security scan...${NC}"

# ── 1. Secret Pattern Detection ────────────────────────────────────────────────
echo "  Scanning for hardcoded secrets..."
SECRET_PATTERN='(password|secret|private_key|api_key|jwt_secret|credentials|token)\s*=\s*["'"'"'][a-zA-Z0-9_\-\.:/+=]{8,}["'"'"']'
MATCHES=$(grep -rEnI "$SECRET_PATTERN" \
  --include="*.java" --include="*.properties" --include="*.yml" --include="*.json" \
  "$ROOT_DIR" \
  --exclude-dir=target --exclude-dir=.git --exclude-dir=node_modules --exclude-dir=.github \
  2>/dev/null || true)

if [ -n "$MATCHES" ]; then
  echo -e "${RED}  ✘ Potential hardcoded secrets detected:${NC}"
  echo "$MATCHES"
  VIOLATIONS=$((VIOLATIONS+1))
else
  echo -e "${GREEN}  ✔ No hardcoded secrets found${NC}"
fi

# ── 2. TODO / FIXME Check ─────────────────────────────────────────────────────
echo "  Scanning for TODO/FIXME comments..."
TODOS=$(grep -rn "TODO\|FIXME" \
  --include="*.java" --include="*.properties" --include="*.yml" \
  "$ROOT_DIR" \
  --exclude-dir=target --exclude-dir=.git --exclude-dir=.github \
  2>/dev/null || true)

if [ -n "$TODOS" ]; then
  echo -e "${YELLOW}  ⚠ Found TODO/FIXME comments (resolve before production):${NC}"
  echo "$TODOS"
  # Warn but don't fail — change VIOLATIONS++ to enforce
else
  echo -e "${GREEN}  ✔ No TODO/FIXME comments found${NC}"
fi

# ── 3. Wildcard CORS Check ────────────────────────────────────────────────────
echo "  Scanning for CORS wildcard (*) configurations..."
CORS_WILDCARDS=$(grep -rn "allowedOrigins.*\\\*\|setAllowedOrigins.*\\\*" \
  --include="*.java" --include="*.yml" --include="*.properties" \
  "$ROOT_DIR" --exclude-dir=target --exclude-dir=.git --exclude-dir=.github 2>/dev/null || true)

if [ -n "$CORS_WILDCARDS" ]; then
  echo -e "${RED}  ✘ Wildcard CORS (*) detected — must not be used in production:${NC}"
  echo "$CORS_WILDCARDS"
  VIOLATIONS=$((VIOLATIONS+1))
else
  echo -e "${GREEN}  ✔ No wildcard CORS configurations found${NC}"
fi

# ── 4. @Autowired Field Injection Check ───────────────────────────────────────
echo "  Scanning for @Autowired field injection (violates AGENTS.md)..."
AUTOWIRED=$(grep -rn "@Autowired" \
  --include="*.java" \
  "$ROOT_DIR" --exclude-dir=target --exclude-dir=.git --exclude-dir=.github \
  2>/dev/null || true)

if [ -n "$AUTOWIRED" ]; then
  echo -e "${RED}  ✘ @Autowired field injection detected — use constructor injection only:${NC}"
  echo "$AUTOWIRED"
  VIOLATIONS=$((VIOLATIONS+1))
else
  echo -e "${GREEN}  ✔ No @Autowired field injection found${NC}"
fi

# ── 5. Raw Entity in Controller Return ────────────────────────────────────────
echo "  Scanning for possible entity returns from controllers..."
ENTITY_RETURNS=$(grep -rn "return.*Entity\|ResponseEntity.*Entity" \
  "$ROOT_DIR" --include="*.java" --exclude-dir=target --exclude-dir=.git --exclude-dir=.github \
  2>/dev/null | grep -i "controller" || true)

if [ -n "$ENTITY_RETURNS" ]; then
  echo -e "${YELLOW}  ⚠ Possible raw entity return from controller detected — use DTOs:${NC}"
  echo "$ENTITY_RETURNS"
else
  echo -e "${GREEN}  ✔ No obvious entity returns from controllers detected${NC}"
fi

# ── Final Result ───────────────────────────────────────────────────────────────
echo ""
if [ "$VIOLATIONS" -gt 0 ]; then
  echo -e "${RED}✘ Security scan FAILED — $VIOLATIONS critical violation(s) found.${NC}"
  exit 1
fi

echo -e "${GREEN}✔ Security scan PASSED.${NC}"
exit 0
