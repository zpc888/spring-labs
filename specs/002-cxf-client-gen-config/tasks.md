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
| User Story 1 | User Story 2 | Independent (parallel) |
| User Story 1 | User Story 3 | Independent (parallel) |
| User Story 1 | User Story 4 | Independent (parallel) |

## Parallel Execution

Stories can execute in parallel after Foundational phase:
- User Story 1, 2, 3 are independent of each other
- User Story 4 (testing) can run after any story completes

## Implementation Strategy

**MVP Scope**: User Story 1 + Foundational phase
- Core: ClientGenConfig + ConfigLoader + CustomSEIGenerator extension
- Provides: YAML config → annotations on SEI

**Incremental Delivery**:
1. Setup → Foundational (blocking prerequisites)
2. Foundational → US1 (P1) - Core annotation feature
3. Any story → US2 (P2) - Per-operation annotations
4. Any story → US3 (P2) - File/classpath loading
5. Any story → US4 (P3) - Maven test

---

## Phase 1: Setup

| ID | Task |
|----|------|
| T001 | Add SnakeYAML dependency to pom.xml in prot-cxf-codegen-plugin |

- [ ] T001 Add SnakeYAML dependency to pom.xml in prot-cxf-codegen-plugin

---

## Phase 2: Foundational

| ID | Task |
|----|------|
| T002 | Create ClientGenConfig.java - YAML config model |
| T003 | Create ConfigLoader.java - File/classpath loader |
| T004 | Update CustomSEIGenerator.java to read client-gen-config argument |

- [ ] T002 Create ClientGenConfig.java in prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ClientGenConfig.java
- [ ] T003 Create ConfigLoader.java in prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ConfigLoader.java
- [ ] T004 Update CustomSEIGenerator.java in prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/CustomSEIGenerator.java to read --client-gen-config from extraargs

---

## Phase 3: User Story 1 - Custom Annotation on Generated SEI Interface (P1)

**Goal**: Allow users to add custom annotations to the generated SEI interface from YAML config

**Independent Test**: Run wsdl2java with `--client-gen-config` pointing to YAML with sei.annotations. Verify generated SEI includes annotations.

### Tasks

| ID | Task |
|----|------|
| T005 | Modify velocity template to support SEI annotations |
| T006 | Create example YAML config with SEI annotations |

- [ ] T005 [P] [US1] Modify velocity template to include SEI annotations in prot-cxf-codegen-plugin/src/main/resources/prot-cxf-sei.vm
- [ ] T006 [US1] Create example client-gen-config.yaml with SEI annotations in prot-cxf-codegen-plugin/src/main/resources/

---

## Phase 4: User Story 2 - Custom Annotation Per Operation (P2)

**Goal**: Allow per-operation annotations mapped by operation name

**Independent Test**: YAML with operations.mapping. Generated methods have correct annotations.

### Tasks

| ID | Task |
|----|------|
| T007 | Extend velocity template for method annotations |

- [ ] T007 [P] [US2] Extend velocity template to include method annotations in prot-cxf-codegen-plugin/src/main/resources/prot-cxf-sei.vm

---

## Phase 5: User Story 3 - YAML Config Loading (P2)

**Goal**: Load config from file path or classpath transparently

**Independent Test**: File-based config and classpath-based config both work.

### Tasks

| ID | Task |
|----|------|
| T008 | Enhance ConfigLoader with classpath fallback |

- [ ] T008 [US3] Enhance ConfigLoader.java to support classpath fallback in prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ConfigLoader.java

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

- [ ] T009 Add SnakeYAML dependency to soap-client pom.xml in prot-cxf-codegen/prot-cxf-codegen-soap-client/pom.xml
- [ ] T010 [P] [US4] Create Maven Invoker test in prot-cxf-codegen/prot-cxf-codegen-soap-client/src/test/java/
- [ ] T011 [US4] Run Maven Invoker test to verify integration works

---

## Phase 7: Polish

| ID | Task |
|----|------|
| T012 | Update README with client-gen-config usage |
| T013 | Verify all acceptance scenarios pass |

- [ ] T012 Update README.md with client-gen-config usage in prot-cxf-codegen/README.md
- [ ] T013 Verify all acceptance scenarios from spec.md pass

---

## Summary

| Metric | Value |
|--------|-------|
| Total Tasks | 13 |
| Setup | 1 |
| Foundational | 3 |
| User Story 1 | 2 |
| User Story 2 | 1 |
| User Story 3 | 1 |
| User Story 4 | 3 |
| Polish | 2 |
| Parallel Opportunities | 3 |

## Format Validation

All tasks follow checklist format: ✅
- [ ] T001 ✓ (checkbox, ID, description with file path)
- [ ] T002 ✓ (checkbox, ID, description with file path)
- [ ] etc.

## Extension Hooks

No after_tasks hooks registered (optional hooks skipped).