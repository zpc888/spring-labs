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
                x-base-url: https://localhost:8080/soap
                x-jaxb-context-paths:
                  - com.example.one
                  - com.example.two
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
        assertEquals("https://localhost:8080/soap", config.getBaseUrl());
        assertEquals(java.util.List.of("com.example.one", "com.example.two"), config.getJaxbContextPaths());
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
                    static-headers:
                      - name: X-Op
                        value: ping
                    dynamic-headers:
                      - com.example.PingHeader
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
        assertEquals(1, config.getOperations().get("ping").getStaticHeaders().size());
        assertEquals(java.util.List.of("com.example.PingHeader"), config.getOperations().get("ping").getDynamicHeaders());
        assertEquals("echo", config.resolveOperationAction("echo"));
    }

    @Test
    void loadFromFilePath_acceptsTopLevelAliases() throws IOException {
        String yamlContent = """
                configKey: aliasClient
                base-url: https://localhost:8081/alias
                jaxbcontextpaths:
                  - com.example.alias
                """;
        Path yamlFile = tempDir.resolve("alias-sei-annotations.yaml");
        Files.writeString(yamlFile, yamlContent);

        ClientGenConfig config = ConfigLoader.load(yamlFile.toAbsolutePath().toString());

        assertEquals("aliasClient", config.getConfigKey());
        assertEquals("https://localhost:8081/alias", config.getBaseUrl());
        assertEquals(java.util.List.of("com.example.alias"), config.getJaxbContextPaths());
    }
}
