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

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Custom Annotation on Generated SEI Interface (Priority: P1)

As a SOAP client developer, I want to add custom annotations to the generated SEI interface based on configuration, so that I can extend the generated code with framework-specific annotations.

**Why this priority**: Core value proposition - the main feature request is to add extra annotations to SEI.

**Independent Test**: Run wsdl2java with the `--client-gen-config` argument pointing to a YAML file containing class-level annotations. Verify the generated SEI interface contains the specified annotations.

**Acceptance Scenarios**:

1. **Given** a valid YAML config file with SEI annotations, **When** running wsdl2java with `--client-gen-config`, **Then** the generated SEI interface includes the specified annotations.
2. **Given** a YAML config on classpath, **When** running wsdl2java with `--client-gen-config=my-config.yaml`, **Then** the config is loaded from classpath and annotations are applied.

---

### User Story 2 - Custom Annotation Per Operation (Priority: P2)

As a SOAP client developer, I want to add custom annotations to individual methods in the generated SEI based on operation names, so that I can add method-specific annotations like @WebMethod, @HandlerChain, etc.

**Why this priority**: Enables fine-grained control over each method's annotations.

**Independent Test**: Run wsdl2java with YAML containing operation-specific annotations. Verify each method in generated SEI has the correct annotations.

**Acceptance Scenarios**:

1. **Given** YAML config with operation "ping" mapped to annotation "com.example.PingHandler", **When** running wsdl2java, **Then** the generated method for "ping" operation has the annotation.
2. **Given** YAML config with multiple operations, **When** code is generated, **Then** each operation's method gets its mapped annotation.

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

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The CXF wsdl2java tool MUST accept a new `--client-gen-config` command-line argument.
- **FR-002**: The `--client-gen-config` value MUST accept a YAML location resolvable from file path or classpath.
- **FR-003**: The YAML config MUST support a hierarchical structure with:
  - `sei:` section for class-level annotations
  - `operations:` section mapping operation names to annotations
- **FR-004**: The config file MUST first attempt to load from the file system, then fallback to classpath.
- **FR-005**: When config is not found, the tool MUST fail gracefully with a clear error message.
- **FR-006**: The generated SEI interface MUST include class-level annotations specified in the `sei:` section.
- **FR-007**: Each method in the generated SEI MUST include annotations specified for its operation.
- **FR-008**: Annotation values in YAML MUST be fully qualified class names.
- **FR-009**: Plugin-level tests MUST be implemented in `prot-cxf-codegen/prot-cxf-codegen-plugin` using `maven-plugin-testing-harness` (or equivalent in-process Mojo harness), not Maven Invoker.
- **FR-010**: Tests MUST execute wsdl2java with frontend `prot-cxf` and use copied fixture `src/test/resources/backend/soap/ping.wsdl`.
- **FR-011**: Test resources MUST include at least two distinct YAML configs (`clientGenConfig` A and B) producing different expected annotation outcomes.
- **FR-012**: A dedicated harness test MUST verify scenario A applies expected class-level and operation-level annotations to generated sources.
- **FR-013**: A dedicated harness test MUST verify scenario B applies its own expected annotations and does not match scenario A assertions.
- **FR-014**: A dedicated harness test MUST verify generation succeeds without `clientGenConfig` and emits no config-driven custom annotations.
- **FR-015**: All new tests MUST pass during `mvn test` without network access or external Maven process invocation.
- **FR-016**: The no-config scenario MUST be treated as backward-compatible baseline behavior.

### Key Entities *(include if feature involves data)*

- **ClientGenConfig**: The YAML configuration entity containing sei and operations sections
- **AnnotationSpec**: A mapping of annotation name to optional parameters
- **SEIGenerator**: Modified CXF SEI generator that reads config and applies annotations

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can generate SEI with custom annotations using `--client-gen-config` argument
- **SC-002**: Generated code compiles without errors with all specified annotations
- **SC-003**: Three harness tests (config A, config B, no config) pass consistently in `prot-cxf-codegen/prot-cxf-codegen-plugin`.
- **SC-004**: Both file and classpath config loading work correctly
- **SC-005**: All Mojo unit tests (`FR-012` through `FR-015`) pass in under 30 seconds on a standard developer machine.
- **SC-006**: Unit test coverage for `prot.cxf.plugin.*` classes reaches at least 80 % of lines exercised by the new test suite.
- **SC-007**: No test relies on spawning an external Maven process; all assertions run within the same JVM as the test runner.

## Assumptions

- Users have CXF 4.2.0 or compatible version
- YAML parsing uses an existing library (SnakeYAML or similar)
- The existing CustomSEIGenerator will be extended, not replaced
- Java 21 is the target runtime