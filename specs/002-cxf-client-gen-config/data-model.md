# Data Model: CXF Client Gen Config

## ClientGenConfig

Root YAML model for generation-time annotation rules.

**YAML Field Naming**: Kebab-case canonical keys with `x-` prefix (e.g., `x-config-key`, `x-base-url`)  
**Java Model Naming**: camelCase (e.g., `configKey`, `baseUrl`)  
**Mapping**: SnakeYAML maps canonical keys and accepted aliases to Java properties.

```java
package prot.cxf.plugin;

import java.util.List;
import java.util.Map;

public class ClientGenConfig {
    /** Optional explicit client key; fallback is portType name; YAML field: x-config-key */
    private String configKey;

    /** Optional client base URL; YAML canonical field: x-base-url; aliases: baseUrl/baseurl/base-url */
    private String baseUrl;

    /** Optional JAXB context paths; YAML canonical field: x-jaxb-context-paths; aliases accepted */
    private List<String> jaxbContextPaths;

    /** Optional static headers rendered into @prot.soap.SoapClient.staticHeaders as concatenated strings; YAML field: x-static-headers */
    private List<String> staticHeaders;

    /** Optional dynamic header provider FQCNs rendered into @prot.soap.SoapClient.dynamicHeaders; YAML field: x-dynamic-headers */
    private List<String> dynamicHeaders;

    /** Per-operation configuration keyed by operation name; YAML field: x-operations */
    private Map<String, OperationConfig> operations;
}

public class OperationConfig {
    private String action; // optional; fallback is wsdl binding soapAction then operation name
    private List<String> staticHeaders; // optional; concatenated "name=value" strings
    private List<String> dynamicHeaders; // optional FQCN
}
```

### Canonical YAML Structure

```yaml
x-config-key: pingClient
x-base-url: https://localhost:8081/soap-simulator
x-jaxb-context-paths:
  - com.example
  - com.prot

x-static-headers:
  - "X-Tenant=demo"
  - "X-Trace-Enabled=true"

x-dynamic-headers:
  - com.example.soap.header.AuthHeaderProvider
  - com.example.soap.header.LocaleHeaderProvider

x-operations:
  ping:
    action: pingAction
    static-headers:
      - "X-Op=opA"
      - "X-Priority=high"
    dynamic-headers:
      - com.example.soap.header.OpHeaderProvider
  echo:
    action: ""
```

> **YAML Naming Convention**: Canonical top-level keys use kebab-case with `x-` prefix (e.g., `x-config-key`, `x-base-url`, `x-jaxb-context-paths`, `x-operations`).
> **Header Format**: Static headers are concatenated strings `"name=value"` (not objects); dynamic headers are FQCNs as strings.
> **Alias Handling**: `baseUrl`, `baseurl`, `base-url` map to `baseUrl`; `jaxbContextPaths`, `jaxb-context-paths`, `jaxbcontextpaths` map to `jaxbContextPaths`.
> **Operation-Level Aliases**: `x-static-headers|static-headers|staticHeaders|staticheaders` → `staticHeaders`; `x-dynamic-headers|dynamic-headers|dynamicHeaders|dynamicheaders` → `dynamicHeaders`.
> **Java Model**: SnakeYAML maps canonical/alias keys to camelCase properties.

### Generated Annotation Examples

**Class-Level Annotation** (from top-level config):
```java
@prot.soap.SoapClient(
    value = "pingClient",
    baseUrl = "https://localhost:8081/soap-simulator",
    jaxbContextPaths = {"com.example", "com.prot"},
    staticHeaders = {"X-Tenant=demo", "X-Trace-Enabled=true"},
    dynamicHeaders = {
        com.example.soap.header.AuthHeaderProvider.class,
        com.example.soap.header.LocaleHeaderProvider.class
    }
)
public interface PingSEI { ... }
```

**Method-Level Annotations** (from operation config):
```java
@prot.soap.SoapAction("pingAction")
@prot.soap.SoapMethodHeader(
    staticHeaders = {"X-Op=opA", "X-Priority=high"},
    dynamicHeaders = {com.example.soap.header.OpHeaderProvider.class}
)
String ping(String message);

@prot.soap.SoapAction("echoAction")
String echo(String input);
```

## Resolution Rules

| Field | Rule |
|------|------|
| `resolvedConfigKey` | Use `configKey` if non-null and non-empty; else use SEI `portType` name |
| `resolvedActionName` | For each operation, use configured `action` if non-empty; else use WSDL binding `soap:operation@soapAction`; else operation name |

## Annotation Emission Mapping

| Target | Annotation | Condition |
|------|------|------|
| SEI type | `@prot.soap.SoapClient(value=resolvedConfigKey, ...)` | Always; non-value attributes only when configured |
| SEI method | `@prot.soap.SoapAction(resolvedActionName)` | Always for each generated operation |
| SEI method | `@prot.soap.SoapMethodHeader(...)` | Emit only when operation `staticHeaders` or `dynamicHeaders` is non-empty |

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
| YAML field naming | Canonical keys use kebab-case with `x-` prefix (including `x-base-url`, `x-jaxb-context-paths`) |
| Java model mapping | SnakeYAML maps canonical/alias YAML keys to camelCase Java properties (`configKey`, `baseUrl`, `jaxbContextPaths`, `staticHeaders`, `dynamicHeaders`, `operations`) |
| `staticHeaders[*]` | Must be concatenated strings in format `"name=value"` (e.g., `"X-Header=value"`) |
| `dynamicHeaders[*]` | Must be fully qualified class name as string |
| `operations` keys | Must match WSDL operation names |
| `operations[*].action` | Optional string; empty means fallback to WSDL binding `soapAction`, then operation name |
| `operations[*].staticHeaders[*]` | Must be concatenated strings in format `"name=value"` |
| `operations[*].dynamicHeaders[*]` | Must be fully qualified class name as string |
| Top-level alias support | `baseUrl\|baseurl\|base-url` and `jaxbContextPaths\|jaxb-context-paths\|jaxbcontextpaths` MUST map to canonical Java fields |
| Operation-level header aliases | `x-static-headers\|static-headers\|staticHeaders\|staticheaders` → `staticHeaders`; `x-dynamic-headers\|dynamic-headers\|dynamicHeaders\|dynamicheaders` → `dynamicHeaders` |
| Annotation packages | All generated annotations MUST use `prot.soap.*` namespace (@prot.soap.SoapClient, @prot.soap.SoapAction, @prot.soap.SoapMethodHeader) |
| Static header format (generated) | Array of concatenated strings (e.g., `staticHeaders = {"X-Calc-Op=minus", "X-Overflow=false"}`) |
| Dynamic header format (generated) | Array of Class<?> references with imports (e.g., `dynamicHeaders = {prot.soap.header.SoapHeaderA.class, prot.soap.header.SoapHeaderB.class}`) |

## State Transitions

N/A - stateless configuration model.
