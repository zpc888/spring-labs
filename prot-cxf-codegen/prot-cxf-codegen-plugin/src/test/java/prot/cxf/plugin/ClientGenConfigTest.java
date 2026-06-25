package prot.cxf.plugin;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClientGenConfigTest {

    @Test
    void getStaticHeaders_whenUnset_returnsEmptyList() {
        ClientGenConfig config = new ClientGenConfig();
        assertEquals(List.of(), config.getStaticHeaders());
    }

    @Test
    void getJaxbContextPaths_whenUnset_returnsEmptyList() {
        ClientGenConfig config = new ClientGenConfig();
        assertEquals(List.of(), config.getJaxbContextPaths());
    }

    @Test
    void resolveConfigKey_prefersConfiguredValue() {
        ClientGenConfig config = new ClientGenConfig();
        config.setConfigKey("configuredClient");

        assertEquals("configuredClient", config.resolveConfigKey("PingServicePortType"));
    }

    @Test
    void resolveConfigKey_fallsBackToPortTypeName() {
        ClientGenConfig config = new ClientGenConfig();

        assertEquals("PingServicePortType", config.resolveConfigKey("PingServicePortType"));
    }

    @Test
    void getOperations_null_returnsEmptyMap() {
        ClientGenConfig config = new ClientGenConfig();
        assertEquals(Map.of(), config.getOperations());
    }

    @Test
    void getOperations_withEntries_returnsMap() {
        OperationConfig operationConfig = new OperationConfig();
        operationConfig.setAction("configuredAction");
        Map<String, OperationConfig> ops = Map.of("ping", operationConfig);

        ClientGenConfig config = new ClientGenConfig();
        config.setOperations(ops);

        assertEquals(1, config.getOperations().size());
        assertTrue(config.getOperations().containsKey("ping"));
        assertEquals("configuredAction", config.getOperations().get("ping").getAction());
    }

    @Test
    void resolveOperationAction_returnsConfiguredAction() {
        OperationConfig operationConfig = new OperationConfig();
        operationConfig.setAction("pingAction");

        ClientGenConfig config = new ClientGenConfig();
        config.setOperations(Map.of("ping", operationConfig));

        assertEquals("pingAction", config.resolveOperationAction("ping"));
    }

    @Test
    void resolveOperationAction_fallsBackWhenActionBlank() {
        OperationConfig operationConfig = new OperationConfig();
        operationConfig.setAction("   ");

        ClientGenConfig config = new ClientGenConfig();
        config.setOperations(Map.of("ping", operationConfig));

        assertEquals("ping", config.resolveOperationAction("ping"));
    }

    @Test
    void getDynamicHeaders_whenUnset_returnsEmptyList() {
        ClientGenConfig config = new ClientGenConfig();
        assertEquals(List.of(), config.getDynamicHeaders());
    }

    @Test
    void baseUrl_roundTrips() {
        ClientGenConfig config = new ClientGenConfig();
        config.setBaseUrl("https://localhost:8081/mock");
        assertEquals("https://localhost:8081/mock", config.getBaseUrl());
    }
}
