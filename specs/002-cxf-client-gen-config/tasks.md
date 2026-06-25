# Tasks: CXF SOAP Client Generation Config Plugin

**Input**: Design documents from `/specs/002-cxf-client-gen-config/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), data-model.md  
**Tests**: Comprehensive unit tests with code coverage validation (≥80%)  
**Organization**: Tasks grouped by user story to enable independent implementation and testing.

---

## Format: `[ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no interdependencies)
- **[Story?]**: User story label for story phase tasks (US1, US2, US3, US4, US5)
- **File paths**: All paths relative to `prot-cxf-codegen/prot-cxf-codegen-plugin` unless noted

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, dependencies, and basic test infrastructure

- [X] T001 Add SnakeYAML dependency (org.yaml:snakeyaml:2.2) to pom.xml
- [X] T002 Ensure JUnit 5, Mockito dependencies in pom.xml for test infrastructure
- [X] T003 [P] Create src/test/resources/backend/soap/ directory for WSDL fixtures
- [X] T004 [P] Copy ping.wsdl fixture to src/test/resources/backend/soap/ping.wsdl
- [X] T005 [P] Copy calculator.wsdl fixture to src/test/resources/backend/soap/calculator.wsdl
- [X] T006 [P] Create test config directory src/test/resources/config/
- [X] T007 Configure maven-surefire-plugin for test execution in pom.xml
- [X] T008 Configure JaCoCo maven plugin for code coverage reporting (goal: 80% threshold for prot.cxf.plugin)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core plugin classes and configuration infrastructure that MUST complete before user story implementation

**⚠️ CRITICAL**: All foundational tasks must complete before user story work begins

- [X] T009 Create OperationConfig class (src/main/java/prot/cxf/plugin/OperationConfig.java) with fields: action (String), staticHeaders (List<String>), dynamicHeaders (List<String>)
- [X] T010 Create ClientGenConfig class (src/main/java/prot/cxf/plugin/ClientGenConfig.java) with fields: configKey, baseUrl, jaxbContextPaths, staticHeaders, dynamicHeaders, operations (Map<String, OperationConfig>)
- [X] T011 Implement SnakeYAML type mapping configuration for ClientGenConfig to support kebab-case YAML keys mapping to camelCase Java properties
- [X] T012 Create ConfigLoader class (src/main/java/prot/cxf/plugin/ConfigLoader.java) with loadConfig(String path) method supporting file-first, classpath-fallback resolution
- [X] T013 Implement ConfigLoader error handling with descriptive exception messages for missing, malformed, or unresolvable YAML configs
- [X] T014 Update CustomSEIGenerator.java to detect --client-gen-config argument from CXF extraargs
- [X] T015 Update CustomSEIGenerator to load and validate ClientGenConfig using ConfigLoader in its constructor/initialize method
- [X] T016 Create template helper class (src/main/java/prot/cxf/plugin/AnnotationGenerator.java) for emitting annotation code strings
- [X] T017 Create src/test/resources/config/clientGenConfigA.yaml with top-level configKey, baseUrl, jaxbContextPaths, staticHeaders, dynamicHeaders, and operations (ping and echo)
- [X] T018 [P] Create src/test/resources/config/clientGenConfigB.yaml with different values than configA to validate config B scenarios
- [X] T019 Create test fixture generator utility (src/test/java/prot/cxf/plugin/TestConfigFactory.java) for programmatic config creation in unit tests

**Checkpoint**: Foundation complete - user story implementation can begin

---

## Phase 3: User Story 1 - Custom Annotation on Generated SEI Interface (Priority: P1) 🎯 MVP

**Goal**: Generate SEI interfaces with mandatory @prot.soap.SoapClient annotation containing configKey, baseUrl, jaxbContextPaths, staticHeaders, and dynamicHeaders

**Independent Test**: Run wsdl2java with clientGenConfig and verify generated SEI contains proper @prot.soap.SoapClient annotation with all fields rendered in correct formats (concatenated strings for static headers, Class<?> references for dynamic headers)

### Tests for User Story 1

> **NOTE: Tests written and verified to FAIL before implementation**

