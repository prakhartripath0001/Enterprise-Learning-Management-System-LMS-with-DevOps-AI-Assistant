#!/bin/bash

# ==============================================================================
# setup.sh — Developer Environment Bootstrap
# Installs Git hooks and ensures all scripts are executable
# ==============================================================================

set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCRIPTS_DIR="$ROOT_DIR/scripts"
HOOKS_DIR="$ROOT_DIR/.git/hooks"
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'

echo -e "${YELLOW}▶ Setting up Enterprise LMS development environment...${NC}"

# ── Make all scripts executable ───────────────────────────────────────────────
chmod +x "$SCRIPTS_DIR"/*.sh
echo -e "${GREEN}✔ All scripts made executable${NC}"

# ── Install Git Hooks via symlinks ────────────────────────────────────────────
mkdir -p "$HOOKS_DIR"

# Pre-commit hook
ln -sf "../../scripts/hooks/pre-commit" "$HOOKS_DIR/pre-commit"
chmod +x "$SCRIPTS_DIR/hooks/pre-commit"
echo -e "${GREEN}✔ pre-commit hook installed${NC}"

# Pre-push hook
ln -sf "../../scripts/hooks/pre-push" "$HOOKS_DIR/pre-push"
chmod +x "$SCRIPTS_DIR/hooks/pre-push"
echo -e "${GREEN}✔ pre-push hook installed${NC}"

echo -e "\n${GREEN}✔ Developer environment setup complete.${NC}"
echo -e "   Run: ${YELLOW}./scripts/test-all.sh${NC} to verify quality gate."
