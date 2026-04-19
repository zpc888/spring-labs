# Implementation Plan: CXF SOAP Client Generation Config Plugin

**Branch**: `002-cxf-client-gen-config` | **Date**: 2026-04-19 | **Spec**: `specs/002-cxf-client-gen-config/spec.md`
**Input**: Feature specification from `/specs/002-cxf-client-gen-config/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Extend the CXF wsdl2java tool with a `--client-gen-config` argument that reads a YAML configuration file (from file path or classpath) containing custom annotations to inject into the generated SEI interface and its method operations. Include a Maven Invoker Plugin test to verify the integration works correctly.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Apache CXF 4.2.0, SnakeYAML (for YAML parsing), Maven Invoker Plugin  
**Storage**: N/A (code generation)  
**Testing**: Maven Invoker Plugin for integration tests  
**Target Platform**: Java 21 runtime  
**Project Type**: Maven plugin extension / code generator  
**Performance Goals**: N/A (code generation, speed depends on WSDL complexity)  
**Constraints**: Must integrate with existing CXF codegen plugin flow  
**Scale/Scope**: Single module extension (~3-5 new files)

### Technical Decisions

| Decision | Rationale | Alternatives |
|----------|----------|--------------|
| YAML config approach | User requirement - hierarchical with sei/operations sections | XML binding file, JSON |
| File + classpath loading | User requirement - auto-detect file then classpath | File-only or classpath-only |
| Fully qualified annotation names | User requirement for simplicity | Short names with package mapping |
| Maven Invoker Plugin | User requirement - standard for Maven plugin testing | Direct Maven execution, exec:java |

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Extension Over Modification | ✅ PASS | Extending existing CustomSEIGenerator, not modifying CXF |
| II. JDK 21+ Compatibility | ✅ PASS | Target Java 21 |
| III. Java Naming Conventions | ✅ PASS | Follow Java conventions |
| IV. Test-First Exploration | ⚠️ NOTE | Feature is code generation - tests verify generated output |
| V. Industry Best Practices | ✅ PASS | Following CXF patterns |

## Project Structure

### Documentation (this feature)

```text
specs/002-cxf-client-gen-config/
├── plan.md              # This file
├── research.md          # Phase 0 output (built-in to this plan)
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (if needed)
└── tasks.md            # Phase 2 output
```

### Source Code (repository root)

```text
prot-cxf-codegen/
├── prot-cxf-codegen-plugin/
│   └── src/
│       └── main/
│           ├── java/prot/cxf/plugin/
│           │   ├── CustomSEIGenerator.java    # Extended
│           │   ├── ClientGenConfig.java       # NEW - YAML config model
│           │   └── ConfigLoader.java       # NEW - File/classpath loader
│           └── resources/
│               └── client-gen-config.yaml  # Example config
├── prot-cxf-codegen-soap-client/
│   └── src/
│       ├── main/
│       │   └── resources/
│       │       └── client-gen-config.yaml    # Example config
│       └── test/
│           └── java/...                  # NEW - Invoker test
```

**Structure Decision**: Extend existing `prot-cxf-codegen-plugin` module with new Java classes. Add tests to `prot-cxf-codegen-soap-client` module.

## Phase 0: Research & Technical Design

### Implementation Approach

#### How CXF Command-Line Arguments Work

Based on research of CXF source code:
1. CXF uses `Option.java` class with `extraargs` list for custom arguments
2. Custom generators can access these via `getExtraargs()` on the Option object
3. The `--` prefix in command line becomes a custom argument passed through

#### Adding Custom Arguments to wsdl2java

```xml
<!-- In pom.xml, use extraargs to pass custom config -->
<extraarg>-client-gen-config</extraarg>
<extraarg>path/to/config.yaml</extraarg>
```

Or command-line:
```bash
wsdl2java -client-gen-config path/to/config.yaml ...
```

#### Implementation Components

1. **ClientGenConfig** - Java class to model YAML structure:
   ```yaml
   sei:
     annotations:
       - fully.qualified.Annotation1
       - fully.qualified.Annotation2
   operations:
     operationName:
       - fully.qualified.OperationAnnotation
   ```

2. **ConfigLoader** - Loads YAML from file path or classpath:
   - Try file system first using `new File(path).exists()`
   - Fall back to classpath using `getResourceAsStream(path)`

3. **CustomSEIGenerator** modification:
   - Read `--client-gen-config` from extraargs
   - Parse YAML with SnakeYAML
   - Apply annotations to SEI class and methods

## Phase 1: Design Details

### Key Classes

| Class | Responsibility |
|------|--------------|
| `ClientGenConfig` | POJO model for YAML config deserialization |
| `ConfigLoader` | Load config from file or classpath, handle errors |
| `CustomSEIGenerator` | Extended to read config and apply annotations |
| `AnnotationAppender` | Utility to build annotation strings |

### Data Model

```java
// ClientGenConfig.java
public class ClientGenConfig {
    private List<String> seiAnnotations;
    private Map<String, List<String>> operations;
    // getters/setters
}
```

### Dependencies Added

| Dependency | Version | Purpose |
|------------|---------|---------|
| `org.yaml:snakeyaml` | 2.2 | YAML parsing |

## Complexity Tracking

| Why Needed | Simpler Alternative Rejected Because |
|-----------|--------------------------------|
| YAML config | User explicitly requested YAML format |
| File + classpath loading | User requirement - both modes needed |
| Maven Invoker test | Standard for Maven plugin verification |

---

## Next Steps

1. Run `/speckit.tasks` to generate tasks.md with implementation tasks
2. Implement the feature following the task list
3. Run `/speckit.analyze` for cross-artifact validation

## Extension Hooks

No post-plan hooks registered (optional hooks skipped).