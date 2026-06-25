# Clarification Summary: 002-cxf-client-gen-config Enhancement

**Date**: 2026-04-22  
**Feature**: CXF SOAP Client Generation Config Plugin (002-cxf-client-gen-config)  
**Status**: 5 Clarifications Encoded ✓

---

## Summary of Changes

The 002-cxf-client-gen-config feature specification has been enhanced with 5 critical clarifications that address operation-level header configuration, header format transformations, code coverage requirements, and comprehensive test verification scenarios.

### Files Updated

1. **`/home/zpc/works/study/sandbox/spring-labs/specs/002-cxf-client-gen-config/spec.md`**
   - Added new clarification session (Session 2026-04-22 Enhancement Round II)
   - Enhanced Functional Requirements (FR-017 through FR-028)
   - Extended Success Criteria (SC-012 through SC-014)

2. **`/home/zpc/works/study/sandbox/spring-labs/specs/002-cxf-client-gen-config/data-model.md`**
   - Updated `ClientGenConfig` class model with string-based static headers
   - Removed `StaticHeader` class (deprecated in favor of concatenated strings)
   - Updated `OperationConfig` to use string-based headers
   - Added "Generated Annotation Examples" section with concrete code samples
   - Enhanced Validation Rules with operation-level header alias support
   - Added generated annotation format specifications

---

## Clarification Details

### 1. Operation-Level Header Alias Support (FR-018)

**Requirement**: Enhanced OperationConfig YAML to accept multiple variants for header field names:

```yaml
x-operations:
  myOperation:
    # All of these aliases are now supported and map to staticHeaders:
    x-static-headers: ["X-Header=value"]
    static-headers: ["X-Header=value"]
    staticHeaders: ["X-Header=value"]
    staticheaders: ["X-Header=value"]
    
    # All of these aliases are now supported and map to dynamicHeaders:
    x-dynamic-headers: ["com.example.HeaderProvider"]
    dynamic-headers: ["com.example.HeaderProvider"]
    dynamicHeaders: ["com.example.HeaderProvider"]
    dynamicheaders: ["com.example.HeaderProvider"]
```

**Validation Rule**: `x-static-headers|static-headers|staticHeaders|staticheaders` → `staticHeaders`; `x-dynamic-headers|dynamic-headers|dynamicHeaders|dynamicheaders` → `dynamicHeaders`

**Coverage**: FR-018, SC-014

---

### 2. Dynamic Headers as Class Types (FR-017d)

**Requirement**: Dynamic headers in both `@prot.soap.SoapClient` and `@prot.soap.SoapMethodHeader` annotations MUST be emitted as `Class<?>.class` format with proper imports.

**YAML Input** (class-level):
```yaml
x-dynamic-headers:
  - com.example.soap.header.AuthHeaderProvider
  - com.example.soap.header.LocaleHeaderProvider
```

**Generated Annotation**:
```java
@prot.soap.SoapClient(
    value = "pingClient",
    dynamicHeaders = {
        com.example.soap.header.AuthHeaderProvider.class,
        com.example.soap.header.LocaleHeaderProvider.class
    }
)
```

**Operation-Level Application**:
```java
@prot.soap.SoapMethodHeader(
    dynamicHeaders = {com.example.soap.header.OpHeaderProvider.class}
)
```

**Coverage**: FR-017c, FR-017d, SC-002b, SC-004a

---

### 3. Static Headers as Concatenated Strings (FR-017a, FR-017b)

**Requirement**: Static headers in both YAML input and generated annotations MUST use concatenated `"name=value"` string format instead of structured objects.

**YAML Input** (class-level and operation-level):
```yaml
x-static-headers:
  - "X-Tenant=demo"
  - "X-Trace-Enabled=true"

x-operations:
  ping:
    static-headers:
      - "X-Op=opA"
      - "X-Priority=high"
```

**Generated Annotation**:
```java
@prot.soap.SoapClient(
    value = "pingClient",
    staticHeaders = {"X-Tenant=demo", "X-Trace-Enabled=true"}
)

// And at operation level:
@prot.soap.SoapMethodHeader(
    staticHeaders = {"X-Op=opA", "X-Priority=high"}
)
```

**Data Model Update**: 
- `ClientGenConfig.staticHeaders` changed from `List<StaticHeader>` to `List<String>`
- `OperationConfig.staticHeaders` changed from `List<StaticHeader>` to `List<String>`
- Removed `StaticHeader` class entirely from new format

**Coverage**: FR-017a, FR-017b, SC-002a, SC-004a

---

### 4. Code Coverage Requirements (FR-027)

**Requirement**: Unit test coverage for `prot.cxf.plugin.*` classes MUST reach minimum **80% line coverage**; all tests MUST pass with code coverage reports generated.

**Success Criteria**:
- **SC-012**: All unit tests pass with code coverage reports confirming ≥80% line coverage for prot.cxf.plugin package.
- Coverage reports must be generated alongside test execution.
- No tests may be skipped or excluded from coverage analysis.

**Coverage**: FR-027, SC-012

---

### 5. Test Verification Matrix (FR-028)

**Requirement**: Test scenarios MUST cover comprehensive matrix of WSDL files and configuration variations.

**Four Distinct Test Scenarios**:

1. **Scenario A - No Config**
   - WSDL: `ping.wsdl`
   - Config: None
   - Expected: No custom annotations emitted (backward-compatible baseline)
   - Tests: Verify no `@prot.soap.SoapClient`, only default CXF behavior

