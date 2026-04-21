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
- Q: What YAML fields are required for the new client generation model? → A: `x-config-key` (required semantic fallback), optional `x-base-url`, optional `x-jaxb-context-paths`, `x-static-headers[{name,value,ifExisting?}]`, `x-dynamic-headers[List<FQCN>]`, and `x-operations.<operationName>.{action,static-headers,dynamic-headers}`.
- Q: How is `resolvedConfigKey` computed for `@prot.soap.SoapClient`? → A: Use `configKey` when non-null and non-empty; otherwise use the SEI portType name as mandatory fallback value.
- Q: Which class-level annotations are mandatory vs optional on generated SEI? → A: Always emit `@prot.soap.SoapClient(value=resolvedConfigKey)`; include optional `baseUrl`, `jaxbContextPaths`, `staticHeaders`, `dynamicHeaders` only when configured.
- Q: How is `resolvedActionName` computed for operation annotations? → A: Always emit `@prot.soap.SoapAction(resolvedActionName)` where `resolvedActionName` is configured `action` when non-empty, else WSDL binding `soap:operation@soapAction`.

### Session 2026-04-20 (Continued)

- Q: What is the canonical annotation package namespace for client registration, headers, and actions? → A: `prot.soap.*` (@prot.soap.SoapClient, @prot.soap.SoapAction, @prot.soap.SoapMethodHeader).
- Q: How should YAML field naming differ from Java object model naming? → A: YAML uses kebab-case canonical keys (including `x-base-url`, `x-jaxb-context-paths`) with accepted aliases for selected fields; Java model uses camelCase (`baseUrl`, `jaxbContextPaths`, etc.). SnakeYAML handles mapping.

### Session 2026-04-21 (Enhancement Round)

- Q: What YAML aliases should be supported for static-headers and dynamic-headers fields? → A: Canonical `x-static-headers`/`x-dynamic-headers` with accepted aliases `static-headers`, `staticHeaders`, `dynamic-headers`, `dynamicHeaders` (all mapped by SnakeYAML).
- Q: Should staticHeaders in generated annotations use concatenated "X=Y" strings or @SoapStaticHeader objects? → A: Both YAML input and generated annotation should use concatenated "X=Y" string format (e.g., `"X-Calc-Op=minus"`).
- Q: Should dynamicHeaders in generated annotations use Class<?> references or String FQCNs? → A: Generated annotations should use Class<?> references (e.g., `prot.soap.header.SoapHeaderAContributor.class`) with proper imports.
- Q: Should header format changes apply to both @prot.soap.SoapClient and @prot.soap.SoapMethodHeader? → A: Yes, both class-level and method-level annotations should use the same concatenated string format for staticHeaders and Class references for dynamicHeaders.
- Q: Should existing unit tests be rewritten or incrementally updated? → A: Incrementally update test assertions to verify new formats while maintaining backward-compatibility baseline (no config = no custom annotations).

### Session 2026-04-22 (Enhancement Round II)

- Q: Should OperationConfig YAML accept header field aliases in addition to canonical kebab-case? → A: Yes. `x-static-headers`, `static-headers`, `staticHeaders`, `staticheaders` all map to `staticHeaders`; `x-dynamic-headers`, `dynamic-headers`, `dynamicHeaders`, `dynamicheaders` all map to `dynamicHeaders`.
- Q: What is the final YAML input format for static headers in both top-level and operation-level config? → A: Concatenated "name=value" strings (e.g., `["X-Calc-Op=minus", "X-Overflow=false"]`), not objects with separate name/value fields.
- Q: What is the final generated annotation format for dynamic headers in both @prot.soap.SoapClient and @prot.soap.SoapMethodHeader? → A: Class<?> references with proper imports (e.g., `dynamicHeaders = {prot.soap.header.SoapHeaderAContributor.class, prot.soap.header.SoapHeaderBContributor.class}`).
- Q: What code coverage threshold must unit tests meet for prot.cxf.plugin package classes? → A: Minimum 80% line coverage; all unit tests must pass with coverage reports generated.
- Q: Which test scenarios must be verified to ensure correct handling of new header formats and YAML aliases? → A: Four scenarios: (1) ping.wsdl without config, (2) ping.wsdl with config, (3) calculator.wsdl with multi-operation headers and override variations, (4) test cases covering string and Class type formats.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Custom Annotation on Generated SEI Interface (Priority: P1)

