# Feature Specification: z-sb4-rest-logging Observability Stack

**Feature Branch**: `004-z-sb4-rest-logging-observability`  
**Created**: 2026-08-23  
**Status**: Draft  
**Input**: User description: "I want to add distrubted tracing, central logging and metric for z-sb4-rest-logging project, i.e. I want this project to use prometheus, grafana with loki, jaeger for z-sb4-rest-logging spring boot application. Please guide me through from both code and configuration."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Run a full local observability stack (Priority: P1)

As a developer running `z-sb4-rest-logging` locally, I want Prometheus, Grafana, Loki, Jaeger, and an OTLP collector available from Compose so that I can inspect metrics, logs, and traces without manual setup.

**Why this priority**: The stack is the foundation for everything else and is the main delivery target of the request.

**Independent Test**: Start the compose stack and verify all observability services are reachable on their documented ports.

**Acceptance Scenarios**:

1. **Given** the repo checkout, **When** `docker compose up -d` is run from `z-sb4-rest-logging`, **Then** Prometheus, Grafana, Loki, Jaeger, and the OTLP collector all start successfully.
2. **Given** the compose stack is running, **When** the app is started on the host with the local profile, **Then** the telemetry services are ready to receive metrics, logs, and traces.
3. **Given** Grafana starts, **When** a developer opens it, **Then** Prometheus, Loki, and Jaeger datasources are already provisioned.

---

### User Story 2 - Produce correlated application telemetry (Priority: P1)

As a developer using the demo application, I want logs, metrics, and traces emitted from the same request so that I can correlate failures and latency across the stack.

**Why this priority**: Correlation is the core value of adding observability to the service.

**Independent Test**: Send requests to the demo endpoints and verify that logs include trace identifiers, Prometheus exposes request metrics, and Jaeger shows traces for the same traffic.

**Acceptance Scenarios**:

1. **Given** an inbound request, **When** it is handled, **Then** the app emits a trace and records request timing metrics.
2. **Given** an outbound HTTP call, **When** it executes, **Then** the app records outbound timing and includes the same trace context in logs.
3. **Given** a request that exercises batch or parallel processing, **When** it completes, **Then** the telemetry includes batch size and operation outcome metrics.

---

### User Story 3 - Centralize JSON logs in Loki (Priority: P2)

As a developer debugging the application, I want structured application logs shipped into Loki so that I can search request and response details centrally.

**Why this priority**: Centralized logs are needed to complete the observability workflow and pair with traces.

**Independent Test**: Generate a request, confirm the log entry is written as JSON, shipped to Loki, and queryable in Grafana.

**Acceptance Scenarios**:

1. **Given** the app receives a request, **When** it logs the request and response exchange, **Then** the log entry is structured JSON and contains trace correlation fields.
2. **Given** sensitive HTTP headers or payloads, **When** logs are written, **Then** the sensitive values are scrubbed before leaving the application.
3. **Given** the log shipper reads the application log file, **When** the app produces a new line, **Then** Loki receives the entry without manual intervention.

---

### User Story 4 - Visualize metrics and traces in Grafana (Priority: P2)

As a developer exploring service behavior, I want starter Grafana dashboards and datasources so that I can open one place and inspect metrics, logs, and traces.

**Why this priority**: The stack is much more usable when it is prewired for the common workflows.

**Independent Test**: Open Grafana after the stack starts and verify the dashboard loads and the datasources resolve.

**Acceptance Scenarios**:

1. **Given** Grafana starts with the repo provisioning, **When** a developer opens the observability dashboard, **Then** request rate, request latency, and batch metrics are visible.
2. **Given** Grafana is connected to Loki, **When** a developer searches logs for a trace identifier, **Then** matching application logs are returned.
3. **Given** Grafana is connected to Jaeger, **When** a developer opens a trace-backed request, **Then** the trace details are visible.

## Edge Cases

- What happens if the telemetry stack is unavailable when the app starts?
- What if the application runs without the local profile?
- What if a request contains sensitive headers or bodies that must not be sent to Loki?
- What if trace sampling is disabled or reduced by environment configuration?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The project MUST provide a local Compose stack that starts Prometheus, Grafana, Loki, Jaeger, and an OTLP collector.
- **FR-002**: The application MUST remain runnable on the host while the observability stack runs in Docker Compose.
- **FR-003**: The application MUST expose Prometheus metrics through Actuator.
- **FR-004**: The application MUST emit tracing data using Spring/Micrometer observability APIs.
- **FR-005**: The tracing configuration MUST be enabled for local development and must be configurable by environment or profile.
- **FR-006**: Application logs MUST be structured JSON and suitable for centralized log ingestion.
- **FR-007**: Application logs MUST include trace correlation fields so logs and traces can be linked.
- **FR-008**: Sensitive request and response data MUST be scrubbed before logs are shipped to Loki.
- **FR-009**: The stack MUST provide Grafana provisioning for datasources and at least one starter dashboard.
- **FR-010**: Prometheus MUST scrape the application metrics endpoint directly.
- **FR-011**: The observability dashboard MUST include request rate, request latency, and batch-size visibility.
- **FR-012**: The application MUST record explicit demo metrics for inbound request handling, outbound call timing, and batch sizing.
- **FR-013**: The app MUST keep its existing functional request/response behavior while adding observability.

### Key Entities *(include if feature involves data)*

- **DemoObservability**: Application-side helper that records observations and custom metrics around demo endpoints.
- **ObservabilityConfig**: Spring configuration that applies common metric tags.
- **Structured HTTP Log**: JSON log entry containing timestamp, trace correlation fields, HTTP metadata, and scrubbed payloads.
- **Compose Observability Stack**: The set of local services that receive, store, and visualize telemetry.
- **Grafana Dashboard**: Starter dashboard showing request rate, request latency, batch size, logs, and traces.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer can start the full observability stack with a single Compose command.
- **SC-002**: A request to the demo app produces a trace, a metrics sample, and a structured log entry that share correlation context.
- **SC-003**: Prometheus exposes the app metrics endpoint and Grafana can query the resulting series.
- **SC-004**: Loki receives the application logs and can return them by trace identifier or request metadata.
- **SC-005**: Jaeger displays traces for traffic sent to the demo endpoints.
- **SC-006**: The app continues to serve its existing endpoints after observability is enabled.