- [X] T020 [P] [US1] Create ClientGenConfigParsingTest (src/test/java/prot/cxf/plugin/ClientGenConfigParsingTest.java) to verify YAML deserialization for configKey, baseUrl, jaxbContextPaths
- [X] T021 [P] [US1] Create StaticHeadersParsingTest (src/test/java/prot/cxf/plugin/StaticHeadersParsingTest.java) to verify top-level staticHeaders parsed as concatenated "name=value" strings
- [X] T022 [P] [US1] Create DynamicHeadersParsingTest (src/test/java/prot/cxf/plugin/DynamicHeadersParsingTest.java) to verify top-level dynamicHeaders parsed as list of FQCN strings
- [X] T023 [P] [US1] Create ConfigLoaderTest (src/test/java/prot/cxf/plugin/ConfigLoaderTest.java) covering file-path resolution and classpath-fallback resolution with assertions for loaded config structure
- [X] T024 [P] [US1] Create ClientGenConfigValidationTest (src/test/java/prot/cxf/plugin/ClientGenConfigValidationTest.java) to verify configKey fallback to portType name when null/empty
- [X] T025 [US1] Create SEIAnnotationGenerationTest (src/test/java/prot/cxf/plugin/SEIAnnotationGenerationTest.java) to verify @prot.soap.SoapClient annotation emission with resolvedConfigKey and optional attributes

### Implementation for User Story 1

- [X] T026 [P] [US1] Implement OperationConfig.java getters/setters and validation logic for action and header fields
- [X] T027 [P] [US1] Implement ClientGenConfig.java getters/setters with resolvedConfigKey() method (fallback: portType name)
- [X] T028 [P] [US1] Configure SnakeYAML constructor in ClientGenConfig with PropertyUtils to handle kebab-case YAML key aliases (x-base-url, base-url, baseUrl → baseUrl)
- [X] T029 [P] [US1] Update Velocity template prot-cxf-sei.vm to emit @prot.soap.SoapClient class-level annotation with resolvedConfigKey as value attribute
- [X] T030 [US1] Update prot-cxf-sei.vm template to emit baseUrl attribute in @prot.soap.SoapClient only when ClientGenConfig.baseUrl is non-null and non-empty
- [X] T031 [US1] Update prot-cxf-sei.vm template to emit jaxbContextPaths attribute as array in @prot.soap.SoapClient only when non-empty list
- [X] T032 [US1] Update prot-cxf-sei.vm template to emit staticHeaders as array of concatenated strings (format: "X-Header=value") in @prot.soap.SoapClient only when configured
- [X] T033 [US1] Update prot-cxf-sei.vm template to emit dynamicHeaders as array of Class<?> references (format: com.example.HeaderProvider.class) with proper import statements in @prot.soap.SoapClient only when configured
- [X] T034 [US1] Implement CustomSEIGenerator.doGenerateSEI() to resolve configKey and pass to template context
- [X] T035 [US1] Implement AnnotationGenerator.emitSoapClientAnnotation() method to generate full @prot.soap.SoapClient code string based on ClientGenConfig

**Checkpoint**: US1 complete - SEI interfaces now include comprehensive @prot.soap.SoapClient annotations with all configured attributes

---

## Phase 4: User Story 2 - Custom Annotation Per Operation (Priority: P2)

**Goal**: Generate operation methods with @prot.soap.SoapAction (always) and conditional @prot.soap.SoapMethodHeader (when headers configured)

**Independent Test**: Run wsdl2java with YAML x-operations entries and verify each generated method includes @prot.soap.SoapAction with resolved action name and @prot.soap.SoapMethodHeader with concatenated strings for staticHeaders and Class<?> references for dynamicHeaders

### Tests for User Story 2

> **NOTE: Tests written and verified to FAIL before implementation**