As a SOAP client developer, I want generated SEI interfaces to include standardized `@prot.soap.SoapClient` annotation values from YAML, so that SOAP clients are configured consistently at generation time.

**Why this priority**: Core value proposition - the main feature request is to add extra annotations to SEI.

**Independent Test**: Run wsdl2java with `--client-gen-config` and verify generated SEI contains mandatory `@prot.soap.SoapClient(value=resolvedConfigKey)` plus optional `baseUrl`, `jaxbContextPaths`, `staticHeaders`, and `dynamicHeaders` only when configured.

**Acceptance Scenarios**:

1. **Given** a valid YAML with non-empty `x-config-key`, **When** running wsdl2java with `--client-gen-config`, **Then** the generated SEI includes `@prot.soap.SoapClient(value=configKey)`.
2. **Given** YAML with null/empty `x-config-key`, **When** generation runs, **Then** `@prot.soap.SoapClient` uses portType name as mandatory `value` fallback.
3. **Given** YAML defines top-level `x-base-url` and/or `x-jaxb-context-paths`, **When** generation runs, **Then** SEI includes those values in `@prot.soap.SoapClient`.
4. **Given** YAML defines top-level static and/or dynamic headers, **When** generation runs, **Then** they are emitted as `@prot.soap.SoapClient(staticHeaders=..., dynamicHeaders=...)` without generating separate type-level header annotations.

---

### User Story 2 - Custom Annotation Per Operation (Priority: P2)

As a SOAP client developer, I want each generated SEI operation method to always receive `@prot.soap.SoapAction` and optional `@prot.soap.SoapMethodHeader`, so that runtime invocation action and header mapping are explicit and predictable.

**Why this priority**: Enables fine-grained control over each method's annotations.

**Independent Test**: Run wsdl2java with YAML `x-operations` entries and verify each generated method includes `@prot.soap.SoapAction` resolved from configured action or WSDL binding `soapAction` fallback, and emits `@prot.soap.SoapMethodHeader` only when operation headers are configured.

**Acceptance Scenarios**:

1. **Given** YAML contains `x-operations.ping.action: pingAction`, **When** wsdl2java runs, **Then** generated `ping` method has `@prot.soap.SoapAction("pingAction")`.
2. **Given** YAML contains `x-operations.echo` without action or with empty action, **When** code is generated, **Then** generated `echo` method has `@prot.soap.SoapAction("<wsdlBindingSoapAction>")`.
3. **Given** YAML contains `x-operations.ping.static-headers` and/or `x-operations.ping.dynamic-headers`, **When** code is generated, **Then** `ping` includes `@prot.soap.SoapMethodHeader` with configured values.
4. **Given** an operation has neither static nor dynamic headers configured, **When** code is generated, **Then** no `@prot.soap.SoapMethodHeader` is emitted for that operation.

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
4. **Given** the soap-client module consumes generated classes, **When** `mvn test` runs in `prot-cxf-codegen-soap-client`, **Then** compilation and runtime tests remain compatible with generated annotations.

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
- If `configKey` is null/empty, `@prot.soap.SoapClient.value` MUST fallback to the generated portType name.
- If `operations.<name>.action` is null/empty, `resolvedActionName` MUST fallback to WSDL binding `soap:operation@soapAction`.
- If both configured action and WSDL binding `soapAction` are empty, fallback MUST be the operation name.
- Operation-level `@prot.soap.SoapMethodHeader` MUST NOT be emitted when both static and dynamic header lists are empty.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The CXF wsdl2java tool MUST accept a new `--client-gen-config` command-line argument.
- **FR-002**: The `--client-gen-config` value MUST accept a YAML location resolvable from file path or classpath.
- **FR-003**: The YAML config MUST support a hierarchical structure with:
   - `x-config-key` string value
   - `x-base-url` optional string value
   - `x-jaxb-context-paths` optional list of strings
   - `x-static-headers` list of concatenated "name=value" strings
   - `x-dynamic-headers` list of fully qualified class names
   - `x-operations` map keyed by operation name
