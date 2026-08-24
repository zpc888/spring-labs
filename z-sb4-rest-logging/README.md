# z-sb4-rest-logging

## 📋 Overview
Provides the custom REST logging functionality for Spring Boot applications. It contains service request/response 
and rest client calling out request/response logging.

* **Runtime:** Java 21 / Spring Boot 4 (Executable JAR)

---

## ℹ️ Implementation Info
- `org.springframework.web.filter.CommonsRequestLoggingFilter` provides out-of-box to log HTTP request info including 
   request payload. But it doesn't support to log response payload.
- To support both request and response payload, custom logging solutions are implemented.
- For rest client side logging, it needs to implement `ClientHttpRequestInterceptor`

---

## 💻 Local Development Setup

### Prerequisites
* Docker / Podman
* Java 21

### 🚀 Observability Stack
This module now includes Prometheus, Grafana, Loki, Jaeger, and an OpenTelemetry Collector.

```bash
# Run the app with Spring Boot's Docker Compose support
../gradlew bootRun --args='--spring.profiles.active=local'
```

The app stays on the host. Run it with the `local` profile so tracing exports to the collector and the
structured JSON logs are written to `build/rest-logging.json`. If you prefer to manage the stack manually,
`docker compose up -d` uses the same service definitions.

Local service ports are mapped to non-default host ports to avoid collisions: httpbin `18091`, Prometheus `19090`,
Grafana `13000`, OTLP `14317/14318`, and Loki `13100`.

---

## 🏗 Quality & Architecture Compliance

See `test-client.http` to integration testing.

Run local validation before pushing code:
```bash
../gradlew clean test
```

---

## 🔌 API Endpoints & Contracts
* **Local Swagger UI:** `http://localhost:8090/swagger-ui.html`
* **Actuator Health Metrics:** `http://localhost:8090/actuator/health`
* **Prometheus:** `http://localhost:19090`
* **Grafana:** `http://localhost:13000` (`admin` / `admin`)
* **Jaeger:** `http://localhost:16686`
