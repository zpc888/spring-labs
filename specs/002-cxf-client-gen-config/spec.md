# Feature Specification: CXF SOAP Client Generation Config Plugin

**Feature Branch**: `002-cxf-client-gen-config`  
**Created**: 2026-04-19  
**Status**: In Progress  
**Input**: User description: "enhance prot-cxf frontend custom plugin to add extra generation command argument client-gen-config whose value is a yaml file from file or classpath, the yaml file contains the extra annotation based on the name of SEI, and the methods' annotation based on operation. Also I need a unit test to run maven using this plugin."

## Clarifications

### Session 2026-04-19

- Q: What should be the exact name of the new command-line argument? → A: `client-gen-config`
- Q: What should the YAML configuration file format structure look like? → A: Hierarchical with operations AND class-level sections
- Q: How should the YAML file be loaded - from file path or classpath, or both? → A: Both auto-detect (file path first, then classpath)
- Q: What annotation format should be used in the YAML file? → A: Fully qualified class names
- Q: For feature `002-cxf-client-gen-config`, what is the required test strategy for wsdl2java verification? → A: Plugin-level tests in `prot-cxf-codegen/prot-cxf-codegen-plugin` using `maven-plugin-testing-harness` (no Maven Invoker)
- Q: Which WSDL fixture should be used by the plugin tests? → A: Copied `ping.wsdl` at `src/test/resources/backend/soap/ping.wsdl`
- Q: Which minimum scenario matrix must be covered for `clientGenConfig`? → A: Three scenarios: config A, config B, and no config

### Session 2026-04-20

- Q: What is the canonical YAML key for per-operation config in this feature? → A: `operations` (correcting prior typo `opertions`).
- Q: What YAML fields are required for the new client generation model? → A: `x-config-key`, `x-static-headers[{name,value,ifExisting?}]`, `x-dynamic-headers[List<FQCN>]`, and `x-operations.<operationName>.action` (kebab-case in YAML); mapped to `configKey`, `staticHeaders`, `dynamicHeaders`, `operations` in Java model (camelCase).
- Q: How is `resolvedConfigKey` computed for `@prot.soap.RegisteredSoapClient`? → A: Use `configKey` when non-null and non-empty; otherwise use the SEI portType name.
- Q: Which class-level annotations are mandatory vs optional on generated SEI? → A: Always emit `@prot.soap.RegisteredSoapClient(resolvedConfigKey)`; emit `@prot.soap.StaticHeaders` and `@prot.soap.DynamicHeaders` only when corresponding YAML values are present.
- Q: How is `resolvedActionName` computed for operation annotations? → A: Always emit `@prot.soap.SoapAction(resolvedActionName)` where `resolvedActionName` is configured `action` when non-empty, else the operation name.

### Session 2026-04-20 (Continued)

- Q: What is the canonical annotation package namespace for client registration, headers, and actions? → A: `prot.soap.*` (@prot.soap.RegisteredSoapClient, @prot.soap.StaticHeaders, @prot.soap.DynamicHeaders, @prot.soap.SoapAction).
- Q: How should YAML field naming differ from Java object model naming? → A: YAML uses kebab-case (x-config-key, x-static-headers, x-dynamic-headers, x-operations); Java model uses camelCase (configKey, staticHeaders, dynamicHeaders, operations). SnakeYAML auto-maps between them.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Custom Annotation on Generated SEI Interface (Priority: P1)

As a SOAP client developer, I want generated SEI interfaces to include standardized client registration and optional header annotations from YAML, so that SOAP clients are configured consistently at generation time.

**Why this priority**: Core value proposition - the main feature request is to add extra annotations to SEI.

**Independent Test**: Run wsdl2java with `--client-gen-config` and verify generated SEI contains mandatory `@prot.soap.RegisteredSoapClient(resolvedConfigKey)` and optional header annotations only when configured.

**Acceptance Scenarios**:

1. **Given** a valid YAML with non-empty `x-config-key`, **When** running wsdl2java with `--client-gen-config`, **Then** the generated SEI includes `@prot.soap.RegisteredSoapClient(configKey)`.
2. **Given** YAML with null/empty `x-config-key`, **When** generation runs, **Then** `@prot.soap.RegisteredSoapClient` uses portType name as `resolvedConfigKey`.
3. **Given** YAML defines `x-static-headers` and/or `x-dynamic-headers`, **When** generation runs, **Then** SEI includes `@prot.soap.StaticHeaders` and/or `@prot.soap.DynamicHeaders` respectively.

---

### User Story 2 - Custom Annotation Per Operation (Priority: P2)

As a SOAP client developer, I want each generated SEI operation method to always receive `@a.b3.SoapAction` with a resolved action name, so that runtime invocation action mapping is explicit and predictable.

**Why this priority**: Enables fine-grained control over each method's annotations.