- **FR-004**: The config file MUST first attempt to load from the file system, then fallback to classpath.
- **FR-005**: When config is not found, the tool MUST fail gracefully with a clear error message.
- **FR-006**: The generated SEI interface MUST include `@prot.soap.SoapClient(value=resolvedConfigKey)` for every generated client.
- **FR-007**: `resolvedConfigKey` MUST use `configKey` when non-null/non-empty, otherwise MUST use the portType name.
- **FR-008**: `@prot.soap.SoapClient.baseUrl` MUST be emitted only when top-level config defines a value.
- **FR-009**: `@prot.soap.SoapClient.jaxbContextPaths` MUST be emitted only when top-level config defines non-empty values.
- **FR-010**: Top-level static and dynamic headers MUST be emitted inside `@prot.soap.SoapClient(staticHeaders=..., dynamicHeaders=...)` and MUST NOT generate separate type-level header annotations.
- **FR-011**: `dynamicHeaders` entries (top-level and operation-level) MUST be fully qualified class names.
- **FR-012**: For each generated operation method, `@prot.soap.SoapAction(resolvedActionName)` MUST always be emitted.
- **FR-013**: `resolvedActionName` MUST use configured `x-operations.<operation>.action` when non-empty; else MUST fallback to WSDL binding `soap:operation@soapAction`; else operation name.
- **FR-014**: Operation config MUST support `staticHeaders` and `dynamicHeaders`; when either is non-empty, `@prot.soap.SoapMethodHeader` MUST be emitted.
- **FR-015**: `@prot.soap.SoapMethodHeader` MUST NOT be emitted when both operation-level static and dynamic headers are absent/empty.
- **FR-016**: Canonical YAML keys are kebab-case with `x-` prefix, mapped to Java camelCase fields by YAML parser.
- **FR-017**: YAML alias spellings MUST be accepted for top-level fields: `baseUrl|baseurl|base-url` → `baseUrl`; `jaxbContextPaths|jaxb-context-paths|jaxbcontextpaths` → `jaxbContextPaths`; `x-static-headers|static-headers|staticHeaders` → `staticHeaders`; `x-dynamic-headers|dynamic-headers|dynamicHeaders` → `dynamicHeaders`.
- **FR-017a**: Static headers in YAML MUST be specified as concatenated strings in format `"name=value"` (e.g., `"X-Calc-Op=minus"`), not as objects with separate `name` and `value` fields.
- **FR-017b**: Static headers in generated `@prot.soap.SoapClient` and `@prot.soap.SoapMethodHeader` annotations MUST be emitted as array of concatenated strings (e.g., `staticHeaders = {"X-Calc-Op=minus", "X-Overflow=false"}`).
- **FR-017c**: Dynamic headers in YAML MUST be specified as list of fully qualified class names (FQCNs) as strings.
- **FR-017d**: Dynamic headers in generated `@prot.soap.SoapClient` and `@prot.soap.SoapMethodHeader` annotations MUST be emitted as array of Class<?> references (e.g., `dynamicHeaders = {prot.soap.header.SoapHeaderAContributor.class, prot.soap.header.SoapHeaderBContributor.class}`) with proper imports.
- **FR-018**: Operation-level `x-operations.<operationName>` config MUST support YAML aliases for static and dynamic headers: `x-static-headers`, `static-headers`, `staticHeaders`, `staticheaders` all map to `staticHeaders`; `x-dynamic-headers`, `dynamic-headers`, `dynamicHeaders`, `dynamicheaders` all map to `dynamicHeaders`.
- **FR-019**: All generated annotations MUST use `prot.soap.*` namespace (@prot.soap.SoapClient, @prot.soap.SoapAction, @prot.soap.SoapMethodHeader).
- **FR-020**: Plugin-level tests MUST be implemented in `prot-cxf-codegen/prot-cxf-codegen-plugin` using `maven-plugin-testing-harness` (or equivalent in-process harness), not Maven Invoker.
- **FR-021**: Tests MUST execute wsdl2java with frontend `prot-cxf` and use copied fixture `src/test/resources/backend/soap/ping.wsdl`.
- **FR-022**: Test resources MUST include at least two distinct YAML configs (`clientGenConfig` A and B) and a no-config scenario.
- **FR-023**: Harness tests MUST verify class-level field emission, operation-level header emission rules, and SoapAction fallback behavior.
- **FR-024**: All new tests MUST pass during `mvn test` without network access or external Maven process invocation.
- **FR-025**: `prot-cxf-codegen-soap-client` module tests MUST remain green to confirm generated-annotation compatibility.
- **FR-026**: The no-config scenario MUST be treated as backward-compatible baseline behavior.
- **FR-027**: Unit test coverage for `prot.cxf.plugin.*` classes MUST reach minimum 80% line coverage; all tests MUST pass with code coverage reports generated.
- **FR-028**: Test scenarios MUST cover: (1) ping.wsdl without config, (2) ping.wsdl with config, (3) calculator.wsdl with multi-operation headers and override variations, (4) specific test cases for header format transformations (concatenated strings for static, Class<?> for dynamic).

