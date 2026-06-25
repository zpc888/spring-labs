package prot.cxf.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClientGenConfig {

    private String configKey;
    private String baseUrl;
    private List<String> jaxbContextPaths;
    private List<StaticHeader> staticHeaders;
    private List<String> dynamicHeaders;
    private Map<String, OperationConfig> operations;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<String> getJaxbContextPaths() {
        return jaxbContextPaths == null ? List.of() : jaxbContextPaths;
    }

    public void setJaxbContextPaths(List<String> jaxbContextPaths) {
        this.jaxbContextPaths = jaxbContextPaths;
    }

    public List<StaticHeader> getStaticHeaders() {
        return staticHeaders == null ? List.of() : staticHeaders;
    }

    public void setStaticHeaders(List<StaticHeader> staticHeaders) {
        this.staticHeaders = staticHeaders;
    }

    public List<String> getDynamicHeaders() {
        return dynamicHeaders == null ? List.of() : dynamicHeaders;
    }

    public void setDynamicHeaders(List<String> dynamicHeaders) {
        this.dynamicHeaders = dynamicHeaders;
    }

    public Map<String, OperationConfig> getOperations() {
        return operations == null ? Collections.emptyMap() : operations;
    }

    public void setOperations(Map<String, OperationConfig> operations) {
        this.operations = operations;
    }

    public String resolveConfigKey(String fallbackPortTypeName) {
        if (configKey != null && !configKey.isBlank()) {
            return configKey;
        }
        return fallbackPortTypeName;
    }

    public String resolveOperationAction(String operationName) {
        OperationConfig operationConfig = getOperations().get(operationName);
        return operationConfig == null ? operationName : operationConfig.resolveAction(operationName, operationName);
    }
}
