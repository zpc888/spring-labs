package prot.cxf.plugin;

import org.apache.cxf.tools.common.ToolException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ConfigLoader {

    private static final Pattern FQCN_PATTERN =
            Pattern.compile("^([A-Za-z_$][A-Za-z\\d_$]*\\.)+[A-Za-z_$][A-Za-z\\d_$]*$");

    private ConfigLoader() {
    }

    public static ClientGenConfig load(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        try (InputStream input = open(location)) {
            if (input == null) {
                throw new ToolException("Unable to find client generation config: " + location);
            }

            Object loaded = new Yaml().load(input);
            if (loaded == null) {
                return new ClientGenConfig();
            }
            if (!(loaded instanceof Map<?, ?> rawConfig)) {
                throw new ToolException("Invalid YAML client generation config: " + location
                        + ". Root element must be a map.");
            }

            ClientGenConfig config = mapConfig(rawConfig, location);

            validateConfig(config, location);
            return config;
        } catch (ToolException e) {
            throw e;
        } catch (IOException e) {
            throw new ToolException("Unable to read client generation config: " + location, e);
        } catch (RuntimeException e) {
            throw new ToolException("Invalid YAML client generation config: " + location, e);
        }
    }

    private static ClientGenConfig mapConfig(Map<?, ?> raw, String location) {
        requireSupportedKeys(raw.keySet(), location, true);

        ClientGenConfig config = new ClientGenConfig();
        config.setConfigKey(asString(first(raw, "x-config-key", "configKey", "config-key", "configkey")));
        config.setBaseUrl(asString(first(raw, "x-base-url", "baseUrl", "baseurl", "base-url")));
        config.setJaxbContextPaths(asStringList(first(raw,
                "x-jaxb-context-paths", "jaxbContextPaths", "jaxb-context-paths", "jaxbcontextpaths"),
                "jaxbContextPaths", location));
        config.setStaticHeaders(asStaticHeaders(first(raw, "x-static-headers", "staticHeaders", "static-headers"),
                "staticHeaders", location));
        config.setDynamicHeaders(asStringList(first(raw, "x-dynamic-headers", "dynamicHeaders", "dynamic-headers"),
                "dynamicHeaders", location));
        config.setOperations(asOperations(first(raw, "x-operations", "operations"), location));
        return config;
    }

    private static Map<String, OperationConfig> asOperations(Object raw, String location) {
        if (raw == null) {
            return Map.of();
        }
        if (!(raw instanceof Map<?, ?> map)) {
            throw new ToolException("Invalid operations in client generation config: " + location);
        }

        Map<String, OperationConfig> operations = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String operationName = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> opMap)) {
                throw new ToolException("Invalid operation entry in client generation config: " + location
                        + ". Operation '" + operationName + "' must be a map.");
            }
            requireSupportedKeys(opMap.keySet(), location, false);

            OperationConfig operationConfig = new OperationConfig();
            operationConfig.setAction(asString(first(opMap, "action")));
            operationConfig.setStaticHeaders(asStaticHeaders(first(opMap, "static-headers", "staticHeaders"),
                    "operations." + operationName + ".staticHeaders", location));
            operationConfig.setDynamicHeaders(asStringList(first(opMap, "dynamic-headers", "dynamicHeaders"),
                    "operations." + operationName + ".dynamicHeaders", location));
            operations.put(operationName, operationConfig);
        }
        return operations;
    }

    private static List<StaticHeader> asStaticHeaders(Object raw, String field, String location) {
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw new ToolException("Invalid " + field + " in client generation config: " + location);
        }
        List<StaticHeader> headers = new ArrayList<>();
        for (Object item : list) {
            StaticHeader header = null;

            // Support string format "name=value"
            if (item instanceof String stringItem) {
                header = StaticHeader.parseFromConcatenated(stringItem);
                if (header == null) {
                    throw new ToolException("Invalid " + field + " entry in client generation config: " + location
                            + ". String format must be 'name=value' but got: " + stringItem);
                }
            }
            // Support object format {name, value, ifExisting}
            else if (item instanceof Map<?, ?> headerMap) {
                header = new StaticHeader();
                header.setName(asString(first(headerMap, "name")));
                header.setValue(asString(first(headerMap, "value")));
                Object ifExisting = first(headerMap, "ifExisting", "if-existing");
                if (ifExisting instanceof Boolean value) {
                    header.setIfExisting(value);
                }
            }
            else {
                throw new ToolException("Invalid " + field + " entry in client generation config: " + location
                        + ". Expected string or object, got: " + (item != null ? item.getClass().getSimpleName() : "null"));
            }

            headers.add(header);
        }
        return headers;
    }

    private static List<String> asStringList(Object raw, String field, String location) {
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw new ToolException("Invalid " + field + " in client generation config: " + location);
        }
        List<String> values = new ArrayList<>();
        for (Object item : list) {
            values.add(asString(item));
        }
        return values;
    }

    private static Object first(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private static String asString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private static void requireSupportedKeys(Set<?> keys, String location, boolean topLevel) {
        Set<String> supported = topLevel
                ? Set.of("x-config-key", "configKey", "config-key", "configkey",
                "x-base-url", "baseUrl", "baseurl", "base-url",
                "x-jaxb-context-paths", "jaxbContextPaths", "jaxb-context-paths", "jaxbcontextpaths",
                "x-static-headers", "staticHeaders", "static-headers",
                "x-dynamic-headers", "dynamicHeaders", "dynamic-headers",
                "x-operations", "operations")
                : Set.of("action", "static-headers", "staticHeaders", "dynamic-headers", "dynamicHeaders");

        for (Object key : keys) {
            String normalized = String.valueOf(key);
            if (!supported.contains(normalized)) {
                if ("opertions".equals(normalized)) {
                    throw new ToolException("Invalid key 'opertions' in client generation config: "
                            + location + ". Use canonical key 'x-operations'.");
                }
                throw new ToolException("Unsupported key '" + normalized + "' in client generation config: "
                        + location);
            }
        }
    }

    private static void validateConfig(ClientGenConfig config, String location) {
        for (String dynamicHeader : config.getDynamicHeaders()) {
            if (dynamicHeader == null || !FQCN_PATTERN.matcher(dynamicHeader).matches()) {
                throw new ToolException("Invalid dynamicHeaders entry in client generation config: "
                        + location + ". Expected fully-qualified class name but got: " + dynamicHeader);
            }
        }

        for (StaticHeader staticHeader : config.getStaticHeaders()) {
            if (!staticHeader.hasRequiredNameAndValue()) {
                throw new ToolException("Invalid staticHeaders entry in client generation config: "
                        + location + ". staticHeaders.name and staticHeaders.value are required.");
            }
        }

        for (Map.Entry<String, OperationConfig> operation : config.getOperations().entrySet()) {
            for (String dynamicHeader : operation.getValue().getDynamicHeaders()) {
                if (dynamicHeader == null || !FQCN_PATTERN.matcher(dynamicHeader).matches()) {
                    throw new ToolException("Invalid operations." + operation.getKey()
                            + ".dynamicHeaders entry in client generation config: " + location);
                }
            }
            for (StaticHeader staticHeader : operation.getValue().getStaticHeaders()) {
                if (!staticHeader.hasRequiredNameAndValue()) {
                    throw new ToolException("Invalid operations." + operation.getKey()
                            + ".staticHeaders entry in client generation config: " + location);
                }
            }
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
