# Tasks: CXF SOAP Client Generation Config Plugin

**Input**: Design documents from `/home/zpc/works/study/sandbox/spring-labs/specs/002-cxf-client-gen-config/`  
**Prerequisites**: `plan.md` (required), `spec.md` (required), `data-model.md`, `quickstart.md`  
**Tests**: Included (explicitly required by the feature spec and user request).  
**Organization**: Tasks are grouped by user story and ordered by dependencies.  
**Key Naming Conventions**:
- **YAML**: kebab-case with `x-` prefix (`x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations`)
- **Java Model**: camelCase (`configKey`, `staticHeaders`, `dynamicHeaders`, `operations`)
- **Annotations**: All use `prot.soap.*` namespace (`@prot.soap.RegisteredSoapClient`, `@prot.soap.StaticHeaders`, `@prot.soap.DynamicHeaders`, `@prot.soap.SoapAction`)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Align plugin test/runtime setup for the enhanced YAML model with kebab-case YAML / camelCase Java mapping and prot.soap.* annotations.

- [X] T001 Update plugin test dependency and surefire configuration in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/pom.xml` to ensure JUnit 5 + harness tests run consistently with SnakeYAML kebab-case to camelCase mapping enabled
- [X] T002 [P] Add/refresh reusable test YAML fixtures for enhanced model (kebab-case: `x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations`) in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/prot/cxf/plugin/` with examples of both config scenarios

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Introduce the canonical config model with YAML kebab-case fields and Java camelCase properties, plus prot.soap.* annotation namespace.

**⚠️ CRITICAL**: No user story work should start until this phase is complete.

- [X] T003 Refactor root config model in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ClientGenConfig.java` with camelCase fields: `configKey`, `staticHeaders`, `dynamicHeaders`, `Map<String, OperationConfig> operations` (SnakeYAML will map YAML `x-config-key` → `configKey`, etc.)
- [X] T004 [P] Create or update static header model class in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/StaticHeader.java` with camelCase properties `name`, `value`, `ifExisting`
- [X] T005 [P] Create or update operation model class in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/OperationConfig.java` with `action` property and helper for fallback resolution (when action is empty/null, use operation name)
- [X] T006 Implement config parsing and canonical-key validation in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ConfigLoader.java` with: (a) SnakeYAML configured to map kebab-case YAML to camelCase Java fields, (b) rejection of misspelled key `opertions` with error message pointing to canonical `x-operations`, (c) validation that `dynamicHeaders` are FQCN, (d) file-first/classpath-fallback load order preserved
- [X] T007 Update model-focused unit coverage in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/ClientGenConfigTest.java` for new camelCase fields and resolution helpers: `resolvedConfigKey` (uses `configKey` if non-empty, else portType name), operation action fallback

**Checkpoint**: Enhanced YAML model with kebab-case/camelCase mapping and loader validation are stable.

---

## Phase 3: User Story 1 - Custom Annotation on Generated SEI Interface (Priority: P1) 🎯 MVP

**Goal**: Always emit `@prot.soap.RegisteredSoapClient(resolvedConfigKey)` and conditionally emit `@prot.soap.StaticHeaders` / `@prot.soap.DynamicHeaders` from enhanced YAML (YAML keys in kebab-case with `x-` prefix).

**Independent Test**: Generate from ping WSDL with YAML containing `x-config-key`, `x-static-headers`, `x-dynamic-headers` and verify mandatory vs optional class-level annotation behavior with prot.soap.* namespace.

### Tests for User Story 1

- [X] T008 [P] [US1] Update SEI template unit assertions in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/CustomSEIGeneratorSeiAnnotationTest.java` to expect: (a) mandatory `@prot.soap.RegisteredSoapClient(...)` with resolved configKey, (b) `@prot.soap.StaticHeaders` only when YAML `x-static-headers` non-empty, (c) `@prot.soap.DynamicHeaders` only when YAML `x-dynamic-headers` non-empty

### Implementation for User Story 1

- [X] T009 [US1] Update SEI annotation resolution logic in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/CustomSEIGenerator.java` to: (a) read parsed `ClientGenConfig` with camelCase fields from YAML (`x-config-key` → `configKey`), (b) compute `resolvedConfigKey` = `configKey` when non-empty else portType name, (c) prepare prot.soap.* annotation strings for class-level injection
- [X] T010 [US1] Update class-level annotation emission in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/resources/prot-cxf-sei.vm` to: (a) always emit `@prot.soap.RegisteredSoapClient(resolvedConfigKey)`, (b) conditionally emit `@prot.soap.StaticHeaders(...)` with header list from camelCase `staticHeaders`, (c) conditionally emit `@prot.soap.DynamicHeaders(...)` with FQCNs from camelCase `dynamicHeaders`

