# System Architecture and Deployment

## Runtime Architecture

```text
[Browser(Vue3)]
    |
    | HTTPS
    v
[Nginx / Reverse Proxy]
    |-- / -> Frontend static assets (Vite build)
    |-- /api/* -> Spring Boot 3.2 backend
                       |
                       | JDBC + Flyway
                       v
                    MySQL 8.0
```

## Module Boundaries
- frontend: auth, goods, order, message, upload, admin, dispute views.
- backend: controller -> service -> mapper layering with JWT + Spring Security.
- database: Flyway versioned schema as source of truth.

## Deployment Topology

```text
Host A: web
- nginx
- frontend dist

Host B: app
- java 17
- laidekuai-backend.jar

Host C: db
- mysql 8.0
- daily backup
```

## Configuration Baseline
- frontend base URL points to `/api` proxy.
- backend: `spring.servlet.multipart.max-file-size=20MB` and `max-request-size=20MB`.
- backend static upload mapping exposed under `/static/files/**`.
- JWT secret and DB credentials provided via environment variables.

## Observability Baseline
- app logs include order timeout cancel counts and scheduler scan counts.
- nginx access log retained for API audit and incident replay.
- MySQL slow query log enabled in non-dev environments.
