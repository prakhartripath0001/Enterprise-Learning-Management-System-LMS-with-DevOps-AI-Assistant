#!/bin/sh
# Compiler watch script for Spring Boot DevTools in Docker
# Watches src/ for changes newer than /tmp/last_compile and runs maven compilation.

touch /tmp/last_compile
echo "Java compiler watcher started. Monitoring src/ folder..."

while true; do
  sleep 2
  if [ -n "$(find src -type f -newer /tmp/last_compile -print -quit)" ]; then
    echo "Java source change detected. Recompiling..."
    touch /tmp/last_compile
    ./mvnw compile -DskipTests -q
    echo "Recompilation complete. DevTools should restart application now."
  fi
done
