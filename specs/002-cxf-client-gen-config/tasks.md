# Tasks: CXF SOAP Client Generation Config Plugin

**Feature**: CXF SOAP Client Generation Config Plugin  
**Branch**: `002-cxf-client-gen-config`  
**Generated**: 2026-04-19

## Dependencies

| From | To | Type |
|-------|-----|------|
| Setup | Foundational | Blocks |
| Foundational | User Story 1 | Blocks |
| Foundational | User Story 2 | Blocks |
| Foundational | User Story 3 | Blocks |
| Foundational | User Story 4 | Blocks |
| Foundational | User Story 5 | Blocks |
| User Story 1 | User Story 2 | Independent (parallel) |
| User Story 1 | User Story 3 | Independent (parallel) |
| User Story 1 | User Story 4 | Independent (parallel) |
| User Story 1 | User Story 5 | Independent (parallel) |

## Parallel Execution

Stories can execute in parallel after Foundational phase:
- User Story 1, 2, 3, 5 are independent of each other
- User Story 4 (integration test) can run after any story completes
- Within US5: T015–T020 are all independently parallelisable after T014

## Implementation Strategy

**MVP Scope**: User Story 1 + Foundational phase
- Core: ClientGenConfig + ConfigLoader + CustomSEIGenerator extension
- Provides: YAML config → annotations on SEI

**Incremental Delivery**:
1. Setup → Foundational (blocking prerequisites)
2. Foundational → US1 (P1) - Core annotation feature
4. Any story → US2 (P2) - Per-operation annotations
5. Any story → US3 (P2) - File/classpath loading
6. Any story → US5 (P2) - Mojo unit tests
7. Any story → US4 (P3) - Maven integration test

---

## Phase 1: Setup

| ID | Task |
|----|------|
| T001 | Add SnakeYAML dependency to pom.xml in prot-cxf-codegen-plugin |

- [X] T001 Add SnakeYAML dependency to pom.xml in prot-cxf-codegen-plugin

---

## Phase 2: Foundational

| ID | Task |
|----|------|
| T002 | Create ClientGenConfig.java - YAML config model |
| T003 | Create ConfigLoader.java - File/classpath loader |
| T004 | Update CustomSEIGenerator.java to read client-gen-config argument |

- [X] T002 Create ClientGenConfig.java in prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ClientGenConfig.java
- [X] T003 Create ConfigLoader.java in prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ConfigLoader.java
- [X] T004 Update CustomSEIGenerator.java in prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/CustomSEIGenerator.java to read --client-gen-config from extraargs

---

## Phase 3: User Story 1 - Custom Annotation on Generated SEI Interface (P1)

**Goal**: Allow users to add custom annotations to the generated SEI interface from YAML config

**Independent Test**: Run wsdl2java with `--client-gen-config` pointing to YAML with sei.annotations. Verify generated SEI includes annotations.

### Tasks

| ID | Task |
|----|------|
| T005 | Modify velocity template to support SEI annotations |
| T006 | Create example YAML config with SEI annotations |

- [X] T005 [P] [US1] Modify velocity template to include SEI annotations in prot-cxf-codegen-plugin/src/main/resources/prot-cxf-sei.vm
- [X] T006 [US1] Create example client-gen-config.yaml with SEI annotations in prot-cxf-codegen-plugin/src/main/resources/

---

## Phase 4: User Story 2 - Custom Annotation Per Operation (P2)

**Goal**: Allow per-operation annotations mapped by operation name

**Independent Test**: YAML with operations.mapping. Generated methods have correct annotations.

### Tasks

| ID | Task |
|----|------|
| T007 | Extend velocity template for method annotations |

- [X] T007 [P] [US2] Extend velocity template to include method annotations in prot-cxf-codegen-plugin/src/main/resources/prot-cxf-sei.vm

---

## Phase 5: User Story 3 - YAML Config Loading (P2)

**Goal**: Load config from file path or classpath transparently

**Independent Test**: File-based config and classpath-based config both work.

### Tasks

| ID | Task |
|----|------|
| T008 | Enhance ConfigLoader with classpath fallback |

- [X] T008 [US3] Enhance ConfigLoader.java to support classpath fallback in prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ConfigLoader.java

---

## Phase 6: User Story 4 - Maven Integration Test (P3)

**Goal**: Verify the full integration works with Maven

**Independent Test**: Maven Invoker Plugin test executes wsdl2java and verifies generated output.

### Tasks

| ID | Task |
|----|------|
| T009 | Add SnakeYAML dependency to soap-client pom.xml |
| T010 | Create Maven Invoker test |
| T011 | Run test to verify integration |

- [X] T009 Add SnakeYAML dependency to soap-client pom.xml in prot-cxf-codegen/prot-cxf-codegen-soap-client/pom.xml
- [X] T010 [P] [US4] Create Maven Invoker test in prot-cxf-codegen/prot-cxf-codegen-soap-client/src/test/java/
- [X] T011 [US4] Run Maven Invoker test to verify integration works

---

## Phase 7: User Story 5 - Mojo Unit Tests for Plugin Classes (P2)

**Goal**: Provide a fast, self-contained unit-test suite for `ClientGenConfig`, `ConfigLoader`, and `CustomSEIGenerator` using JUnit 5 — no external Maven process required (FR-010 through FR-016).

