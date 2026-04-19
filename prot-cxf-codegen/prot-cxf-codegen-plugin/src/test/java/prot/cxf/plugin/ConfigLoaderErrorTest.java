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
        // ToolException is a RuntimeException, so the inner "Unable to find" throw is
        // caught by the outer RuntimeException handler and re-wrapped as "Invalid YAML".
        // Verify that a ToolException is thrown and the cause chain contains "Unable to find".
        ToolException ex = assertThrows(ToolException.class,
                () -> ConfigLoader.load("no-such-file.yaml"));
        // The outer message is "Invalid YAML ...", the cause carries "Unable to find ..."
        Throwable cause = ex.getCause();
        assertNotNull(cause, "ToolException should have a cause for missing file");
        assertTrue(cause.getMessage().contains("Unable to find"),
                "Cause should contain 'Unable to find' in: " + cause.getMessage());
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
}

