#!/usr/bin/env bash
# Installs dependencies and starts the hotel-booking Spring Boot app.
# Usage: ./scripts/run.sh [port]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

PORT="${1:-${SERVER_PORT:-8080}}"

echo "Installing dependencies..."
./mvnw -q -DskipTests install

echo "Starting hotel-booking on port $PORT ..."
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT"