- [X] T036 [P] [US2] Create OperationConfigParsingTest (src/test/java/prot/cxf/plugin/OperationConfigParsingTest.java) to verify x-operations deserialization with action, staticHeaders, dynamicHeaders fields
- [X] T037 [P] [US2] Create ActionResolutionTest (src/test/java/prot/cxf/plugin/ActionResolutionTest.java) to verify resolvedActionName: configured action → WSDL soapAction → operation name fallback
- [X] T038 [P] [US2] Create OperationStaticHeadersTest (src/test/java/prot/cxf/plugin/OperationStaticHeadersTest.java) to verify operation-level staticHeaders parsed as concatenated strings
- [X] T039 [P] [US2] Create OperationDynamicHeadersTest (src/test/java/prot/cxf/plugin/OperationDynamicHeadersTest.java) to verify operation-level dynamicHeaders parsed as FQCN list
- [X] T040 [US2] Create SoapActionAnnotationTest (src/test/java/prot/cxf/plugin/SoapActionAnnotationTest.java) to verify @prot.soap.SoapAction always emitted for every operation with correct resolved action name
- [X] T041 [US2] Create SoapMethodHeaderAnnotationTest (src/test/java/prot/cxf/plugin/SoapMethodHeaderAnnotationTest.java) to verify @prot.soap.SoapMethodHeader emitted only when headers non-empty, with correct formats

### Implementation for User Story 2

- [X] T042 [P] [US2] Implement OperationConfig.resolvedActionName() method with fallback logic (configured → WSDL soapAction → operation name)
- [X] T043 [P] [US2] Enhance ConfigLoader to validate operation names match WSDL binding operations with clear error messages for mismatches
- [X] T044 [US2] Update prot-cxf-sei.vm template to emit @prot.soap.SoapAction annotation on every generated method with resolvedActionName value
- [X] T045 [US2] Update prot-cxf-sei.vm template to emit @prot.soap.SoapMethodHeader annotation on operation methods only when operation-level staticHeaders or dynamicHeaders is non-empty
- [X] T046 [US2] Update prot-cxf-sei.vm template to emit @prot.soap.SoapMethodHeader.staticHeaders as array of concatenated strings (format: "X-Op=value")
- [X] T047 [US2] Update prot-cxf-sei.vm template to emit @prot.soap.SoapMethodHeader.dynamicHeaders as array of Class<?> references (format: com.example.OperationHeaderProvider.class) with imports
- [X] T048 [US2] Implement AnnotationGenerator.emitSoapActionAnnotation() method to generate @prot.soap.SoapAction code string
- [X] T049 [US2] Implement AnnotationGenerator.emitSoapMethodHeaderAnnotation() method for operation-level header annotations
- [X] T050 [US2] Update CustomSEIGenerator to resolve operation configs and pass per-method annotations to template

**Checkpoint**: US2 complete - operation methods now include @prot.soap.SoapAction and conditional @prot.soap.SoapMethodHeader with proper header formats

---

## Phase 5: User Story 3 - YAML Config Loading from File and Classpath (Priority: P2)

**Goal**: Transparently load config files from file system (first) or classpath (fallback) with clear error handling

**Independent Test**: Execute config loading with both file-path and classpath-based configs to verify both loading strategies work correctly with appropriate fallback behavior

### Tests for User Story 3

> **NOTE: Tests written and verified to FAIL before implementation**

- [X] T051 [P] [US3] Create ConfigLoaderFilePathTest (src/test/java/prot/cxf/plugin/ConfigLoaderFilePathTest.java) to verify file system loading when config file exists
- [X] T052 [P] [US3] Create ConfigLoaderClasspathFallbackTest (src/test/java/prot/cxf/plugin/ConfigLoaderClasspathFallbackTest.java) to verify classpath loading when file not found
- [X] T053 [P] [US3] Create ConfigLoaderErrorHandlingTest (src/test/java/prot/cxf/plugin/ConfigLoaderErrorHandlingTest.java) to verify graceful failures with descriptive messages for missing/malformed configs

### Implementation for User Story 3

- [X] T054 [P] [US3] Implement ConfigLoader.loadConfig(String path) to attempt File system resolution first using new File(path).exists()
- [X] T055 [P] [US3] Implement ConfigLoader.loadConfig() classpath fallback using Thread.currentThread().getContextClassLoader().getResourceAsStream()
- [X] T056 [US3] Implement ConfigLoader error handling with specific exception types for missing file, malformed YAML, and IO errors
- [X] T057 [US3] Create comprehensive error messages in ConfigLoader describing tried paths, attempted load strategies, and root cause of failures
- [X] T058 [US3] Update CustomSEIGenerator to handle ConfigLoader exceptions and provide clear plugin-level error output

