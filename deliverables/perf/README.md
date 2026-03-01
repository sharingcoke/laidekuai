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

## Requirement 16.4 k6 Acceptance Scripts
- goods list acceptance script: `perf/k6/goods_list.js`
- order create acceptance script: `perf/k6/order_create.js`

### Run goods list acceptance
```bash
k6 run -e BASE_URL=http://127.0.0.1:9090/api perf/k6/goods_list.js
```

Expected:
- request failure rate `< 1%`
- p95 latency `< 500ms`
- response `status=200` and business `code=0`

### Run order create acceptance
```bash
k6 run \
  -e BASE_URL=http://127.0.0.1:9090/api \
  -e LOGIN_USERNAME=user1 \
  -e LOGIN_PASSWORD=123456 \
  -e ADDRESS_ID=1 \
  -e GOODS_ID=1 \
  perf/k6/order_create.js
```

Expected:
- request failure rate `< 1%`
- p95 latency `< 800ms`
- login and order create return business `code=0`