### Key Entities *(include if feature involves data)*

- **ClientGenConfig**: Root YAML model containing `configKey`, optional `baseUrl`, optional `jaxbContextPaths`, top-level `staticHeaders` (list of "X=Y" strings), top-level `dynamicHeaders` (list of FQCN strings), and `operations` map
- **StaticHeader** (Deprecated for new format): Previously used `{name, value, ifExisting}` tuple; replaced by simple concatenated strings at YAML level (internal model may keep both for backward compatibility)
- **OperationConfig**: Per-operation configuration containing `action`, `staticHeaders` (list of "X=Y" strings), and `dynamicHeaders` (list of FQCN strings)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Generated SEI always includes `@prot.soap.SoapClient(value=resolvedConfigKey)` with fallback behavior verified for empty `configKey`.
- **SC-002**: Generated SEI emits `baseUrl`, `jaxbContextPaths`, `staticHeaders` (as "X=Y" strings), and `dynamicHeaders` (as Class<?> references) inside `@prot.soap.SoapClient` only when configured.
- **SC-002a**: Static headers in generated annotations are concatenated strings in format `"X=Y"` (e.g., `staticHeaders = {"X-Calc-Op=minus", "X-Overflow=false"}`).
- **SC-002b**: Dynamic headers in generated annotations are Class<?> references (e.g., `dynamicHeaders = {prot.soap.header.SoapHeaderAContributor.class}`).
- **SC-003**: Three harness tests (config A, config B, no config) pass consistently in `prot-cxf-codegen/prot-cxf-codegen-plugin`.
- **SC-004**: For each generated operation, `@prot.soap.SoapAction` is present and uses configured `action`, else WSDL binding `soapAction`, else operation-name fallback.
- **SC-004a**: Operation-level `@prot.soap.SoapMethodHeader` emits staticHeaders as concatenated strings and dynamicHeaders as Class<?> references using the same format as class-level.
- **SC-005**: Both file-path-first and classpath-fallback config loading behaviors are verified by automated tests.
- **SC-006**: All Mojo/harness tests (`FR-019` through `FR-023`) pass in under 30 seconds on a standard developer machine.
- **SC-007**: Unit test coverage for `prot.cxf.plugin.*` classes reaches at least 80 % of lines exercised by the new test suite.
- **SC-007a**: Tests cover new YAML field aliases for `staticHeaders` and `dynamicHeaders` normalization.
- **SC-007b**: Tests verify header format transformation from YAML to generated annotation code (string concatenation for static, Class<?> for dynamic).
- **SC-008**: No test relies on spawning an external Maven process; all assertions run within the same JVM as the test runner.
- **SC-009**: YAML aliases for `baseUrl`, `jaxbContextPaths`, `staticHeaders`, and `dynamicHeaders` are correctly normalized to Java model fields.
- **SC-010**: Operation-level `@prot.soap.SoapMethodHeader` is emitted only when operation headers exist, and omitted otherwise.
- **SC-011**: `prot-cxf-codegen-soap-client` module tests pass unchanged after generator updates.
- **SC-012**: All unit tests pass with code coverage reports confirming ≥80% line coverage for prot.cxf.plugin package.
- **SC-013**: Test matrix includes four distinct scenarios: (1) ping.wsdl without config (no custom annotations), (2) ping.wsdl with config (basic annotations), (3) calculator.wsdl with multi-operation headers and overrides (complex scenarios), (4) specific cases for concatenated string and Class<?> format validation.
- **SC-014**: Operation-level YAML header field aliases (`x-static-headers`, `static-headers`, `staticHeaders`, `staticheaders` and `x-dynamic-headers`, `dynamic-headers`, `dynamicHeaders`, `dynamicheaders`) are all correctly recognized and normalized.

## Assumptions

- Users have CXF 4.2.0 or compatible version
- YAML parsing uses an existing library (SnakeYAML or similar)
- The existing CustomSEIGenerator will be extended, not replaced
- Java 21 is the target runtime