**Checkpoint**: US3 complete - config files reliably loaded from both file system and classpath with predictable fallback behavior

---

## Phase 6: User Story 4 - Plugin-Level wsdl2java Test Matrix (Priority: P1)

**Goal**: Verify CXF wsdl2java generation behavior through plugin-level harness tests covering three distinct config scenarios and a no-config baseline

**Independent Test**: Run maven-plugin-testing-harness tests for all three config scenarios (configA, configB, no config) against ping.wsdl fixture and verify generated SEI matches expected annotations and formats

### Tests for User Story 4

> **NOTE: Comprehensive harness-driven tests covering multiple scenarios**

- [X] T059 [P] [US4] Create PluginHarnessTestBase (src/test/java/prot/cxf/plugin/PluginHarnessTestBase.java) with maven-plugin-testing-harness setup, WSDL fixture paths, and helper methods for assertion validation
- [X] T060 [P] [US4] Create PingWsdlNoConfigTest (src/test/java/prot/cxf/plugin/PingWsdlNoConfigTest.java) using harness to verify baseline: ping.wsdl without --client-gen-config generates SEI without custom annotations (backward compatibility)
- [X] T061 [P] [US4] Create PingWsdlWithConfigATest (src/test/java/prot/cxf/plugin/PingWsdlWithConfigATest.java) using harness to verify generated SEI includes configA annotations: @prot.soap.SoapClient with configKey, baseUrl, jaxbContextPaths, and operation methods with @prot.soap.SoapAction
- [X] T062 [P] [US4] Create PingWsdlWithConfigBTest (src/test/java/prot/cxf/plugin/PingWsdlWithConfigBTest.java) using harness to verify configB generates different annotations than configA, validating config override logic
- [X] T063 [US4] Create HeaderFormatValidationTest (src/test/java/prot/cxf/plugin/HeaderFormatValidationTest.java) to verify generated code contains staticHeaders as concatenated strings ("X-Header=value") and dynamicHeaders as Class<?> references (HeaderProvider.class)
- [X] T064 [US4] Create YamlAliasNormalizationTest (src/test/java/prot/cxf/plugin/YamlAliasNormalizationTest.java) to verify YAML aliases (baseUrl, base-url, x-base-url, staticHeaders, static-headers, x-static-headers, etc.) all correctly normalize to canonical Java fields
- [X] T065 [US4] Create CalculatorWsdlComplexScenariosTest (src/test/java/prot/cxf/plugin/CalculatorWsdlComplexScenariosTest.java) using harness with calculator.wsdl to verify multi-operation header overrides and complex inheritance/override scenarios
- [X] T066 [P] [US4] Create FallbackBehaviorTest (src/test/java/prot/cxf/plugin/FallbackBehaviorTest.java) to verify fallback logic: null configKey→portType name, null action→WSDL soapAction→operation name
- [X] T067 [US4] Add assertion helpers to PluginHarnessTestBase for verifying annotation presence/absence, field values, import statements, and header format correctness

### Implementation for User Story 4

- [X] T068 [P] [US4] Update prot-cxf-sei.vm Velocity template to support conditional annotation emission based on ClientGenConfig presence and field values
- [X] T069 [P] [US4] Add template context variables for configKey, resolvedConfigKey, baseUrl, jaxbContextPaths, staticHeaders (formatted), dynamicHeaders (formatted)
- [X] T070 [US4] Add template context variables for operation-level action, resolvedActionName, operation staticHeaders, operation dynamicHeaders
- [X] T071 [P] [US4] Implement template logic to emit concatenated static header strings (no spaces: "X-Header=value")
- [X] T072 [P] [US4] Implement template logic to emit dynamic headers as Class<?> references with required import statements
- [X] T073 [US4] Update CustomSEIGenerator to pass all necessary context variables to Velocity template
- [X] T074 [US4] Validate generated code compiles without errors for all three config scenarios
- [X] T075 [US4] Ensure all harness tests complete in <30 seconds (using in-process harness, not Maven Invoker)