**Checkpoint**: SEI class-level annotation generation with prot.soap.* namespace satisfies FR-006 through FR-010, using kebab-case YAML and camelCase Java model.

---

## Phase 4: User Story 4 - Plugin-Level wsdl2java Test Matrix (Priority: P1)

**Goal**: Keep existing ping harness scenarios working under the enhanced YAML model (kebab-case with `x-` prefix, camelCase Java fields) and prot.soap.* annotation assertions.

**Independent Test**: Run harness tests for config A, config B, and no-config ping scenarios in plugin module; verify generated SEI contains expected prot.soap.* annotations.

### Tests for User Story 4

- [X] T011 [P] [US4] Migrate ping harness YAML A/B to enhanced kebab-case schema in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/harness/scenario-a/client-gen-config.yaml` and `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/harness/scenario-b/client-gen-config.yaml`: YAML format uses `x-config-key`, `x-static-headers: [{name, value, ifExisting?}]`, `x-dynamic-headers: [FQCN...]`, `x-operations.<operationName>.action`
- [X] T012 [P] [US4] Normalize ping harness plugin args for `client-gen-config` in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/harness/scenario-a/pom.xml` and `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/harness/scenario-b/pom.xml` with proper `-client-gen-config` extraarg and YAML file reference
- [X] T013 [US4] Fix ping generation assertions in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/Wsdl2JavaProtCxfHarnessTest.java` to verify: (a) mandatory `@prot.soap.RegisteredSoapClient`, (b) `@prot.soap.SoapAction` presence on all operations, (c) configured action override from YAML `x-operations.<op>.action`, (d) fallback action = operation name when empty, (e) no-config baseline behavior

### Implementation for User Story 4

- [X] T014 [US4] Refactor ping harness helper methods in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/Wsdl2JavaProtCxfHarnessTest.java` for explicit scenario expectations and stable generated-source lookup, with assertions using prot.soap.* annotation FQCNs

**Checkpoint**: Existing ping wsdl generation unit tests are fully aligned with the enhanced YAML model (kebab-case + camelCase Java) and prot.soap.* namespace.

---

## Phase 5: User Story 2 - Custom Annotation Per Operation (Priority: P2)

**Goal**: Always emit method-level `@prot.soap.SoapAction(resolvedActionName)` where `resolvedActionName` uses config `x-operations.<op>.action` or operation fallback.

**Independent Test**: Template and harness assertions prove each generated method has `@prot.soap.SoapAction` with correct override/fallback behavior based on YAML `x-operations` structure.

### Tests for User Story 2

- [X] T015 [P] [US2] Update operation template tests in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/CustomSEIGeneratorOperationAnnotationTest.java` to assert: (a) mandatory `@prot.soap.SoapAction(...)` on every method, (b) action value from YAML `x-operations.<operationName>.action` when non-empty, (c) fallback to operation name when action empty/null

### Implementation for User Story 2

- [X] T016 [US2] Update operation annotation mapping in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/CustomSEIGenerator.java` to: (a) read `operations` map from parsed `ClientGenConfig` (camelCase field from YAML `x-operations`), (b) for each generated operation, compute `resolvedActionName` = configured action when non-empty else operation name, (c) provide resolved action value for each method
- [X] T017 [US2] Update method annotation rendering in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/resources/prot-cxf-sei.vm` to emit `@prot.soap.SoapAction("resolvedActionName")` for every generated operation method

**Checkpoint**: Method-level action annotation requirements FR-011 and FR-012 are satisfied with prot.soap.SoapAction using kebab-case YAML `x-operations` data.

---

## Phase 6: User Story 3 - YAML Config Loading from File and Classpath (Priority: P2)

**Goal**: Preserve file-first/classpath-fallback loading and clear validation errors for invalid keys/values; reject misspelled YAML keys (e.g., `opertions` instead of `x-operations`).

**Independent Test**: Loader tests pass for file-based kebab-case YAML, classpath-based YAML, missing config, malformed YAML, and misspelled key rejection with guidance to canonical names.

### Tests for User Story 3

- [X] T018 [P] [US3] Update file-loading tests in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/ConfigLoaderFileTest.java` to assert parsing of kebab-case YAML keys (`x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations.<name>.action`) into camelCase Java fields (`configKey`, `staticHeaders`, `dynamicHeaders`, `operations`)
- [X] T019 [P] [US3] Update classpath-loading tests in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/ConfigLoaderClasspathTest.java` to validate enhanced kebab-case schema from classpath fixtures with correct mapping to camelCase Java model
- [X] T020 [US3] Update error-path tests in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/ConfigLoaderErrorTest.java` to require clear errors for: (a) missing config file, (b) invalid YAML syntax, (c) misspelled key `opertions` (error message MUST mention canonical `x-operations`), (d) invalid FQCN in `x-dynamic-headers`

