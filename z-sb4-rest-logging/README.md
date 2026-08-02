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

### 🚀 Mimicking Production Locally (1-Click Run)
This application uses Spring Boot’s native **Docker Compose Support**. When you run the application profile `local`, 
Spring will automatically spin up, configure, and wire all required infrastructure containers.

```bash
# Run from this module so Spring can find compose.yaml
../gradlew bootRun --args='--spring.profiles.active=local'
```

*To manage the containers manually instead, use:*
```bash
docker compose up -d
```

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
