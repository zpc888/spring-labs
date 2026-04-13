# Feature Specification: Spring Bean Lifecycle Demo

**Feature Branch**: `001-bean-lifecycle-demo`  
**Created**: 2026-04-12  
**Status**: Draft  
**Input**: User description: "I want to add a z-spring-bean-lifecycle project to demo a bean in spring lifecycle: instance created -> dependencies injected -> BeanNameAware interface -> Other Awares interfaces -> ApplicationContextAware interface -> BeanPostProcessor beforeInit -> PostContrust annotation method -> InitializingBean interface -> init method from xml definition -> BeanPostProcess afterInit --> Bean is ready to call now -> PreDestroy annotation method -> DispableBean interface -> destroy method from xml definition, please use spring framework, not spring boot, and use xml to define the bean, not java configuration nor component scan, use jdk 21 and the latest spring framework 6.x"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Complete Bean Initialization Lifecycle (Priority: P1)

As a developer learning Spring framework, I want to observe the complete initialization sequence of a Spring bean so that I can understand when each callback and interface is invoked.

**Why this priority**: Understanding bean lifecycle is fundamental to Spring development. This is the core educational value of the project.

**Independent Test**: Can be verified by running the application and observing console output showing each lifecycle phase in order.

**Acceptance Scenarios**:

1. **Given** a Spring application context configured with XML, **When** the context is started, **Then** all initialization callbacks must be invoked in the correct order: instance creation → dependency injection → Aware interfaces → BeanPostProcessor.beforeInit → @PostConstruct → InitializingBean.afterPropertiesSet → custom init-method → BeanPostProcessor.afterInit
2. **Given** a Spring application context, **When** the context is shut down, **Then** all destruction callbacks must be invoked in order: @PreDestroy → DisposableBean.destroy → custom destroy-method

---

### User Story 2 - Understand Aware Interface Callbacks (Priority: P2)

As a developer, I want to see which Aware interfaces are invoked and in what order so that I can use them properly in my own applications.

**Why this priority**: Aware interfaces provide access to Spring container internals. Understanding their order is crucial for proper usage.

**Independent Test**: Can be verified by checking console output shows BeanNameAware, BeanFactoryAware, ApplicationContextAware in correct sequence.

**Acceptance Scenarios**:

1. **Given** a bean implementing multiple Aware interfaces, **When** the bean is initialized, **Then** the output must show BeanNameAware → BeanFactoryAware → ApplicationContextAware order
2. **Given** a bean with injected dependencies, **When** Aware callbacks are invoked, **Then** they must be invoked AFTER dependency injection but BEFORE PostConstruct

---

### User Story 3 - Observe Bean Destruction Sequence (Priority: P3)

As a developer, I want to observe the complete bean destruction sequence so that I can properly implement cleanup logic in my applications.

**Why this priority**: Proper cleanup is essential for resource management in production applications.

**Independent Test**: Can be verified by closing the application context and observing destruction callbacks in console output.

**Acceptance Scenarios**:

1. **Given** a Spring application context with registered shutdown hook, **When** the JVM is terminated, **Then** destruction callbacks must execute in order: @PreDestroy → DisposableBean.destroy → custom destroy-method
2. **Given** a bean with scoped dependencies, **When** destruction occurs, **Then** destruction must happen in correct reverse order of initialization

---

### Edge Cases

- What happens when an exception occurs during initialization? How does Spring handle and report failures?
- What if a bean implements both InitializingBean and has a custom init-method defined in XML?
- How does BeanPostProcessor behave with prototype-scoped beans?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The project MUST demonstrate complete Spring bean initialization lifecycle with all standard callbacks
- **FR-002**: The project MUST use XML configuration exclusively (no @Configuration, @ComponentScan, or annotation-based bean definitions)
- **FR-003**: The project MUST use Spring Framework 6.x (not Spring Boot) with JDK 21
- **FR-004**: The project MUST include a runnable main class that starts the application context and displays lifecycle events
- **FR-005**: Each lifecycle callback MUST be clearly labeled in output to show execution order
- **FR-006**: The project MUST include a BeanPostProcessor implementation to demonstrate its role in the lifecycle
- **FR-007**: The project MUST demonstrate both initialization and destruction phases

## Clarifications

### Session 2026-04-12

- Q: Implementation details in specification → A: Remove implementation details from spec (keep spec as high-level requirements; planning phase will define specifics)

## Key Entities *(include if feature involves data)*

- **LifecycleBean**: A sample bean that implements all lifecycle callbacks to demonstrate the full Spring bean lifecycle
- **ServiceBean**: A service bean with lifecycle callbacks, dependency of ControllerBean
- **ControllerBean**: A controller bean that depends on ServiceBean, implements all lifecycle callbacks
- **DependentBeanLifecycleLog**: Utility class that logs lifecycle events to both console and static List
- **CustomBeanPostProcessor**: Custom BeanPostProcessor to show pre/post initialization processing
- **ApplicationConfig**: XML configuration file defining all beans

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Running the main class displays at least 12 distinct lifecycle callback invocations in correct order
- **SC-002**: All lifecycle phases are visible in console output with clear labels
- **SC-003**: The example compiles and runs without errors on JDK 21 with Spring Framework 6.x
- **SC-004**: Console output matches the documented Spring bean lifecycle specification

## Assumptions

- Users have basic knowledge of Java and are familiar with IDE usage
- Users can run Maven or Gradle build from command line
- The target environment has JDK 21 installed
- Users understand the difference between Spring Framework and Spring Boot
