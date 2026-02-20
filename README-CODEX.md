# Codex Cloud Run Guide

## Backend

- Start
  - Linux/macOS: `scripts/start-backend.sh`
  - Windows: `scripts/start-backend.ps1`
- Env
  - `PORT` (default 9090)
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (optional)
  - `UPLOAD_PATH`, `UPLOAD_ALLOWED_TYPES`, `UPLOAD_MAX_SIZE` (optional)

## Frontend

- Start
  - Linux/macOS: `scripts/start-frontend.sh`
  - Windows: `scripts/start-frontend.ps1`
- Env
  - `PORT` (default 5173)
  - `VITE_API_TARGET` (dev proxy target, default `http://localhost:9090`)
  - `VITE_API_BASE` (axios base, default `/api`)

## Tests

- Linux/macOS: `scripts/test.sh`
- Windows: `scripts/test.ps1`

Notes:
- `laidekuai-frontend` 当前没有单元测试脚本；仓库根目录提供了 Playwright E2E 用例（`tests/`）。
- For production-style routing, place a reverse proxy in front of the frontend and backend so `/api` points to the backend.
