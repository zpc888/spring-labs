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

---

## 💻 Local Development Setup

### Prerequisites
* Docker / Podman
* Java 21

### 🚀 Mimicking Production Locally (1-Click Run)
This application uses Spring Boot’s native **Docker Compose Support**. When you run the application profile `local`, Spring will automatically spin up, configure, and wire all required infrastructure containers (e.g., PostgreSQL, Kafka, Redis).

```bash
# Clone and run instantly
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

*To manage the containers manually instead, use:*
```bash
docker compose -f src/main/resources/docker-compose.yml up -d
```

---

## 🏗 Quality & Architecture Compliance
This project enforces strict quality gates via the parent build plugin. Code compilation will fail if rules are broken.

* **Style Checks:** Enforced by Checkstyle and Error Prone during the `compile` phase.
* **Security & Vulnerabilities:** Scanned automatically via Snyk.
* **Architecture Rules:** Asserted via `ArchUnit` inside the test suite (e.g., ensuring zero direct cross-bean method references to maintain clean Lite-mode configuration).

Run local validation before pushing code:
```bash
mvn clean verify
```

---

## 🔌 API Endpoints & Contracts
* **Local Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **Actuator Health Metrics:** `http://localhost:8080/actuator/health`

