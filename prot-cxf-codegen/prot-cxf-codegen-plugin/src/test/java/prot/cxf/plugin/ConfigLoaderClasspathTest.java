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
        assertNotNull(config.getSeiAnnotations());
        assertEquals(java.util.List.of("com.example.CustomAnnotation"), config.getSeiAnnotations());
    }

    @Test
    void loadFromClasspath_operationAnnotations() {
        ClientGenConfig config = ConfigLoader.load("prot/cxf/plugin/operation-annotations.yaml");

        assertNotNull(config);
        assertTrue(config.getOperations().containsKey("ping"));
        assertTrue(config.getOperations().get("ping").contains("com.example.PingHandler"));
    }
}

