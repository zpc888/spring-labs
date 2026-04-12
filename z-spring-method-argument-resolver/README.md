# spring-method-argument-resolver-poc

Spring Boot proof-of-concept for custom method argument resolvers with Redis-backed token context.

## Prerequisites

- Java 17+
- Docker (for local Redis via `docker-compose`)

## Run the application locally

1. Start Redis:

```bash
docker compose up -d redis
```

2. Run the Spring Boot app:

```bash
./gradlew bootRun
```

Maven alternative:

```bash
./mvnw spring-boot:run
```

The app listens on `http://localhost:8081` by default. Redis is configured at `localhost:6380` in `src/main/resources/application.yml`.

## API documentation

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api/docs`

## Test API with curl

### 1) Login and get token

```bash
curl -i -X POST http://localhost:8081/api/v1/login \
  -H 'Content-Type: application/json' \
  -H 'channel: MOBILE' \
  -d '{"cardType":"DEBIT_CARD","cardNumber":"1234567890","password":"password123"}'
```

Expected `200 OK` response body:

```json
{"token":"<generated-token>"}
```

Copy the token value from the response and use it in the next request.

### 2) Transfer using the token

```bash
curl -i -X POST http://localhost:8081/api/v1/transfer3 \
  -H 'Content-Type: application/json' \
  -H 'x-access-token: <generated-token>' \
  -d '{"fromAccount":"ACC001","toAccount":"ACC002"}'
```

Expected `200 OK` response body:

```json
{
  "cardType":"DEBIT_CARD",
  "cardNumber":"1234567890",
  "channel":"MOBILE",
  "fromAccount":"ACC001",
  "toAccount":"ACC002",
  "status":"Transfer initiated"
}
```

### Quick negative checks

Missing token should return `403`:

```bash
curl -i -X POST http://localhost:8081/api/v1/transfer3 \
  -H 'Content-Type: application/json' \
  -d '{"fromAccount":"ACC001","toAccount":"ACC002"}'
```

Malformed JSON on login should return `400`:

```bash
curl -i -X POST http://localhost:8081/api/v1/login \
  -H 'Content-Type: application/json' \
  -H 'channel: MOBILE' \
  -d '{"cardType":"DEBIT_CARD","cardNumber":"1234567890","password":"password123"'
```

## Running tests

Run all tests:

```bash
./gradlew test
```

Maven alternative:

```bash
./mvnw test
```

Run only integration tests:

```bash
./gradlew test --tests com.example.demo.IntegrationTest
```

Maven alternative:

```bash
./mvnw -Dtest=IntegrationTest test
```

## How embedded Redis integration test works

`src/test/java/com/example/demo/IntegrationTest.java` uses full end-to-end HTTP testing with Spring Boot and an in-process Redis:

- `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` starts the app on a random port.
- `@LocalServerPort` injects the selected port for HTTP calls via `TestRestTemplate`.
- `@BeforeAll` starts embedded Redis on a free TCP port.
- `@DynamicPropertySource` points Spring Redis client to that embedded Redis host/port.
- `@BeforeEach` clears Redis (`FLUSHALL`) so each test is isolated.
- The suite covers both happy paths and negative paths for `/api/v1/login` and `/api/v1/transfer3`, including malformed JSON behavior.