### Implementation for User Story 3

- [X] T021 [US3] Finalize loader exceptions/messages in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/main/java/prot/cxf/plugin/ConfigLoader.java` to: (a) configure SnakeYAML PropertyNamingStrategy for kebab-case to camelCase mapping, (b) provide clear validation error messages identifying misspelled YAML keys vs canonical names, (c) satisfy updated file/classpath/error tests

**Checkpoint**: Config loading behavior with kebab-case YAML → camelCase Java mapping and validation are reliable and test-covered.

---

## Phase 7: User Story 5 - Mojo Unit Tests for Plugin Classes (Priority: P2)

**Goal**: Expand harness-style unit coverage with new calculator.wsdl generation scenarios in addition to ping; verify kebab-case YAML parsing and prot.soap.* annotation generation.

**Independent Test**: `mvn test` in plugin module passes with both ping and calculator scenario assertions; both use enhanced kebab-case YAML with `x-` prefix fields.

### Tests for User Story 5

- [X] T022 [P] [US5] Add calculator config fixtures for scenario A/B in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/harness/scenario-calculator-a/client-gen-config.yaml` and `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/harness/scenario-calculator-b/client-gen-config.yaml` using kebab-case YAML format (`x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations`)
- [X] T023 [P] [US5] Add calculator harness POM fixtures in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/harness/scenario-calculator-a/pom.xml` and `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/harness/scenario-calculator-b/pom.xml` wired to `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/resources/backend/soap/calculator.wsdl` with `-client-gen-config` extraargs
- [X] T024 [US5] Add calculator generation test methods in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/Wsdl2JavaProtCxfHarnessTest.java` with expectations: (a) mandatory `@prot.soap.RegisteredSoapClient` with resolved configKey, (b) per-method mandatory `@prot.soap.SoapAction`, (c) configured action override from YAML `x-operations.<method>.action`, (d) fallback action = operation name for empty action
- [X] T025 [US5] Add calculator-specific assertions in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/Wsdl2JavaProtCxfHarnessTest.java` for optional `@prot.soap.StaticHeaders` and `@prot.soap.DynamicHeaders` emission only when YAML `x-static-headers` / `x-dynamic-headers` configured and non-empty

### Implementation for User Story 5

- [X] T026 [US5] Consolidate shared harness utilities (scenario setup, generated-file lookup for ping/calculator) in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/src/test/java/prot/cxf/plugin/Wsdl2JavaProtCxfHarnessTest.java` with stable assertion helpers for prot.soap.* annotation verification; ensure kebab-case YAML parsing is validated end-to-end

**Checkpoint**: Calculator wsdl generation scenarios are covered and passing; kebab-case YAML to camelCase Java model mapping verified for both ping and calculator WSDLs.

---

## Final Phase: Polish & Cross-Cutting Concerns

**Purpose**: Validate end-to-end consistency with kebab-case YAML, camelCase Java model, and prot.soap.* annotation namespace; update documentation and run full test suite.

