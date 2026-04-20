package prot.cxf.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderFileTest {

    @TempDir
    Path tempDir;

    @Test
    void loadFromFilePath_returnsParsedConfig() throws IOException {
        String yamlContent = """
                x-config-key: pingClient
                x-static-headers:
                  - name: X-Tenant
                    value: demo
                    ifExisting: true
                x-dynamic-headers:
                  - com.example.HeaderProvider
                """;
        Path yamlFile = tempDir.resolve("test-sei-annotations.yaml");
        Files.writeString(yamlFile, yamlContent);

        ClientGenConfig config = ConfigLoader.load(yamlFile.toAbsolutePath().toString());

        assertNotNull(config, "Config should not be null");
        assertEquals("pingClient", config.getConfigKey());
        assertEquals(1, config.getStaticHeaders().size());
        assertEquals("X-Tenant", config.getStaticHeaders().get(0).getName());
        assertEquals("demo", config.getStaticHeaders().get(0).getValue());
        assertEquals(Boolean.TRUE, config.getStaticHeaders().get(0).getIfExisting());
        assertEquals(java.util.List.of("com.example.HeaderProvider"), config.getDynamicHeaders());
    }

    @Test
    void loadFromFilePath_withOperations() throws IOException {
        String yamlContent = """
                x-operations:
                  ping:
                    action: pingAction
                  echo:
                    action: ""
                """;
        Path yamlFile = tempDir.resolve("test-operation-annotations.yaml");
        Files.writeString(yamlFile, yamlContent);

        ClientGenConfig config = ConfigLoader.load(yamlFile.toAbsolutePath().toString());

        assertNotNull(config);
        assertNotNull(config.getOperations());
        assertTrue(config.getOperations().containsKey("ping"));
        assertEquals("pingAction", config.getOperations().get("ping").getAction());
        assertEquals("echo", config.resolveOperationAction("echo"));
    }
}
