package prot.cxf.plugin;

import org.apache.cxf.tools.common.ToolException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
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

            // Load YAML and validate keys
            Yaml yaml = new Yaml();
            Map<Object, Object> rawConfig = yaml.load(input);
            validateCanonicalKeys(rawConfig, location);

            if (rawConfig == null || rawConfig.isEmpty()) {
                return new ClientGenConfig();
            }

            // Convert kebab-case keys to camelCase
            Map<Object, Object> convertedConfig = convertKeysToCamelCase(rawConfig);

            ClientGenConfig config = new ClientGenConfig();
            // Manually map the converted config to the model
            mapConfigToModel(convertedConfig, config);

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

    private static Map<Object, Object> convertKeysToCamelCase(Map<Object, Object> map) {
        Map<Object, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            // Convert kebab-case key to camelCase (only for top-level keys starting with 'x-')
            String camelCaseKey = convertToCamelCase(key);

            // For nested maps and lists: Only convert the operations nested maps,
            // but not the staticHeaders list items (which have natural keys like 'name', 'value')
            if (camelCaseKey.equals("operations") && value instanceof Map) {
                // Operations map keys are operation names - convert them too
                Map<Object, Object> opsMap = (Map<Object, Object>) value;
                Map<Object, Object> convertedOpsMap = new LinkedHashMap<>();
                for (Map.Entry<Object, Object> opEntry : opsMap.entrySet()) {
                    String opKey = opEntry.getKey().toString();
                    Object opValue = opEntry.getValue();
                    if (opValue instanceof Map) {
                        convertedOpsMap.put(opKey, convertKeysToCamelCase((Map<Object, Object>) opValue));
                    } else {
                        convertedOpsMap.put(opKey, opValue);
                    }
                }
                value = convertedOpsMap;
            }
            // Note: do NOT recursively convert lists like staticHeaders - their inner keys (name, value)
            // are not kebab-case and should stay as-is

            result.put(camelCaseKey, value);
        }
        return result;
    }

    private static void mapConfigToModel(Map<Object, Object> map, ClientGenConfig config) {
        if (map.containsKey("configKey")) {
            config.setConfigKey((String) map.get("configKey"));
        }
        if (map.containsKey("staticHeaders")) {
            Object staticHeadersObj = map.get("staticHeaders");
            if (staticHeadersObj instanceof java.util.List) {
                java.util.List<?> headersList = (java.util.List<?>) staticHeadersObj;
                java.util.List<StaticHeader> headers = new java.util.ArrayList<>();
                for (Object obj : headersList) {
                    if (obj instanceof Map) {
                        Map<Object, Object> headerMap = (Map<Object, Object>) obj;
                        StaticHeader header = new StaticHeader();
                        header.setName((String) headerMap.get("name"));
                        header.setValue((String) headerMap.get("value"));
                        if (headerMap.containsKey("ifExisting")) {
                            header.setIfExisting((Boolean) headerMap.get("ifExisting"));
                        }
                        headers.add(header);
                    }
                }
                config.setStaticHeaders(headers);
            }
        }
        if (map.containsKey("dynamicHeaders")) {
            Object dynamicHeadersObj = map.get("dynamicHeaders");
            if (dynamicHeadersObj instanceof java.util.List) {
                config.setDynamicHeaders((java.util.List<String>) dynamicHeadersObj);
            }
        }
        if (map.containsKey("operations")) {
            Object operationsObj = map.get("operations");
            if (operationsObj instanceof Map) {
                Map<Object, Object> opsMap = (Map<Object, Object>) operationsObj;
                Map<String, OperationConfig> operations = new LinkedHashMap<>();
                for (Map.Entry<Object, Object> entry : opsMap.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<Object, Object> opMap = (Map<Object, Object>) entry.getValue();
                        OperationConfig opConfig = new OperationConfig();
                        if (opMap.containsKey("action")) {
                            opConfig.setAction((String) opMap.get("action"));
                        }
                        operations.put(entry.getKey().toString(), opConfig);
                    }
                }
                config.setOperations(operations);
            }
        }
    }

    private static void validateCanonicalKeys(Object rawConfig, String location) {
        if (rawConfig instanceof Map<?, ?> map) {
            if (map.containsKey("opertions")) {
                throw new ToolException("Invalid key 'opertions' in client generation config: "
                        + location + ". Use canonical key 'x-operations'.");
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
            if (staticHeader.getName() == null || staticHeader.getName().isBlank()) {
                throw new ToolException("Invalid staticHeaders entry in client generation config: "
                        + location + ". staticHeaders.name is required.");
            }
            if (staticHeader.getValue() == null) {
                throw new ToolException("Invalid staticHeaders entry in client generation config: "
                        + location + ". staticHeaders.value is required.");
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


    /**
     * Convert kebab-case with x- prefix (x-config-key, x-static-headers) to camelCase (configKey, staticHeaders).
     * Drops the 'x-' prefix and converts remaining parts to camelCase.
     */
    private static String convertToCamelCase(String kebabCase) {
        if (kebabCase == null || !kebabCase.contains("-")) {
            return kebabCase;
        }

        // Remove x- prefix if present
        String withoutPrefix = kebabCase;
        if (kebabCase.startsWith("x-")) {
            withoutPrefix = kebabCase.substring(2);
        }

        if (!withoutPrefix.contains("-")) {
            return withoutPrefix;
        }

        StringBuilder result = new StringBuilder();
        String[] parts = withoutPrefix.split("-");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                result.append(part);
            } else {
                if (part.length() > 0) {
                    result.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) {
                        result.append(part.substring(1));
                    }
                }
            }
        }
        return result.toString();
    }
}

