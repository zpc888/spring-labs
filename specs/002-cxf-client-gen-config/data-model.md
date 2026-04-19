# Data Model: CXF Client Gen Config

## ClientGenConfig

The main configuration model parsed from YAML.

```java
package prot.cxf.plugin;

import java.util.List;
import java.util.Map;

/**
 * Configuration model for client generation customization.
 *Parsed from YAML via SnakeYAML.
 */
public class ClientGenConfig {
    
    /** Class-level annotations to add to the SEI interface */
    private List<String> seiAnnotations;
    
    /** Operation-specific annotations keyed by operation name */
    private Map<String, List<String>> operations;
    
    public List<String> getSeiAnnotations() {
        return seiAnnotations;
    }
    
    public void setSeiAnnotations(List<String> seiAnnotations) {
        this.seiAnnotations = seiAnnotations;
    }
    
    public Map<String, List<String>> getOperations() {
        return operations;
    }
    
    public void setOperations(Map<String, List<String>> operations) {
        this.operations = operations;
    }
    
    /**
     * Get annotations for a specific operation.
     * @param operationName The operation name from WSDL
     * @return List of annotation class names, empty list if not found
     */
    public List<String> getOperationAnnotations(String operationName) {
        if (operations == null || operationName == null) {
            return List.of();
        }
        return operations.getOrDefault(operationName, List.of());
    }
    
    /**
     * Check if this config has any SEI annotations.
     * @return true if seiAnnotations is not empty
     */
    public boolean hasSeiAnnotations() {
        return seiAnnotations != null && !seiAnnotations.isEmpty();
    }
}
```

### YAML Structure

```yaml
# SEI-level annotations (applied to the interface class)
sei:
  annotations:
    - javax.xml.ws.annotation.WebService
    - com.example.CustomAnnotation

# Per-operation annotations (applied to each method)
operations:
  ping:
    - com.example.PingHandler
  echo:
    - com.example.EchoHandler
  addBook:
    - com.example.AddBookHandler
```

### Relationships

| Entity | Related To | Type |
|--------|-----------|------|
| ClientGenConfig | CustomSEIGenerator | Uses config to generate code |
| ConfigLoader | ClientGenConfig | Creates config instance |

## Validation Rules

| Rule | Description |
|------|------------|
| sei.annotations non-null | Must be list if present |
| operations keys non-null | Must be valid operation names |
| annotations are FQN | Must be fully qualified class names |

## State Transitions

N/A - Stateless data model

## Example Values

### Example 1: Basic Config

```yaml
sei:
  annotations:
    - javax.xml.ws.WebService
    - com.example.CustomSEI
operations:
  ping:
    - com.example.PingHandler
```

### Example 2: Multiple Annotations

```yaml
sei:
  annotations:
    - javax.xml.ws.WebService
    - com.example.MyCustomAnnotation
operations:
  getBook:
    - com.example.GetHandler
    - com.example.LoggingHandler
  addBook:
    - com.example.AddHandler
    - jakarta.jws.WebMethod
```