**Checkpoint**: US4 complete - comprehensive plugin-level tests validate all config scenarios and annotation generation behavior

---

## Phase 7: User Story 5 - Mojo Unit Tests for Plugin Classes (Priority: P2)

**Goal**: Create isolated unit tests for ClientGenConfig, ConfigLoader, CustomSEIGenerator without requiring full Maven build invocation

**Independent Test**: Run `mvn test` in prot-cxf-codegen-plugin and verify all Mojo unit tests pass with comprehensive coverage of plugin class behavior

### Tests for User Story 5

> **NOTE: Isolated unit tests for plugin logic verification**

- [X] T076 [P] [US5] Create ClientGenConfigUnitTest (src/test/java/prot/cxf/plugin/ClientGenConfigUnitTest.java) for getters/setters, resolvedConfigKey() fallback logic, field initialization
- [X] T077 [P] [US5] Create OperationConfigUnitTest (src/test/java/prot/cxf/plugin/OperationConfigUnitTest.java) for resolvedActionName() fallback logic with mocked WSDL soapAction values
- [X] T078 [P] [US5] Create ConfigLoaderUnitTest (src/test/java/prot/cxf/plugin/ConfigLoaderUnitTest.java) with mocked file system and classpath scenarios, covering error paths
- [X] T079 [P] [US5] Create SnakeYAMLMappingTest (src/test/java/prot/cxf/plugin/SnakeYAMLMappingTest.java) to verify YAML key aliases correctly map to Java fields: x-base-url, base-url, baseUrl, baseurl all → baseUrl
- [X] T080 [P] [US5] Create OperationLevelHeaderAliasTest (src/test/java/prot/cxf/plugin/OperationLevelHeaderAliasTest.java) to verify operation-level header field aliases (x-static-headers, static-headers, staticHeaders, staticheaders → staticHeaders)
- [X] T081 [P] [US5] Create AnnotationGeneratorUnitTest (src/test/java/prot/cxf/plugin/AnnotationGeneratorUnitTest.java) to verify code string generation for @prot.soap.SoapClient, @prot.soap.SoapAction, @prot.soap.SoapMethodHeader
- [X] T082 [P] [US5] Create CustomSEIGeneratorUnitTest (src/test/java/prot/cxf/plugin/CustomSEIGeneratorUnitTest.java) with mocked CXF Option, WSDL model, and template rendering
- [X] T083 [P] [US5] Create StaticHeaderStringFormatTest (src/test/java/prot/cxf/plugin/StaticHeaderStringFormatTest.java) to verify static headers remain concatenated strings in annotations (no object parsing)
- [X] T084 [P] [US5] Create DynamicHeaderClassFormatTest (src/test/java/prot/cxf/plugin/DynamicHeaderClassFormatTest.java) to verify dynamic headers emitted as Class<?> references (e.g., HeaderProvider.class)
- [X] T085 [P] [US5] Create MalformedYamlTest (src/test/java/prot/cxf/plugin/MalformedYamlTest.java) to verify error handling for invalid YAML structure, missing required fields, type mismatches
- [X] T086 [P] [US5] Create EmptyConfigTest (src/test/java/prot/cxf/plugin/EmptyConfigTest.java) to verify behavior when config is empty or minimal (baseline/default behavior)

### Implementation for User Story 5

- [X] T087 [P] [US5] Implement client config equals() and toString() methods for test assertions
- [X] T088 [P] [US5] Implement operation config equals() and toString() methods for test assertions
- [X] T089 [US5] Add Mockito setup in test base classes for mocking CXF Option, WSDL Service, Binding, Operation objects
- [X] T090 [US5] Implement test utilities for creating sample ClientGenConfig and OperationConfig objects
- [X] T091 [US5] Add test assertion helpers for verifying annotation code string patterns (regex validation of emitted annotation code)

**Checkpoint**: US5 complete - isolated unit tests comprehensively validate plugin class logic

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Code coverage validation, documentation, and integration testing

