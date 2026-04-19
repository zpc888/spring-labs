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
        String yamlContent = "sei:\n  annotations:\n    - com.example.CustomAnnotation\n";
        Path yamlFile = tempDir.resolve("test-sei-annotations.yaml");
        Files.writeString(yamlFile, yamlContent);

        ClientGenConfig config = ConfigLoader.load(yamlFile.toAbsolutePath().toString());

        assertNotNull(config, "Config should not be null");
        assertNotNull(config.getSeiAnnotations(), "SEI annotations should not be null");
        assertTrue(config.getSeiAnnotations().contains("com.example.CustomAnnotation"),
                "Should contain 'com.example.CustomAnnotation'");
    }

    @Test
    void loadFromFilePath_withOperations() throws IOException {
        String yamlContent = "operations:\n  ping:\n    - com.example.PingHandler\n";
        Path yamlFile = tempDir.resolve("test-operation-annotations.yaml");
        Files.writeString(yamlFile, yamlContent);

        ClientGenConfig config = ConfigLoader.load(yamlFile.toAbsolutePath().toString());

        assertNotNull(config);
        assertNotNull(config.getOperations());
        assertTrue(config.getOperations().containsKey("ping"));
        assertTrue(config.getOperations().get("ping").contains("com.example.PingHandler"));
    }
}