**Independent Test**: Run wsdl2java with YAML `x-operations` entries and verify each generated method includes `@prot.soap.SoapAction` resolved from configured action or fallback operation name.

**Acceptance Scenarios**:

1. **Given** YAML contains `x-operations.ping.action: pingAction`, **When** wsdl2java runs, **Then** generated `ping` method has `@prot.soap.SoapAction("pingAction")`.
2. **Given** YAML contains `x-operations.echo` without action or with empty action, **When** code is generated, **Then** generated `echo` method has `@prot.soap.SoapAction("echo")`.

---

### User Story 3 - YAML Config Loading from File and Classpath (Priority: P2)

As a SOAP client developer, I want the config file to be loaded from file path or classpath transparently, so that I can easily bundle configs in JARs or use external files.

**Why this priority**: Provides flexibility in how configs are provided and makes the feature more user-friendly.

**Independent Test**: Test with file-based config and classpath-based config to ensure both work.

**Acceptance Scenarios**:

1. **Given** a file path config that exists, **When** running wsdl2java, **Then** it's loaded from the file system.
2. **Given** a file path config that doesn't exist, **When** running wsdl2java, **Then** it's loaded from classpath.
3. **Given** config not found in either location, **When** running wsdl2java, **Then** the operation fails gracefully with clear error.

---

### User Story 4 - Plugin-Level wsdl2java Test Matrix (Priority: P1)

As a plugin developer, I want plugin-level tests that execute CXF `wsdl2java` through frontend `prot-cxf`, so that generation behavior is verified in-JVM for multiple `clientGenConfig` inputs.

**Why this priority**: This directly validates the requested behavior and prevents regressions in config-driven generation logic.

**Independent Test**: Run `mvn test` in `prot-cxf-codegen/prot-cxf-codegen-plugin` and verify harness-driven plugin tests for all three scenarios using the copied `ping.wsdl` fixture.

**Acceptance Scenarios**:

1. **Given** `clientGenConfig` A and frontend `prot-cxf`, **When** wsdl2java runs against `src/test/resources/backend/soap/ping.wsdl`, **Then** generated SEI output contains the annotations defined by config A.
2. **Given** `clientGenConfig` B and frontend `prot-cxf`, **When** wsdl2java runs against the same fixture, **Then** generated SEI output contains the annotations defined by config B and differs from config A expectations.
3. **Given** no `clientGenConfig`, **When** wsdl2java runs with frontend `prot-cxf`, **Then** generation succeeds and no config-driven custom annotations are emitted.

---

### User Story 5 - Mojo Unit Tests for Plugin Classes (Priority: P2)

As a plugin developer, I want a dedicated unit test suite for the plugin classes (`ClientGenConfig`, `ConfigLoader`, `CustomSEIGenerator`) using the Maven Mojo Testing framework, so that I can verify plugin behaviour in isolation without requiring a full Maven build invocation.

**Why this priority**: Validates individual plugin class logic faster and more reliably than integration tests; enables test-driven development of the config loading and annotation-applying logic.

**Independent Test**: Run `mvn test` inside `prot-cxf-codegen/prot-cxf-codegen-plugin`; all Mojo unit tests pass without requiring an external Maven process.

**Acceptance Scenarios**:

1. **Given** a YAML config file on the file system, **When** `ConfigLoader` is invoked with its absolute path, **Then** a `ClientGenConfig` object is returned with the correct SEI annotations and operation mappings populated.
2. **Given** a YAML config file bundled on the test classpath, **When** `ConfigLoader` is invoked with a name that does not resolve to a file, **Then** the config is loaded from the classpath and the same `ClientGenConfig` object is returned.
3. **Given** a `ClientGenConfig` with SEI-level annotations, **When** `CustomSEIGenerator` processes a WSDL service, **Then** the generated SEI Java source contains each specified annotation on the interface declaration.
4. **Given** a `ClientGenConfig` with per-operation annotations for operation "ping", **When** `CustomSEIGenerator` generates the SEI, **Then** only the "ping" method carries the configured annotation while other methods are unaffected.
5. **Given** a malformed or missing YAML file, **When** `ConfigLoader` is invoked, **Then** a descriptive exception is thrown and no partial config is returned.

---

### Edge Cases

- What happens when the YAML config file is malformed?
- How does system handle missing operation mappings (undefined operations)?
- What if two annotations are specified for the same operation/class?
- How to handle empty or empty YAML file?
- If `clientGenConfig` is omitted, generation MUST follow baseline behavior and emit no config-driven custom annotations.
- If `configKey` is null/empty, `resolvedConfigKey` MUST fallback to the generated portType name.
- If `operations.<name>.action` is null/empty, `resolvedActionName` MUST fallback to `<name>`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The CXF wsdl2java tool MUST accept a new `--client-gen-config` command-line argument.
- **FR-002**: The `--client-gen-config` value MUST accept a YAML location resolvable from file path or classpath.
- **FR-003**: The YAML config MUST support a hierarchical structure with:
  - `x-config-key` string value
  - `x-static-headers` list of objects with `name`, `value`, and optional `ifExisting`
  - `x-dynamic-headers` list of fully qualified class names
  - `x-operations` map keyed by operation name
