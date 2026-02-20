#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/laidekuai-backend"
FRONTEND_DIR="$ROOT_DIR/laidekuai-frontend"

export PORT="${PORT:-9090}"

cd "$BACKEND_DIR"
./mvnw -q test

cd "$FRONTEND_DIR"
if [ -f package.json ]; then
  if npm run -s | grep -q " test"; then
    npm run test
  else
    echo "No frontend test script configured."
  fi
fi