- [X] T092 [P] Run JaCoCo code coverage analysis: `mvn clean test jacoco:report` in prot-cxf-codegen-plugin
- [X] T093 Validate code coverage report confirms ≥80% line coverage for prot.cxf.plugin.* classes (generate HTML report at target/site/jacoco/index.html)
- [X] T094 [P] Document test matrix in README.md or TESTING.md covering: scenario 1 (no config), scenario 2 (configA), scenario 3 (configB), scenario 4 (calculator.wsdl complex)
- [X] T095 [P] Document YAML alias support with examples for baseUrl, jaxbContextPaths, staticHeaders, dynamicHeaders (top-level and operation-level)
- [X] T096 [P] Update quickstart.md with final examples showing generated annotation output: @prot.soap.SoapClient with staticHeaders as strings, dynamicHeaders as Class<?> references
- [X] T097 Update prot-cxf-codegen-soap-client module to include example client-gen-config.yaml with proper header formats and operation configurations
- [X] T098 Run full integration test: `mvn clean install` in prot-cxf-codegen root and verify prot-cxf-codegen-soap-client tests pass unchanged
- [X] T099 Verify all tasks run in parallel within Phase 2 (foundational) using concurrent test execution: `mvn clean test -Dparallel=methods -DthreadCount=4`
- [X] T100 Create HEADER_FORMATS.md documenting static header concatenation format and dynamic header Class<?> reference format with examples
- [X] T101 [P] Add integration test scenario validation in prot-cxf-codegen-soap-client to verify generated clients with annotations are usable at runtime
- [X] T102 Final validation: confirm all unit tests pass in <30 seconds with `mvn clean test -q`
- [X] T103 [P] Document error scenarios and troubleshooting guide in TROUBLESHOOTING.md

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup ✓
    ↓
Phase 2: Foundational ✓ [CRITICAL - BLOCKS all stories]
    ↓ (all stories can start in parallel after this)
    ├─ Phase 3: US1 (P1) 🎯 MVP
    ├─ Phase 4: US2 (P2)
    ├─ Phase 5: US3 (P2)
    ├─ Phase 6: US4 (P1)
    └─ Phase 7: US5 (P2)
    ↓
Phase 8: Polish & Coverage Validation ✓
```

### Task Dependencies (Critical Path)

```
Setup Phase (T001-T008): Sequential
    ↓
Foundational Phase (T009-T019): Can parallelize T003-T008, T017-T019 (marked [P])
    ↓
User Stories (T020-T091): Complete independently after Foundational
    ├─ US1 (T020-T035): Tests first, then implementation
    ├─ US2 (T036-T050): Tests first, then implementation  
    ├─ US3 (T051-T058): Tests first, then implementation
    ├─ US4 (T059-T075): Harness tests all scenarios
    └─ US5 (T076-T091): Isolated unit tests
    ↓
Polish Phase (T092-T103): Coverage validation and documentation
```

### User Story Independence

- **US1 (MVP)**: Deliverable on its own - class-level annotations only
- **US2**: Independent - operation-level annotations, builds on US1 but testable alone
- **US3**: Independent - config loading strategy, orthogonal to others
- **US4**: Comprehensive - validates all three stories through harness integration
- **US5**: Independent - unit tests for internal plugin logic

### Parallel Opportunities

**Within Phase 2 (Foundational)**:
- T003-T008: All marked [P] can run concurrently
- T017-T019: All marked [P] can run concurrently
- Serialize T009-T016 as they build on each other

**Within Phase 3 (US1)**:
- T020-T025: All marked [P] tests can run concurrently
- T026-T029: Models/parsing logic marked [P] can run concurrently
- Serialize T030-T035: Template and generator changes

**Within Phase 4 (US2)**:
- T036-T039: All marked [P] tests can run concurrently
- T042-T043: Marked [P] can run concurrently
- Serialize T040-T041, T044-T050: Template updates and generator enhancements

**Within Phase 6 (US4)**:
- T059-T062: All marked [P] harness test classes can run concurrently
- T066: Marked [P] can run with T059-T062
- Serialize T063-T065, T068-T074: Format validation and template work

**Within Phase 7 (US5)**:
- T076-T082: All marked [P] unit tests can run concurrently
- Serialize T083-T091: Format-specific and integration tests

**Across User Stories** (after Foundational):
- All 5 user stories (US1-US5) can be worked in parallel by different developers
- Recommended team allocation: 3-5 developers, one per story

---

## Parallel Execution Examples

### Example 1: Within Foundational Phase (T003-T008 concurrent)

```bash
# Terminal 1-4: Can start simultaneously after T001-T002
Task: T003 - Create test resource directories
Task: T004 - Copy WSDL ping fixture
Task: T005 - Copy WSDL calculator fixture  
Task: T006 - Create config test directory
Task: T007 - Configure maven-surefire-plugin
Task: T008 - Configure JaCoCo plugin
```

### Example 2: Within US1 Phase (T020-T025 concurrent)

```bash
# All test classes can be written in parallel before implementation
Task: T020 - ClientGenConfigParsingTest
Task: T021 - StaticHeadersParsingTest
Task: T022 - DynamicHeadersParsingTest
Task: T023 - ConfigLoaderTest
Task: T024 - ClientGenConfigValidationTest
# Then serialize:
Task: T025 - SEIAnnotationGenerationTest (depends on foundation tests)
```

### Example 3: Multi-Story Parallel (After Foundational Complete)

```bash
# Developer A starts US1
Task: T020-T035 (US1 - Custom SEI Annotations)

