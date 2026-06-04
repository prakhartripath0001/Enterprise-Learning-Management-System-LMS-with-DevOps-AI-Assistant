#!/bin/bash

# ==============================================================================
# Git Pre-Push Hook: Quality and Security Gate Enforcer
# ==============================================================================

set -eo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0;0m'

echo -e "${YELLOW}Executing Pre-Push Hooks...${NC}"

# Find all maven projects in the workspace
MAVEN_PROJECTS=$(find . -name "pom.xml" -not -path "*/target/*" -exec dirname {} \;)

# ------------------------------------------------------------------------------
# 1. Check for TODO / FIXME comments in modified files
# ------------------------------------------------------------------------------
echo -e "Checking for forbidden comments (TODO/FIXME)..."
FORBIDDEN_COMMENTS=$(git diff --cached --name-only | grep -E '\.(java|properties|yml|xml|sh)$' | xargs grep -E -n 'TODO|FIXME' || true)

if [ -n "$FORBIDDEN_COMMENTS" ]; then
    echo -e "${RED}[ERROR] Push blocked: Found TODO or FIXME comments in staged files:${NC}"
    echo "$FORBIDDEN_COMMENTS"
    exit 1
fi
echo -e "${GREEN}No TODO/FIXME comments found.${NC}"

# ------------------------------------------------------------------------------
# 2. Secret Scan (Detect passwords, secrets, private keys in staged files)
# ------------------------------------------------------------------------------
echo -e "Scanning staged files for hardcoded secrets..."
SECRET_PATTERNS='(password|secret|private_key|token|api_key|credentials|jwt_secret)\s*=\s*["'\''][a-zA-Z0-9_\-\.\:\/\+\=\%]{8,}["'\'']'
STAGED_FILES=$(git diff --cached --name-only | grep -E '\.(java|properties|yml|xml|json)$' || true)

SECRET_FOUND=false
if [ -n "$STAGED_FILES" ]; then
    while IFS= read -r file; do
        if [ -f "$file" ]; then
            MATCHES=$(grep -E -n "$SECRET_PATTERNS" "$file" || true)
            if [ -n "$MATCHES" ]; then
                echo -e "${RED}[ERROR] Potential secret detected in $file:${NC}"
                echo "$MATCHES"
                SECRET_FOUND=true
            fi
        fi
    done <<< "$STAGED_FILES"
fi

if [ "$SECRET_FOUND" = true ]; then
    echo -e "${RED}Push blocked: Potential secrets detected!${NC}"
    exit 1
fi
echo -e "${GREEN}No hardcoded secrets detected.${NC}"

# ------------------------------------------------------------------------------
# 3. Run Maven Compile, Test, and Verify
# ------------------------------------------------------------------------------
for project in $MAVEN_PROJECTS; do
    echo -e "Building and testing Maven project: ${YELLOW}$project${NC}..."
    
    # Run tests
    if ! (cd "$project" && ./mvnw clean test); then
        echo -e "${RED}[ERROR] Maven tests failed in $project! Push blocked.${NC}"
        exit 1
    fi
    echo -e "${GREEN}Maven tests passed in $project.${NC}"

    # Run verification (integration tests & checkstyle)
    if ! (cd "$project" && ./mvnw verify -DskipTests); then
        echo -e "${RED}[ERROR] Maven verify failed in $project! Push blocked.${NC}"
        exit 1
    fi
    echo -e "${GREEN}Maven verification passed in $project.${NC}"
done

# ------------------------------------------------------------------------------
# 4. Check Code Coverage Threshold (Minimum 80% line coverage in Jacoco)
# ------------------------------------------------------------------------------
MIN_COVERAGE=80
for project in $MAVEN_PROJECTS; do
    JACOCO_REPORT="$project/target/site/jacoco/jacoco.xml"
    if [ -f "$JACOCO_REPORT" ]; then
        echo -e "Analyzing coverage for $project..."
        # Extract line coverage using simple xml parser
        COVERED_LINES=$(grep -o -E 'type="LINE" missed="[0-9]+" covered="[0-9]+"' "$JACOCO_REPORT" | head -n 1 | sed -E 's/.*covered="([0-9]+)".*/\1/')
        MISSED_LINES=$(grep -o -E 'type="LINE" missed="[0-9]+" covered="[0-9]+"' "$JACOCO_REPORT" | head -n 1 | sed -E 's/.*missed="([0-9]+)".*/\1/')
        
        TOTAL_LINES=$((COVERED_LINES + MISSED_LINES))
        if [ "$TOTAL_LINES" -gt 0 ]; then
            ACTUAL_COVERAGE=$(( (COVERED_LINES * 100) / TOTAL_LINES ))
            if [ "$ACTUAL_COVERAGE" -lt "$MIN_COVERAGE" ]; then
                echo -e "${RED}[ERROR] Code coverage ($ACTUAL_COVERAGE%) is below required minimum of $MIN_COVERAGE% in $project! Push blocked.${NC}"
                exit 1
            fi
            echo -e "${GREEN}Coverage check passed: $ACTUAL_COVERAGE% (min required: $MIN_COVERAGE%).${NC}"
        fi
    else
        echo -e "${YELLOW}[WARNING] JaCoCo XML report not found at $JACOCO_REPORT. Skipping coverage threshold check.${NC}"
    fi
done

echo -e "${GREEN}All pre-push validations successfully completed! Proceeding with git push.${NC}"
exit 0
