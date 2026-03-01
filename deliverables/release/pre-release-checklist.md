# Pre-Release Checklist and Runbook

## Upgrade Precheck
- verify `java -version` is 17+.
- verify MySQL connectivity and account privilege for Flyway.
- verify backup exists for production schema before deployment.
- verify no pending local changes in deployment branch.
- run environment dependency check before release:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File deliverables/release/check-env.ps1`
  - for full release gate (db + maven): `powershell -NoProfile -ExecutionPolicy Bypass -File deliverables/release/check-env.ps1 -RequireMySql true -RequireMaven true`

## Deploy Commands

```bash
# backend
cd laidekuai-backend
./mvnw -q -DskipTests package
java -jar target/laidekuai-backend-*.jar

# frontend
cd laidekuai-frontend
npm ci
npm run build
```

## Flyway Verification
- check startup log contains successful migration up to latest version.
- check table `flyway_schema_history` newest row is `Success`.
- check new tables/endpoints required by current release are reachable.

## Smoke Verification
- login API works with valid credential.
- goods list API returns data.
- upload API returns `/static/files/...` URL.
- admin disputes and admin audit logs endpoints return 200 for admin token.
- admin scheduler metrics endpoint returns counters for timeout scan/cancel.
- buyer refund detail route loads without runtime error.
- run release smoke script and require `SMOKE_ALL_OK`:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File deliverables/release/verify-smoke.ps1 -BaseUrl http://127.0.0.1:9090`
- run RBAC + state-machine smoke script and require `RBAC_STATE_ALL_OK`:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File deliverables/release/verify-rbac-state.ps1 -BaseUrl http://127.0.0.1:9090 -EnableStateChecks true -BuyerToken <buyer-token> -SellerToken <seller-token> -IllegalPayOrderId <order-id> -IllegalCancelOrderId <order-id> -IllegalRefundOrderId <order-id> -IllegalShipOrderItemId <order-item-id>`
- run `k6` smoke script and verify success/failure checks both pass:
  - `k6 run -e BASE_URL=http://127.0.0.1:9090/api deliverables/perf/k6-core-smoke.js`
- run requirement 16.4 acceptance scripts in target environment:
  - `k6 run -e BASE_URL=http://127.0.0.1:9090/api perf/k6/goods_list.js`
  - `k6 run -e BASE_URL=http://127.0.0.1:9090/api -e LOGIN_USERNAME=<user> -e LOGIN_PASSWORD=<password> -e ADDRESS_ID=<id> -e GOODS_ID=<id> perf/k6/order_create.js`

## Failure Rollback
- rollback app binary to previous release artifact.
- rollback frontend static assets to previous build.
- if migration introduced non-compatible change, restore DB from snapshot.
- rerun smoke checks on rolled-back version before opening traffic.