**Independent Test**: Run `mvn test` inside `prot-cxf-codegen/prot-cxf-codegen-plugin`; all tests pass without network or a forked Maven process (SC-005, SC-007).

### Tasks

| ID | Task |
|----|------|
| T014 | Add JUnit 5 + Mockito test dependencies to plugin pom.xml |
| T015 | Create test YAML fixtures on the test classpath |
| T016 | Create ConfigLoaderFileTest — file-system loading (FR-012) |
| T017 | Create ConfigLoaderClasspathTest — classpath fallback (FR-013) |
| T018 | Create ConfigLoaderErrorTest — missing / malformed YAML error handling |
| T019 | Create CustomSEIGeneratorSeiAnnotationTest — class-level annotations (FR-014) |
| T020 | Create CustomSEIGeneratorOperationAnnotationTest — per-operation annotations (FR-015) |
| T021 | Run `mvn test` to confirm all tests pass (FR-016) |

- [X] T014 [US5] Add `junit-jupiter` (test scope) and `mockito-core` (test scope) dependencies to prot-cxf-codegen/prot-cxf-codegen-plugin/pom.xml; configure `maven-surefire-plugin` ≥ 3.x to enable JUnit 5 provider
- [X] T015 [P] [US5] Create three YAML fixture files under prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/prot/cxf/plugin/: `sei-annotations.yaml` (sei.annotations: ["com.example.CustomAnnotation"]), `operation-annotations.yaml` (operations: {ping: ["com.example.PingHandler"]}), and `malformed.yaml` (invalid content such as `{bad: [unclosed`)
- [X] T016 [P] [US5] Create ConfigLoaderFileTest.java in prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/: write `sei-annotations.yaml` content to a `@TempDir` temp file, call `ConfigLoader.load(tempFile.toAbsolutePath().toString())`, assert the returned `ClientGenConfig` is non-null and `getSeiAnnotations()` contains `"com.example.CustomAnnotation"` (covers FR-012)
- [X] T017 [P] [US5] Create ConfigLoaderClasspathTest.java in prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/: call `ConfigLoader.load("prot/cxf/plugin/sei-annotations.yaml")` — a path that does not exist on the file system but is present on the test classpath — assert returned `ClientGenConfig` is non-null and `getSeiAnnotations()` equals `["com.example.CustomAnnotation"]` (covers FR-013)
- [X] T018 [P] [US5] Create ConfigLoaderErrorTest.java in prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/: (a) assert `ConfigLoader.load(null)` returns null; (b) assert `ConfigLoader.load("  ")` returns null; (c) assert `ConfigLoader.load("no-such-file.yaml")` throws `ToolException` whose message contains `"Unable to find"`; (d) write `malformed.yaml` to a temp file, assert `ConfigLoader.load(path)` throws `ToolException` whose message contains `"Invalid YAML"` (covers FR-016 error cases)
- [X] T019 [US5] Create CustomSEIGeneratorSeiAnnotationTest.java in prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/: load `prot-cxf-sei.vm` from the classpath via `VelocityEngine`; build a `VelocityContext` with a minimal stub `intf` (PackageName, Name, empty Imports/Annotations/Methods) and set `customSeiAnnotations = List.of("com.example.CustomAnnotation")` and `customOperationAnnotations = emptyMap()`; render to a String; assert the output contains `@com.example.CustomAnnotation` before the `public interface` line (covers FR-014)
- [X] T020 [US5] Create CustomSEIGeneratorOperationAnnotationTest.java in prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/: render `prot-cxf-sei.vm` with `customSeiAnnotations = emptyList()`, `customOperationAnnotations = Map.of("ping", List.of("com.example.PingHandler"))`, and a two-method stub `intf` (methods "ping" and "echo"); assert rendered source contains `@com.example.PingHandler` near the `ping` method and does NOT contain `@com.example.PingHandler` near the `echo` method (covers FR-015)
- [X] T021 [US5] Run `mvn test` inside prot-cxf-codegen/prot-cxf-codegen-plugin and confirm "BUILD SUCCESS" with zero failures; all six test classes (T016–T020 = 5 classes, T014 setup enables them all) must be reported as passed (covers FR-016, SC-005, SC-007)

---

## Phase 8: Polish

| ID | Task |
|----|------|
| T012 | Update README with client-gen-config usage |
| T013 | Verify all acceptance scenarios pass |

- [X] T012 Update README.md with client-gen-config usage in prot-cxf-codegen/README.md
- [X] T013 Verify all acceptance scenarios from spec.md pass

---

## Summary

| Metric | Value |
|--------|-------|
| Total Tasks | 21 |
| Setup | 1 |
| Foundational | 3 |
| User Story 1 | 2 |
| User Story 2 | 1 |
| User Story 3 | 1 |
| User Story 4 | 3 |
| User Story 5 | 8 |
| Polish | 2 |
| Parallel Opportunities | 6 (T015, T016, T017, T018, T019, T020) |

## Format Validation

All tasks follow checklist format: ✅
- T001–T012 completed (`[X]`); T013 and T014–T021 pending (`[ ]`)
- Every task: checkbox · sequential ID · optional [P] · optional [USn] · description with exact file path

## Extension Hooks

No after_tasks hooks registered (optional hooks skipped).