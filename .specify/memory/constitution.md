<!--
Sync Impact Report:
- Version change: 0.0.0 → 1.0.0 (initial adoption)
- New principles: 5 (Extension Over Modification, JDK 21+ Compatibility, Java Naming Conventions, Test-First Exploration, Industry Best Practices)
- Added sections: Technical Standards, Development Workflow
- Templates requiring updates: ✅ plan-template.md (no changes needed - constitution gates are generic), ✅ spec-template.md (no changes needed), ✅ tasks-template.md (no changes needed - structure is generic)
-->

# Spring Labs Constitution

## Core Principles

### I. Extension Over Modification
All Spring framework exploration MUST use Spring's extension points (BeanPostProcessor, EnvironmentPostProcessor, BeanFactoryPostProcessor, ApplicationContextInitializer, etc.) rather than modifying Spring's source code. The goal is to understand and leverage Spring's framework, not fork or alter it.

**Rationale**: This ensures examples remain compatible with future Spring versions and demonstrates deep understanding of Spring's architecture.

### II. JDK 21+ Compatibility
All code MUST target JDK 21 or higher as the minimum runtime. Examples SHOULD leverage modern Java features including virtual threads (java.lang.Thread.Builder), structured concurrency (ScopedValue), pattern matching for switch, and sealed classes where appropriate.

**Rationale**: Modern Java LTS releases provide significant improvements in productivity and performance. Exploring these features is part of the learning objective.

### III. Java Naming Conventions
All code MUST follow Oracle's Java naming conventions:
- Class/Interface/Enum names: PascalCase (e.g., `MyBeanProcessor`)
- Methods and fields: camelCase (e.g., `processBean`)
- Constants: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
- Package names: lowercase with dots (e.g., `com.example.lifecycle`)

**Rationale**: Industry standard naming ensures readability and professional quality.

### IV. Test-First Exploration (NON-NEGOTIABLE)
TDD mandatory: Each exploration example MUST have failing tests written first to define the expected behavior, then implementation to make tests pass. Red-Green-Refactor cycle strictly enforced.

**Rationale**: Tests document the expected behavior and ensure examples work as documented. They also serve as learning verification.

### V. Industry Best Practices
Follow Spring best practices and industry standards:
- Constructor injection over field or setter injection
- Immutable objects where possible (use final fields, Builder pattern when needed)
- Proper exception handling (no empty catch blocks, specific exception types)
- Structured logging with appropriate log levels
- Comprehensive Javadoc documentation for all public APIs

**Rationale**: Examples should demonstrate production-quality code, not just "working" code.

## Technical Standards

All exploration modules MUST adhere to:

- **Build Tool**: Maven or Gradle (consistent within each module)
- **Package Naming**: Reverse domain + project identifier (e.g., `com.example.lifecycle`)
- **Documentation**: Each module MUST have a README.md with usage instructions
- **Runnable Examples**: Each feature MUST include a runnable Main class demonstrating the concept
- **Test Coverage**: Aim for >80% coverage on core functionality

## Development Workflow

- **Code Review**: All PRs require review before merge
- **Testing**: All features require passing tests before merge
- **Versioning**: Follow Semantic Versioning (MAJOR.MINOR.PATCH)
- **Complexity Justification**: Architectural complexity must be justified in documentation
- **Runtime Guidance**: Use `README.md` for runtime development guidance

## Governance

This Constitution supersedes all other practices. Amendments require:

1. Documentation of the proposed change
2. Approval through code review
3. Migration plan if backward compatibility is affected
4. Version bump following semantic versioning rules

**Version**: 1.0.0 | **Ratified**: 2026-04-12 | **Last Amended**: 2026-04-12
