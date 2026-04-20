# Data Model: CXF Client Gen Config

## ClientGenConfig

Root YAML model for generation-time annotation rules.

**YAML Field Naming**: Kebab-case (e.g., `x-config-key`, `x-static-headers`)  
**Java Model Naming**: camelCase (e.g., `configKey`, `staticHeaders`)  
**Mapping**: SnakeYAML automatically maps kebab-case YAML fields to camelCase Java properties.

```java
package prot.cxf.plugin;

import java.util.List;
import java.util.Map;

public class ClientGenConfig {
    /** Optional explicit client key; fallback is portType name; YAML field: x-config-key */
    private String configKey;

    /** Optional static headers rendered into @prot.soap.StaticHeaders; YAML field: x-static-headers */
    private List<StaticHeader> staticHeaders;

    /** Optional dynamic header provider FQCNs rendered into @prot.soap.DynamicHeaders; YAML field: x-dynamic-headers */
    private List<String> dynamicHeaders;

    /** Per-operation configuration keyed by operation name; YAML field: x-operations */
    private Map<String, OperationConfig> operations;
}

public class StaticHeader {
    private String name;
    private String value;
    private Boolean ifExisting; // optional
}

public class OperationConfig {
    private String action; // optional; fallback is operation name
}
```

### Canonical YAML Structure

```yaml
x-config-key: pingClient

x-static-headers:
  - name: X-Tenant
    value: demo
    ifExisting: true
  - name: X-Trace-Enabled
    value: "true"

x-dynamic-headers:
  - com.example.soap.header.AuthHeaderProvider
  - com.example.soap.header.LocaleHeaderProvider

x-operations:
  ping:
    action: pingAction
  echo:
    action: ""
```

> **YAML Naming Convention**: All top-level and nested keys use kebab-case with `x-` prefix (e.g., `x-config-key`, `x-static-headers`, `x-operations`).
> **Java Model**: SnakeYAML maps `x-config-key` → `configKey`, `x-static-headers` → `staticHeaders`, etc.

## Resolution Rules

| Field | Rule |
|------|------|
| `resolvedConfigKey` | Use `configKey` if non-null and non-empty; else use SEI `portType` name |
| `resolvedActionName` | For each operation, use configured `action` if non-empty; else use operation name |

## Annotation Emission Mapping

| Target | Annotation | Condition |
|------|------|------|
| SEI type | `@prot.soap.RegisteredSoapClient(resolvedConfigKey)` | Always |
| SEI type | `@prot.soap.StaticHeaders(...)` | `staticHeaders` present and non-empty |
| SEI type | `@prot.soap.DynamicHeaders(...)` | `dynamicHeaders` present and non-empty |
| SEI method | `@prot.soap.SoapAction(resolvedActionName)` | Always for each generated operation |

## Relationships

| Entity | Related To | Type |
|--------|-----------|------|
| ClientGenConfig | CustomSEIGenerator | Supplies generation metadata |
| StaticHeader | ClientGenConfig | Aggregated list item |
| OperationConfig | ClientGenConfig | Map value keyed by operation name |
| ConfigLoader | ClientGenConfig | Creates validated config instance |

## Validation Rules

| Rule | Description |
|------|------------|
| YAML field naming | Must use kebab-case with `x-` prefix (e.g., `x-config-key`, `x-static-headers`, `x-dynamic-headers`, `x-operations`) |
| Java model mapping | SnakeYAML maps YAML kebab-case fields to camelCase Java properties (`configKey`, `staticHeaders`, `dynamicHeaders`, `operations`) |
| `staticHeaders[*].name` | Required non-empty string |
| `staticHeaders[*].value` | Required string |
| `staticHeaders[*].ifExisting` | Optional boolean |
| `dynamicHeaders[*]` | Must be fully qualified class name |
| `operations` keys | Must match WSDL operation names |
| `operations[*].action` | Optional string; empty means fallback to operation name |
| Annotation packages | All generated annotations MUST use `prot.soap.*` namespace (@prot.soap.RegisteredSoapClient, @prot.soap.StaticHeaders, @prot.soap.DynamicHeaders, @prot.soap.SoapAction) |

## State Transitions

N/A - stateless configuration model.