- **FR-004**: The config file MUST first attempt to load from the file system, then fallback to classpath.
- **FR-005**: When config is not found, the tool MUST fail gracefully with a clear error message.
- **FR-006**: The generated SEI interface MUST include `@prot.soap.RegisteredSoapClient(resolvedConfigKey)` for every generated client.
- **FR-007**: `resolvedConfigKey` MUST use `configKey` when non-null/non-empty, otherwise MUST use the portType name.
- **FR-008**: If `x-static-headers` is present and non-empty, generated SEI MUST include `@prot.soap.StaticHeaders` representing configured header tuples.
- **FR-009**: If `x-dynamic-headers` is present and non-empty, generated SEI MUST include `@prot.soap.DynamicHeaders` with configured FQCN values.
- **FR-010**: `dynamicHeaders` entries MUST be fully qualified class names.
- **FR-011**: For each generated operation method, `@prot.soap.SoapAction(resolvedActionName)` MUST always be emitted.
- **FR-012**: `resolvedActionName` MUST use configured `x-operations.<operation>.action` when non-empty, otherwise MUST fallback to the operation name.
- **FR-013**: The canonical YAML field names MUST use kebab-case (`x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations`); these MUST be mapped to camelCase Java model fields (`configKey`, `staticHeaders`, `dynamicHeaders`, `operations`) by the YAML parser.
- **FR-014**: All annotation classes MUST use the `prot.soap.*` package namespace (@prot.soap.RegisteredSoapClient, @prot.soap.StaticHeaders, @prot.soap.DynamicHeaders, @prot.soap.SoapAction).
- **FR-014**: Plugin-level tests MUST be implemented in `prot-cxf-codegen/prot-cxf-codegen-plugin` using `maven-plugin-testing-harness` (or equivalent in-process Mojo harness), not Maven Invoker.
- **FR-015**: Tests MUST execute wsdl2java with frontend `prot-cxf` and use copied fixture `src/test/resources/backend/soap/ping.wsdl`.
- **FR-016**: Test resources MUST include at least two distinct YAML configs (`clientGenConfig` A and B) producing different expected annotation outcomes.
- **FR-017**: A dedicated harness test MUST verify scenario A applies expected class-level and operation-level annotations to generated sources.
- **FR-018**: A dedicated harness test MUST verify scenario B applies its own expected annotations and does not match scenario A assertions.
- **FR-019**: A dedicated harness test MUST verify generation succeeds without `clientGenConfig` and emits no config-driven custom annotations.
- **FR-020**: All new tests MUST pass during `mvn test` without network access or external Maven process invocation.
- **FR-021**: The no-config scenario MUST be treated as backward-compatible baseline behavior.

### Key Entities *(include if feature involves data)*

- **ClientGenConfig**: Root YAML model containing `configKey`, `staticHeaders`, `dynamicHeaders`, and `operations`
- **StaticHeader**: Header tuple with `name`, `value`, and optional `ifExisting`
- **OperationConfig**: Per-operation configuration containing `action`
- **SEIGenerator**: Modified CXF SEI generator that resolves keys/actions and emits required annotations

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Generated SEI always includes `@prot.soap.RegisteredSoapClient(resolvedConfigKey)` with fallback behavior verified for empty `configKey`.
- **SC-002**: Generated SEI includes `@prot.soap.StaticHeaders` and/or `@prot.soap.DynamicHeaders` only when YAML provides corresponding non-empty values.
- **SC-003**: Three harness tests (config A, config B, no config) pass consistently in `prot-cxf-codegen/prot-cxf-codegen-plugin`.
- **SC-004**: For each generated operation, `@prot.soap.SoapAction` is present and uses configured `action` or operation-name fallback.
- **SC-005**: Both file-path-first and classpath-fallback config loading behaviors are verified by automated tests.
- **SC-006**: All Mojo unit tests (`FR-017` through `FR-020`) pass in under 30 seconds on a standard developer machine.
- **SC-007**: Unit test coverage for `prot.cxf.plugin.*` classes reaches at least 80 % of lines exercised by the new test suite.
- **SC-008**: No test relies on spawning an external Maven process; all assertions run within the same JVM as the test runner.
- **SC-009**: YAML kebab-case field names (`x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations`) are correctly mapped to Java camelCase model fields by SnakeYAML parser.

## Assumptions

- Users have CXF 4.2.0 or compatible version
- YAML parsing uses an existing library (SnakeYAML or similar)
- The existing CustomSEIGenerator will be extended, not replaced
- Java 21 is the target runtime