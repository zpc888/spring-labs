package prot.cxf.plugin;

import org.apache.cxf.tools.common.ToolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderErrorTest {

    @TempDir
    Path tempDir;

    @Test
    void load_null_returnsNull() {
        assertNull(ConfigLoader.load(null));
    }

    @Test
    void load_blank_returnsNull() {
        assertNull(ConfigLoader.load("  "));
    }

    @Test
    void load_missingFile_throwsToolException() {
        ToolException ex = assertThrows(ToolException.class,
                () -> ConfigLoader.load("no-such-file.yaml"));
        assertTrue(ex.getMessage().contains("Unable to find"),
                "Expected missing-file message, got: " + ex.getMessage());
    }

    @Test
    void load_malformedYaml_throwsToolException() throws IOException {
        Path malformed = tempDir.resolve("malformed.yaml");
        Files.writeString(malformed, "{bad: [unclosed");

        ToolException ex = assertThrows(ToolException.class,
                () -> ConfigLoader.load(malformed.toAbsolutePath().toString()));
        assertTrue(ex.getMessage().contains("Invalid YAML"),
                "Expected 'Invalid YAML' in: " + ex.getMessage());
    }

    @Test
    void load_invalidOpertionsKey_throwsToolExceptionWithCanonicalHint() throws IOException {
        Path yaml = tempDir.resolve("invalid-opertions.yaml");
        Files.writeString(yaml, "opertions:\n  ping:\n    action: pingAction\n");

        ToolException ex = assertThrows(ToolException.class,
                () -> ConfigLoader.load(yaml.toAbsolutePath().toString()));
        assertTrue(ex.getMessage().contains("opertions"));
        assertTrue(ex.getMessage().contains("x-operations"));
    }

    @Test
    void load_invalidDynamicHeader_throwsToolException() throws IOException {
        Path yaml = tempDir.resolve("invalid-dynamic-header.yaml");
        Files.writeString(yaml, "x-dynamic-headers:\n  - NotAQualifiedName\n");

        ToolException ex = assertThrows(ToolException.class,
                () -> ConfigLoader.load(yaml.toAbsolutePath().toString()));
        assertTrue(ex.getMessage().contains("dynamicHeaders"));
    }

    @Test
    void load_unsupportedTopLevelKey_throwsToolException() throws IOException {
        Path yaml = tempDir.resolve("unsupported-key.yaml");
        Files.writeString(yaml, "x-unsupported: true\n");

        ToolException ex = assertThrows(ToolException.class,
                () -> ConfigLoader.load(yaml.toAbsolutePath().toString()));
        assertTrue(ex.getMessage().contains("Unsupported key"));
    }

    @Test
    void load_invalidOperationDynamicHeader_throwsToolException() throws IOException {
        Path yaml = tempDir.resolve("invalid-op-dynamic-header.yaml");
        Files.writeString(yaml, "x-operations:\n  ping:\n    dynamic-headers:\n      - notQualified\n");

        ToolException ex = assertThrows(ToolException.class,
                () -> ConfigLoader.load(yaml.toAbsolutePath().toString()));
        assertTrue(ex.getMessage().contains("operations.ping.dynamicHeaders"));
    }
}
