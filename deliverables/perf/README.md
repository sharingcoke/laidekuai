# Performance Smoke Script

## Goal
- provide repeatable lightweight load verification for release gate.
- cover one public success path and one unauthorized failure path.

## Script
- `k6-core-smoke.js`

## Run
```bash
k6 run -e BASE_URL=http://127.0.0.1:9090/api deliverables/perf/k6-core-smoke.js
```

## Expected
- `goods list status is 200` check passes.
- `admin endpoint rejects anonymous` check passes.
- p95 for public goods list under 800ms in target environment baseline.
