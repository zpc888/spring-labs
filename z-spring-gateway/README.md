# spring-gateway-poc

Small Spring Cloud Gateway proof-of-concept with:
- one `gateway` app (port `8081`)
- two `hello-service` instances (same codebase, different profiles/ports):
  - `dev1` -> port `8082`
  - `dev2` -> port `8083`

## Prerequisites

- Java 17+ (project currently runs with Java 21)
- Linux/macOS shell (examples below use `zsh`/`bash` syntax)

## Build

From the project root:

```bash
cd /home/zpc/works/study/sandbox/spring-gateway-poc
./gradlew :hello-service:build :gateway:build
```

## Run 3 processes (recommended)

Open **three terminals**.

### Terminal 1 - hello-service dev1 (port 8082)

```bash
cd /home/zpc/works/study/sandbox/spring-gateway-poc
java -jar hello-service/build/libs/hello-service-1.0.0.jar --spring.profiles.active=dev1
```

### Terminal 2 - hello-service dev2 (port 8083)

```bash
cd /home/zpc/works/study/sandbox/spring-gateway-poc
java -jar hello-service/build/libs/hello-service-1.0.0.jar --spring.profiles.active=dev2
```

### Terminal 3 - gateway (port 8081)

```bash
cd /home/zpc/works/study/sandbox/spring-gateway-poc
java -jar gateway/build/libs/gateway-1.0.0.jar
```

## Health checks

```bash
curl -sS http://localhost:8081/actuator/health
curl -sS http://localhost:8082/actuator/health
curl -sS http://localhost:8083/actuator/health
```

Expected: each returns `{"status":"UP"}`.

## Route behavior

Configured routes are in `gateway/src/main/resources/application.yml`.

| Route ID | Incoming path | Predicate(s) | Forwarded URI | Path rewrite |
|---|---|---|---|---|
| `app1-route` | `/app1/**` | `Path=/app1/**` | `http://localhost:8082` | `StripPrefix=1` |
| `app2-route` | `/app2/**` | `Path=/app2/**` | `http://localhost:8083` | `StripPrefix=1` |
| `tenant-app2-route` | `/tenant/app2/**` | `Path=/tenant/app2/**`, `TenantHeader=gold` | `http://localhost:8083` | `StripPrefix=2` |

### Examples

```bash
# app1-route -> hello-service dev1 (8082)
curl -sS "http://localhost:8081/app1/hello?name=alice"

# app2-route -> hello-service dev2 (8083)
curl -sS "http://localhost:8081/app2/hello?name=bob"

# tenant-app2-route matches only when X-Tenant: gold
curl -i -sS -H 'X-Tenant: gold' "http://localhost:8081/tenant/app2/hello?name=carol"

# no match for tenant-app2-route when header is not gold (typically 404)
curl -i -sS -H 'X-Tenant: silver' "http://localhost:8081/tenant/app2/hello?name=carol"
```

## Application behavior

### hello-service

`hello-service` exposes:
- `GET /hello`
- CRUD endpoints for tasks:
  - `POST /tasks`
  - `GET /tasks`
  - `GET /tasks/{taskId}`
  - `PUT /tasks/{taskId}`
  - `DELETE /tasks/{taskId}`

Task data is stored in-memory (`InMemoryStore`), so each instance keeps **its own** task list.
Creating a task through `/app1/**` does not appear in `/app2/**` (and vice versa).

### Task API through gateway

```bash
# Create task in dev1 instance via gateway (/app1 -> 8082)
curl -sS -X POST "http://localhost:8081/app1/tasks" \
  -H 'Content-Type: application/json' \
  -d '{"id":0,"name":"buy milk","description":"from shop"}'

# List tasks from dev1 via gateway
curl -sS "http://localhost:8081/app1/tasks"

# List tasks from dev2 via gateway (different in-memory store)
curl -sS "http://localhost:8081/app2/tasks"
```

### gateway

`gateway` behavior in this repo:
- routes requests based on path/header predicates
- rewrites paths using `StripPrefix`
- has a global `RequestResponseLogFilter` that logs:
  - request method, URI, headers, query params
  - request body for `POST`/`PUT`/`PATCH` when content is text-like/json/xml
  - response status, headers, and response body (text-like/json/xml)
- has custom components:
  - `TenantHeaderRoutePredicateFactory` (`TenantHeader=...`)
  - `RequestTimingGatewayFilterFactory` (`RequestTiming=...`) which adds a latency header (for example `X-Latency-Ms`)

## Useful notes

- If startup fails with port-in-use errors, stop existing processes on `8081`, `8082`, `8083`.
- When running from IDE, use the same profiles:
  - hello-service instance 1: `--spring.profiles.active=dev1`
  - hello-service instance 2: `--spring.profiles.active=dev2`
- For the custom tenant route, include header `X-Tenant: gold`.


