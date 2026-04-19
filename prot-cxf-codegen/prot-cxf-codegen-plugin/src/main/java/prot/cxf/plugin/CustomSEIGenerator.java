package prot.cxf.plugin;

import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.generators.SEIGenerator;

import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Just a sample custom generator which use custom velocity template to generate SEI
 *
 * @author Valeriy Molyakov
 */
public class CustomSEIGenerator extends SEIGenerator {

    private static final List<String> CLIENT_CONFIG_FLAGS = List.of("-clientgenconfig", "-clientgenconfig",
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
            setAttributes("customSeiAnnotations", getCustomSeiAnnotations());
            setAttributes("customOperationAnnotations", getCustomOperationAnnotations());
        }

        super.doWrite(templateName, outputs);
    }

    private List<String> getCustomSeiAnnotations() {
        if (clientGenConfig == null) {
            return Collections.emptyList();
        }
        return clientGenConfig.getSeiAnnotations();
    }

    private Map<String, List<String>> getCustomOperationAnnotations() {
        if (clientGenConfig == null) {
            return Collections.emptyMap();
        }
        return clientGenConfig.getOperations();
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
