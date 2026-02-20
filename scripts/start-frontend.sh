#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/laidekuai-frontend"

export PORT="${PORT:-5173}"
export VITE_API_TARGET="${VITE_API_TARGET:-http://localhost:9090}"
export VITE_API_BASE="${VITE_API_BASE:-/api}"

cd "$FRONTEND_DIR"
if [ ! -d node_modules ]; then
  npm install
fi
npm run dev -- --host 0.0.0.0 --port "$PORT"