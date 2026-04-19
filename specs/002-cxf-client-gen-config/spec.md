# Feature Specification: CXF SOAP Client Generation Config Plugin

**Feature Branch**: `002-cxf-client-gen-config`  
**Created**: 2026-04-19  
**Status**: Draft  
**Input**: User description: "enhance prot-cxf frontend custom plugin to add extra generation command argument client-gen-config whose value is a yaml file from file or classpath, the yaml file contains the extra annotation based on the name of SEI, and the methods' annotation based on operation. Also I need a unit test to run maven using this plugin."

## Clarifications

### Session 2026-04-19

- Q: What should be the exact name of the new command-line argument? → A: `client-gen-config`
- Q: What should the YAML configuration file format structure look like? → A: Hierarchical with operations AND class-level sections
- Q: How should the YAML file be loaded - from file path or classpath, or both? → A: Both auto-detect (file path first, then classpath)
- Q: What test framework do you want to use for the Maven unit test? → A: Maven Invoker Plugin
- Q: What annotation format should be used in the YAML file? → A: Fully qualified class names

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

### User Story 4 - Maven Integration Test (Priority: P3)

As a developer, I want to verify the plugin works correctly in Maven builds, so that I can validate the integration works end-to-end.

**Why this priority**: Ensures the feature works in the primary build tool (Maven).

**Independent Test**: Run Maven with the invoker plugin to execute wsdl2java and verify generated output.

**Acceptance Scenarios**:

1. **Given** a Maven project with the CXF plugin configured, **When** running `mvn generate-sources`, **Then** code is generated with custom annotations.
2. **Given** a test that executes Maven, **When** the test runs, **Then** it verifies the generated sources contain expected annotations.

---

### Edge Cases

- What happens when the YAML config file is malformed?
- How does system handle missing operation mappings (undefined operations)?
- What if two annotations are specified for the same operation/class?
- How to handle empty or empty YAML file?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The CXF wsdl2java tool MUST accept a new `--client-gen-config` command-line argument.
- **FR-002**: The `--client-gen-config` value MUST be a path to a YAML configuration file.
- **FR-003**: The YAML config MUST support a hierarchical structure with:
  - `sei:` section for class-level annotations
  - `operations:` section mapping operation names to annotations
- **FR-004**: The config file MUST first attempt to load from the file system, then fallback to classpath.
- **FR-005**: When config is not found, the tool MUST fail gracefully with a clear error message.
- **FR-006**: The generated SEI interface MUST include class-level annotations specified in the `sei:` section.
- **FR-007**: Each method in the generated SEI MUST include annotations specified for its operation.
- **FR-008**: Annotation values in YAML MUST be fully qualified class names.
- **FR-009**: A Maven Invoker Plugin-based unit test MUST verify the full integration.

### Key Entities *(include if feature involves data)*

- **ClientGenConfig**: The YAML configuration entity containing sei and operations sections
- **AnnotationSpec**: A mapping of annotation name to optional parameters
- **SEIGenerator**: Modified CXF SEI generator that reads config and applies annotations

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can generate SEI with custom annotations using `--client-gen-config` argument
- **SC-002**: Generated code compiles without errors with all specified annotations
- **SC-003**: Unit test passes, verifying Maven integration works correctly
- **SC-004**: Both file and classpath config loading work correctly

## Assumptions

- Users have CXF 4.2.0 or compatible version
- YAML parsing uses an existing library (SnakeYAML or similar)
- The existing CustomSEIGenerator will be extended, not replaced
- Java 21 is the target runtime