package prot.cxf.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderClasspathTest {

    @Test
    void loadFromClasspath_returnsParsedConfig() {
        // "prot/cxf/plugin/sei-annotations.yaml" does not exist on the file system
        // but is present on the test classpath
        ClientGenConfig config = ConfigLoader.load("prot/cxf/plugin/sei-annotations.yaml");

        assertNotNull(config, "Config should not be null when loaded from classpath");
        assertEquals("customClient", config.getConfigKey());
        assertEquals("https://localhost:8081/mock", config.getBaseUrl());
        assertEquals(java.util.List.of("com.example.a", "com.example.b"), config.getJaxbContextPaths());
        assertEquals(1, config.getStaticHeaders().size());
        assertEquals("X-Tenant", config.getStaticHeaders().get(0).getName());
        assertEquals(java.util.List.of("com.example.CustomHeaderProvider"), config.getDynamicHeaders());
    }

    @Test
    void loadFromClasspath_operationAnnotations() {
        ClientGenConfig config = ConfigLoader.load("prot/cxf/plugin/operation-annotations.yaml");

        assertNotNull(config);
        assertTrue(config.getOperations().containsKey("ping"));
        assertEquals("pingAction", config.resolveOperationAction("ping"));
        assertEquals(1, config.getOperations().get("ping").getStaticHeaders().size());
        assertEquals(java.util.List.of("com.example.PingHeaderProvider"),
                config.getOperations().get("ping").getDynamicHeaders());
    }
}
