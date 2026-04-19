package prot.cxf.plugin;

import org.apache.cxf.tools.common.ToolException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {

    private ConfigLoader() {
    }

    public static ClientGenConfig load(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        Yaml yaml = new Yaml();
        try (InputStream input = open(location)) {
            if (input == null) {
                throw new ToolException("Unable to find client generation config: " + location);
            }
            return yaml.loadAs(input, ClientGenConfig.class);
        } catch (IOException e) {
            throw new ToolException("Unable to read client generation config: " + location, e);
        } catch (RuntimeException e) {
            throw new ToolException("Invalid YAML client generation config: " + location, e);
        }
    }

    private static InputStream open(String location) throws IOException {
        Path path = Path.of(location);
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            InputStream input = classLoader.getResourceAsStream(location);
            if (input != null) {
                return input;
            }
        }

        return ConfigLoader.class.getClassLoader().getResourceAsStream(location);
    }
}