- [X] T027 [P] Update feature usage/examples for enhanced YAML kebab-case format in `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/README.md` and `/home/zpc/works/study/sandbox/spring-labs/specs/002-cxf-client-gen-config/quickstart.md`: showcase YAML with `x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations.<op>.action`, and example generated Java with `@prot.soap.*` annotations
- [X] T028 Execute full plugin test suite from `/home/zpc/works/study/sandbox/spring-labs/prot-cxf-codegen/prot-cxf-codegen-plugin/pom.xml` (`mvn test`) and record passing expectations for ping + calculator scenarios with kebab-case YAML input and prot.soap.* annotation assertions in `/home/zpc/works/study/sandbox/spring-labs/specs/002-cxf-client-gen-config/tasks.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup) → Phase 2 (Foundational)
- Phase 2 (Foundational) → enables all user stories
- Phase 3 (US1, P1) and Phase 4 (US4, P1) should complete before P2 stories to lock prot.soap.* annotation behavior and ping regression coverage
- Phase 5 (US2, P2) depends on Phase 3 template/class model changes
- Phase 6 (US3, P2) depends on Phase 2 loader model changes (kebab-case → camelCase mapping)
- Phase 7 (US5, P2) depends on Phase 3 + Phase 5 + Phase 6 (calculator harness validates final annotation + loading behavior with enhanced YAML)
- Final Phase depends on all user stories

### User Story Completion Order (Dependency Graph)

1. **US1 (P1)** → mandatory/optional class-level `@prot.soap.*` annotation generation from YAML `x-config-key`, `x-static-headers`, `x-dynamic-headers`
2. **US4 (P1)** → ping harness regression matrix aligned to enhanced kebab-case YAML with prot.soap.* assertions
3. **US2 (P2)** → mandatory per-operation `@prot.soap.SoapAction` logic using YAML `x-operations.<op>.action`
4. **US3 (P2)** → file/classpath load + validation guarantees for kebab-case YAML → camelCase Java mapping
5. **US5 (P2)** → calculator wsdl generation scenario expansion with kebab-case YAML and prot.soap.* assertions

---

## Parallel Execution Examples

### User Story 1

- T008 can run in parallel with T009 prep work (test authoring vs generator logic), then apply T010 template changes.

### User Story 4

- T011 and T012 can run in parallel (different resource files with kebab-case YAML), then T013/T014 assertions.

### User Story 3

- T018 and T019 can run in parallel (file and classpath tests with kebab-case YAML fixtures), then T020 and T021 error handling.

### User Story 5

- T022 and T023 can run in parallel (calculator fixtures with kebab-case YAML), then T024/T025 assertions, then T026 consolidation.

---

## Implementation Strategy

### MVP First

1. Complete Phase 1 and Phase 2 (canonical config model with kebab-case → camelCase mapping).
2. Deliver US1 (Phase 3): Class-level `@prot.soap.*` annotations.
3. Lock regressions with US4 ping matrix (Phase 4) using enhanced kebab-case YAML.
4. Validate MVP by running plugin tests for ping scenarios with prot.soap.* assertions.

### Incremental Delivery

1. Add US2 for method-level mandatory `@prot.soap.SoapAction` using YAML `x-operations`.
2. Add US3 for loader robustness and kebab-case key validation (reject `opertions` typo, guide to canonical `x-operations`).
3. Add US5 for calculator scenario coverage with enhanced YAML.
4. Finish with polish/docs/full test pass covering all naming conventions.

---

## Format Validation

All tasks use the required checklist format:
- Checkbox: `- [ ]`
- Sequential IDs: `T001` to `T028`
- `[P]` only on truly parallelizable tasks
- `[US#]` labels on user-story tasks only
- Every task description includes at least one explicit absolute file path
- Naming conventions explicitly reflected:
  - **YAML examples** use kebab-case with `x-` prefix (e.g., `x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations`)
  - **Java model** uses camelCase (e.g., `configKey`, `staticHeaders`, `dynamicHeaders`, `operations`)
  - **Annotations** use `prot.soap.*` namespace (e.g., `@prot.soap.RegisteredSoapClient`, `@prot.soap.StaticHeaders`, `@prot.soap.DynamicHeaders`, `@prot.soap.SoapAction`)

---

## Task Count Summary

- **Total Tasks**: 28
- **Setup Phase**: 2 tasks
- **Foundational Phase**: 5 tasks
- **User Story 1 (P1)**: 3 tasks
- **User Story 4 (P1)**: 4 tasks
- **User Story 2 (P2)**: 3 tasks
- **User Story 3 (P2)**: 4 tasks
- **User Story 5 (P2)**: 5 tasks
- **Polish Phase**: 2 tasks

**Parallel Opportunities**: 12 tasks marked `[P]` enabling concurrent execution within each phase.

**MVP Scope**: Complete Phases 1, 2, 3, and 4 (14 tasks) to deliver kebab-case YAML parsing with prot.soap.* class-level and method-level annotations on ping scenario.

---

## Key Naming Convention Validation

✅ **YAML Field Naming**: All YAML examples use kebab-case with `x-` prefix
- `x-config-key` (mapped to Java `configKey`)
- `x-static-headers` (mapped to Java `staticHeaders`)
- `x-dynamic-headers` (mapped to Java `dynamicHeaders`)
- `x-operations.<operationName>.action` (mapped to Java `operations`)

✅ **Java Model Naming**: All Java model fields use camelCase
- `configKey`, `staticHeaders`, `dynamicHeaders`, `operations`
- SnakeYAML PropertyNamingStrategy handles kebab-case → camelCase mapping

✅ **Annotation Namespace**: All annotations use `prot.soap.*`
- `@prot.soap.RegisteredSoapClient(resolvedConfigKey)`
- `@prot.soap.StaticHeaders(...)`
- `@prot.soap.DynamicHeaders(...)`
- `@prot.soap.SoapAction(resolvedActionName)`