# Developer B starts US2
Task: T036-T050 (US2 - Operation Annotations)

# Developer C starts US3
Task: T051-T058 (US3 - Config Loading)

# Developer D starts US4
Task: T059-T075 (US4 - Harness Tests)

# Developer E starts US5
Task: T076-T091 (US5 - Unit Tests)

# All stories integrate in Phase 8 for coverage validation
```

---

## Implementation Strategy

### MVP First: User Story 1 Only

**Time Estimate**: 2-3 days for single developer

1. ✅ Complete Phase 1: Setup (T001-T008) - 1 day
2. ✅ Complete Phase 2: Foundational (T009-T019) - 1 day
3. ✅ Complete Phase 3: US1 (T020-T035) - 1-2 days
4. **STOP and VALIDATE**: Test US1 independently with `mvn test`
5. **Deploy/Demo**: Working MVP with class-level @prot.soap.SoapClient annotations

**At this point, users can generate SEI interfaces with standardized annotations - core value delivered!**

### Incremental Delivery: Adding Features

**Story 2** (1 day): Add operation-level @prot.soap.SoapAction and @prot.soap.SoapMethodHeader
**Story 3** (half day): Validate config loading from file and classpath
**Story 4** (1 day): Comprehensive harness test matrix
**Story 5** (half day): Unit test coverage validation
**Polish** (half day): Coverage reports and documentation

**Total Incremental**: ~4.5 days to full feature with all scenarios covered

### Parallel Team Strategy: 3-5 Developers

**Day 1**: Team completes Setup + Foundational (8 hours)
- 1-2 devs on Setup (T001-T008)
- 2-3 devs on Foundational, utilizing parallelization

**Day 2-3**: Stories in parallel
- Developer 1: US1 (T020-T035)
- Developer 2: US2 (T036-T050)
- Developer 3: US3 (T051-T058)
- Developer 4: US4 harness (T059-T075)
- Developer 5: US5 unit tests (T076-T091)

**Day 4**: Integration & Polish
- All devs: Verify stories integrate without conflicts
- Phase 8: Coverage validation, documentation

**Total Timeline**: 3-4 days with team

---

## Success Criteria Mapping

| Success Criterion | Validated By Tasks |
|------|------|
| SC-001: @prot.soap.SoapClient always included with resolvedConfigKey | T025, T031, T034, T035, T059-T062 |
| SC-002a: Static headers as concatenated strings | T021, T032, T037-T038, T063, T084 |
| SC-002b: Dynamic headers as Class<?> references | T022, T033, T039, T064, T085 |
| SC-003: Three harness tests pass (configA, configB, no config) | T060-T062 |
| SC-004: @prot.soap.SoapAction always with resolved action | T040, T044, T048, T062 |
| SC-004a: Operation-level @prot.soap.SoapMethodHeader with formats | T041, T047, T049, T063 |
| SC-005: File-path and classpath loading verified | T051-T052, T054-T055 |
| SC-006: Tests complete in <30 seconds | T102 (validation task) |
| SC-007: ≥80% code coverage for prot.cxf.plugin package | T092-T093 |
| SC-007a: YAML alias normalization tested | T079-T080, T083 |
| SC-007b: Header format transformation tested | T063-T064, T084-T085 |
| SC-008: No external Maven process spawned | T059-T075 (harness-based) |
| SC-009: YAML aliases correctly normalized | T079-T080, T083 |
| SC-010: Operation-level @prot.soap.SoapMethodHeader conditional | T041, T047, T066 |
| SC-011: Soap-client module tests unchanged | T098 |
| SC-012: Code coverage reports ≥80% | T092-T093 |
| SC-013: Four test scenarios covered | T060-T062, T063, T065 |
| SC-014: Operation-level header aliases recognized | T062, T080 |

---

## Test Verification Matrix

| Scenario | WSDL | Config | Expected Outcome | Task |
|----------|------|--------|------------------|------|
| **Baseline** | ping | None | No custom annotations, backward compatible | T060 |
| **Config A** | ping | configA | @prot.soap.SoapClient with configA values, @prot.soap.SoapAction on methods | T061 |
| **Config B** | ping | configB | @prot.soap.SoapClient with configB values (different from A), validates override | T062 |
| **Complex** | calculator | multi-op config | Multiple operations with different headers, fallback behaviors validated | T065 |
| **Format: Static** | any | with static headers | Headers as concatenated strings "X-Y=value" | T063 |
| **Format: Dynamic** | any | with dynamic headers | Headers as Class<?> references HeaderProvider.class with imports | T063 |
| **Alias: baseUrl** | any | base-url or baseUrl key | Maps correctly to baseUrl Java field | T079 |
| **Alias: jaxbContextPaths** | any | jaxb-context-paths | Maps correctly to jaxbContextPaths Java field | T079 |
| **Alias: Op Headers** | any | static-headers or staticHeaders in x-operations | Maps correctly to operation staticHeaders field | T080 |
| **Fallback: ConfigKey** | any | null/empty x-config-key | Uses portType name as resolvedConfigKey | T024, T067 |
| **Fallback: Action** | any | null/empty action in x-operations | Uses WSDL soapAction, then operation name | T037, T067 |
| **File Loading** | any | file path config | Loads from file system when exists | T051, T054 |
| **Classpath Loading** | any | classpath config | Falls back to classpath when file not found | T052, T055 |
| **Error: Missing Config** | any | invalid path | Descriptive error message | T053, T057 |
| **Error: Malformed YAML** | any | invalid YAML syntax | Descriptive error message | T085, T057 |

---

## Code Coverage Goals

**Target**: ≥80% line coverage for `prot.cxf.plugin` package

**Coverage Breakdown** (estimated):
- `ClientGenConfig.java`: 95% (getters/setters, resolvedConfigKey logic)
- `OperationConfig.java`: 95% (getters/setters, resolvedActionName logic)
- `ConfigLoader.java`: 85% (file/classpath loading, error paths)
- `CustomSEIGenerator.java`: 80% (argument detection, config loading integration)
- `AnnotationGenerator.java`: 90% (code emission logic)

**Validation**: Run `mvn clean test jacoco:report` and verify report shows ≥80% coverage

---

## Notes

- ✅ All tasks follow strict checklist format: `- [ ] [ID] [P?] [Story?] Description`
- ✅ File paths are absolute or project-relative and exact
- ✅ Each user story is independently testable and deployable
- ✅ Tests written and verified to FAIL before implementation (TDD approach)
- ✅ Parallel tasks marked [P] have no file or data dependencies
- ✅ Fallback logic explicitly tested (configKey→portType, action→soapAction→opname)
- ✅ Header format transformations validated: strings→strings, FQCNs→Class<?> references
- ✅ YAML alias normalization verified for both top-level and operation-level fields
- ✅ Plugin-level harness tests use in-process execution (<30 seconds)
- ✅ Code coverage ≥80% required for prot.cxf.plugin package
- ✅ All tests pass in `mvn test` without external Maven invocation
- ✅ Backward compatibility verified: no-config scenario unchanged
- ✅ MVP achievable with US1 alone (configurable SEI annotations)
