---

description: "Task list for Spring Bean Lifecycle Demo feature implementation"
---

# Tasks: Spring Bean Lifecycle Demo

**Input**: Design documents from `/specs/001-bean-lifecycle-demo/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md

**Tests**: Tests ARE included per Constitution Principle IV (Test-First Exploration). Tests verify lifecycle behavior.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create Maven project structure at repository root `z-spring-bean-lifecycle/`
- [x] T002 [P] Create pom.xml with Spring Framework 6.x and JDK 21 configuration
- [x] T003 [P] Create base directory structure: src/main/java/com/example/lifecycle/, src/main/resources/
- [x] T004 [P] Create test directory structure: src/test/java/com/example/lifecycle/
- [x] T032 [P] Create build.gradle to support both Gradle and Maven build systems

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T005 Create applicationContext.xml in src/main/resources/ with XML namespace and annotation-config
- [x] T006 Create DependentBeanLifecycleLog utility class in src/main/java/com/example/lifecycle/ (uses getClass().getSimpleName() for log prefix)
- [x] T011 [P] [US1] Create LifecycleBean in src/main/java/com/example/lifecycle/bean/LifecycleBean.java (uses DependentBeanLifecycleLog.log with getClass())
- [x] T012 [P] [US1] Create ServiceBean in src/main/java/com/example/lifecycle/bean/ServiceBean.java (uses DependentBeanLifecycleLog.log with getClass())
- [x] T013 [P] [US1] Create ControllerBean in src/main/java/com/example/lifecycle/bean/ControllerBean.java (uses DependentBeanLifecycleLog.log with getClass(), depends on ServiceBean)
- [x] T014 [US1] Update applicationContext.xml to define LifecycleBean, ServiceBean, ControllerBean with init-method and destroy-method
- [x] T015 [US1] Create Main.java in src/main/java/com/example/lifecycle/Main.java to start context and display lifecycle events
- [x] T016 [US1] Verify tests pass (T009, T010) - lifecycle callbacks execute in correct order

**Checkpoint**: User Story 1 complete - lifecycle demo works

---

## Phase 4: User Story 2 - Understand Aware Interface Callbacks (Priority: P2)

**Goal**: Verify Aware interfaces are called in correct order

**Independent Test**: Run and verify console shows BeanNameAware → BeanFactoryAware → ApplicationContextAware in order

### Tests for User Story 2

- [x] T017 [P] [US2] Write test in src/test/java/com/example/lifecycle/bean/AwareCallbackOrderTest.java to verify Aware interface order

### Implementation for User Story 2

- [x] T018 [US2] Verify LifecycleBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware
- [x] T019 [US2] Add logging to verify Aware callbacks happen after dependency injection but before @PostConstruct
- [x] T020 [US2] Verify test passes (T017) - Aware callbacks in correct order

**Checkpoint**: User Story 2 complete - Aware interface order verified

---

## Phase 5: User Story 3 - Observe Bean Destruction Sequence (Priority: P3)

**Goal**: Demonstrate complete destruction lifecycle

**Independent Test**: Register shutdown hook and verify destruction callbacks on context close

### Tests for User Story 3

- [x] T021 [P] [US3] Write test in src/test/java/com/example/lifecycle/bean/BeanDestructionOrderTest.java to verify destruction callback order

### Implementation for User Story 3

- [x] T022 [US3] Add @PreDestroy method to LifecycleBean, ServiceBean, ControllerBean
- [x] T023 [US3] Implement DisposableBean.destroy() in all beans
- [x] T024 [US3] Add custom destroy-method to all beans defined in XML
- [x] T025 [US3] Register shutdown hook in Main.java and verify destruction order: @PreDestroy → DisposableBean.destroy → custom destroy-method
- [x] T026 [US3] Verify test passes (T021) - destruction callbacks in correct order

**Checkpoint**: User Stories 1, 2, and 3 all complete

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

### Tests for Polish Phase

- [x] T027 [P] Write test in src/test/java/com/example/lifecycle/LifecycleIntegrationTest.java for end-to-end verification of both beans

### Implementation for Polish Phase

- [x] T028 [P] Add comprehensive logging with lifecycle phase labels
- [x] T029 [P] Update README.md in z-spring-bean-lifecycle/ with usage instructions
- [x] T030 Add verification that DependentBeanLifecycleLog contains correct order for both ControllerBean and ServiceBean
- [x] T031 Verify all tests pass (T009, T010, T017, T021, T027)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Depends on US1 completion
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Depends on US1 and US2 completion

### Within Each User Story

- Models before services
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- Models within a story marked [P] can run in parallel

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test lifecycle output independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Tests are included per Constitution Principle IV (Test-First Exploration)
- Tests verify lifecycle callback order and are run before implementation
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
