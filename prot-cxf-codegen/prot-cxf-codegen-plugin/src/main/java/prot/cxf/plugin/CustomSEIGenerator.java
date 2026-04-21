package prot.cxf.plugin;

import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.generators.SEIGenerator;

import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Just a sample custom generator which use custom velocity template to generate SEI
 *
 * @author Valeriy Molyakov
 */
public class CustomSEIGenerator extends SEIGenerator {

    private static final List<String> CLIENT_CONFIG_FLAGS = List.of("-clientgenconfig", "--clientgenconfig",
            "--client-gen-config", "-client-gen-config",
            "-clientGenConfig", "--clientGenConfig");
    private ClientGenConfig clientGenConfig;

    @Override
    public void generate(ToolContext penv) throws ToolException {
        this.clientGenConfig = ConfigLoader.load(resolveClientGenConfigLocation(penv));
        super.generate(penv);
    }

    @Override
    protected void doWrite(String templateName, Writer outputs) throws ToolException {

        if (templateName.endsWith("/sei.vm")) {
            templateName = "prot-cxf-sei.vm";
            setAttributes("customConfigKey", getConfigKey());
            setAttributes("customBaseUrl", getBaseUrl());
            setAttributes("customJaxbContextPaths", getJaxbContextPaths());
            setAttributes("customStaticHeaders", getStaticHeaders());
            setAttributes("customStaticHeadersAsStrings", getStaticHeadersAsStrings(getStaticHeaders()));
            setAttributes("customDynamicHeaders", getDynamicHeaders());
            setAttributes("customOperationConfigs", getOperationConfigs());
        }

        super.doWrite(templateName, outputs);
    }

    private String getConfigKey() {
        if (clientGenConfig == null) {
            return null;
        }
        return clientGenConfig.getConfigKey();
    }

    private String getBaseUrl() {
        if (clientGenConfig == null) {
            return null;
        }
        return clientGenConfig.getBaseUrl();
    }

    private List<String> getJaxbContextPaths() {
        if (clientGenConfig == null) {
            return List.of();
        }
        return clientGenConfig.getJaxbContextPaths();
    }

    private List<StaticHeader> getStaticHeaders() {
        if (clientGenConfig == null) {
            return Collections.emptyList();
        }
        return clientGenConfig.getStaticHeaders();
    }

    private List<String> getDynamicHeaders() {
        if (clientGenConfig == null) {
            return Collections.emptyList();
        }
        return clientGenConfig.getDynamicHeaders();
    }

    private Map<String, OperationConfig> getOperationConfigs() {
        if (clientGenConfig == null) {
            return Collections.emptyMap();
        }
        Map<String, OperationConfig> configured = clientGenConfig.getOperations();
        Map<String, OperationConfig> normalized = new HashMap<>(configured);
        configured.forEach((name, config) -> {
            normalized.putIfAbsent(decapitalize(name), config);
            normalized.putIfAbsent(capitalize(name), config);
        });
        return normalized;
    }

    /**
     * Convert StaticHeader objects to concatenated strings for annotation generation
     */
    public List<String> getStaticHeadersAsStrings(List<StaticHeader> headers) {
        return headers.stream()
                .map(StaticHeader::toConcatenatedFormat)
                .filter(s -> !s.isBlank())
                .toList();
    }

    /**
     * Get dynamic headers (already strings/FQCNs)
     */
    public List<String> getDynamicHeadersAsFQCNs(List<String> headers) {
        return headers;
    }


    private String decapitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.length() == 1) {
            return value.toLowerCase();
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.length() == 1) {
            return value.toUpperCase();
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String resolveClientGenConfigLocation(ToolContext context) {
        for (String key : List.of("clientgenconfig", "clientGenConfig")) {
            Object directValue = context.get(key);
            if (directValue instanceof String value && !value.isBlank()) {
                return normalizeOptionValue(value);
            }
        }

        Map<String, Object> paramMap = extractParamMap(context);
        for (Object value : paramMap.values()) {
            String fromCollection = findFlagValue(value);
            if (fromCollection != null) {
                return fromCollection;
            }
        }
        return null;
    }

    private String normalizeOptionValue(String value) {
        if (value.startsWith("=")) {
            return value.substring(1);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParamMap(ToolContext context) {
        try {
            Field field = ToolContext.class.getDeclaredField("paramMap");
            field.setAccessible(true);
            Object raw = field.get(context);
            if (raw instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
        } catch (ReflectiveOperationException ignored) {
            // best-effort only
        }
        return Collections.emptyMap();
    }

    private String findFlagValue(Object rawValue) {
        if (rawValue instanceof String[] array) {
            return findFlagValue(Arrays.asList(array));
        }
        if (rawValue instanceof Collection<?> values) {
            String previous = null;
            for (Object item : values) {
                if (!(item instanceof String value)) {
                    previous = null;
                    continue;
                }
                if (previous != null && CLIENT_CONFIG_FLAGS.contains(previous) && !value.isBlank()) {
                    return value;
                }
                previous = value;
            }
        }
        return null;
    }
}