2. **Scenario B - Basic Config**
   - WSDL: `ping.wsdl`
   - Config: Standard configuration with class-level and operation-level settings
   - Expected: Basic annotations with configKey, baseUrl, headers
   - Tests: Verify `@prot.soap.SoapClient`, `@prot.soap.SoapAction`, `@prot.soap.SoapMethodHeader`

3. **Scenario C - Complex Multi-Operation Config**
   - WSDL: `calculator.wsdl`
   - Config: Multiple operations with different header combinations and override variations
   - Expected: Complex header scenarios with operation-level overrides
   - Tests: Verify per-operation differentiation, header override behavior

4. **Scenario D - Header Format Validation**
   - YAML: Various header format inputs (concatenated strings, FQCN lists)
   - Config: Specific test cases for format transformation
   - Expected: Correct transformation from YAML to generated annotation code
   - Tests: String concatenation validation, Class type resolution, import generation

**Coverage**: FR-028, SC-013

---

## New Functional Requirements (FR-018 through FR-028)

| Requirement | Description |
|-------------|-------------|
| **FR-018** | Operation-level YAML header field aliases support |
| **FR-019** | Annotation namespace specification (prot.soap.*) |
| **FR-020-026** | Plugin-level testing framework requirements |
| **FR-027** | Unit test code coverage: minimum 80% for prot.cxf.plugin.* |
| **FR-028** | Test scenario matrix: 4 comprehensive WSDL/config combinations |

---

## New Success Criteria (SC-012 through SC-014)

| Criterion | Description |
|-----------|-------------|
| **SC-012** | Code coverage reports confirming ≥80% line coverage |
| **SC-013** | Test matrix covering 4 scenarios (no-config, basic, complex, format validation) |
| **SC-014** | Operation-level header field aliases recognized and normalized |

---

## Data Model Changes

### Class Fields Refactored

**Before**:
```java
private List<StaticHeader> staticHeaders;  // Objects with name, value, ifExisting

public class StaticHeader {
    private String name;
    private String value;
    private Boolean ifExisting;
}
```

**After**:
```java
private List<String> staticHeaders;  // Simple concatenated strings "name=value"

// StaticHeader class deprecated and removed
```

### YAML Examples Updated

**Before** (object-based):
```yaml
x-static-headers:
  - name: X-Tenant
    value: demo
    ifExisting: true
```

**After** (string-based):
```yaml
x-static-headers:
  - "X-Tenant=demo"
  - "X-Trace-Enabled=true"
```

### Annotation Generation Examples Added

New section "Generated Annotation Examples" provides concrete code samples showing:
1. Class-level annotations with full configuration
2. Method-level annotations with operation-specific settings
3. Proper import statements for Class<?> references
4. Fallback annotation behavior

---

## Validation Rules Enhancements

### Operation-Level Header Aliases

New validation rule:
```
Operation-level header aliases: 
  x-static-headers|static-headers|staticHeaders|staticheaders → staticHeaders
  x-dynamic-headers|dynamic-headers|dynamicHeaders|dynamicheaders → dynamicHeaders
```

### Header Format (Generated)

New validation rules:
```
Static header format (generated): 
  Array of concatenated strings (e.g., staticHeaders = {"X-Calc-Op=minus", "X-Overflow=false"})

Dynamic header format (generated): 
  Array of Class<?> references with imports (e.g., dynamicHeaders = {prot.soap.header.SoapHeaderA.class, prot.soap.header.SoapHeaderB.class})
```

---

## Impact Assessment

### Backward Compatibility
- **No-config scenario**: Remains backward-compatible (FR-026, SC-011)
- **Existing tests**: Incrementally updated, not rewritten
- **Data model**: Internal model may retain both formats for transition period

### Implementation Scope
- **Plugin classes**: `ClientGenConfig`, `OperationConfig`, `ConfigLoader`, `CustomSEIGenerator`
- **Test coverage**: 80% minimum across prot.cxf.plugin package
- **Test scenarios**: 4 comprehensive combinations covering all critical paths

### Testing Requirements
- No external Maven process spawning (in-JVM harness only)
- All tests under 30 seconds on standard dev machine
- Code coverage reports mandatory
- Downstream module (`prot-cxf-codegen-soap-client`) compatibility verified

---

## File Locations

**Spec**: `/home/zpc/works/study/sandbox/spring-labs/specs/002-cxf-client-gen-config/spec.md`
**Data Model**: `/home/zpc/works/study/sandbox/spring-labs/specs/002-cxf-client-gen-config/data-model.md`

**Total Changes**:
- spec.md: 49 lines added, 13 lines modified
- data-model.md: 56 lines added, 24 lines modified

---

## Next Steps

1. ✅ Clarifications encoded in spec.md and data-model.md
2. → Implement enhanced `ClientGenConfig` YAML parsing with alias support
3. → Refactor static headers from objects to strings
4. → Update annotation generation to use Class<?> format for dynamic headers
5. → Create comprehensive test matrix covering all 4 scenarios
6. → Configure code coverage reporting (>80% target)
7. → Run `/speckit.plan` to generate detailed implementation plan

---

## Summary Table

| Clarification | Type | Requirement | Coverage |
|---------------|------|-------------|----------|
| 1. Operation-level header alias support | YAML | Accept 4+ variants per field | FR-018, SC-014 |
| 2. Dynamic headers as Class types | Annotation | Class<?>.class format | FR-017d, SC-002b, SC-004a |
| 3. Static headers as concatenated strings | YAML & Annotation | "name=value" format | FR-017a, FR-017b, SC-002a |
| 4. Code coverage threshold | Testing | ≥80% line coverage for prot.cxf.plugin.* | FR-027, SC-012 |
| 5. Test verification matrix | Testing | 4 scenarios: no-config, basic, complex, format | FR-028, SC-013 |


