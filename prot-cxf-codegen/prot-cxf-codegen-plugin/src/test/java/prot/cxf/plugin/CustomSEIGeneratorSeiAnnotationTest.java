package prot.cxf.plugin;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the prot-cxf-sei.vm template emits required and optional SEI-level annotations.
 */
class CustomSEIGeneratorSeiAnnotationTest {

    private VelocityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        engine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        engine.init();
    }

    @Test
    void template_alwaysRendersRegisteredSoapClient_beforeInterfaceDeclaration() throws Exception {
        VelocityContext ctx = buildMinimalContext();
        ctx.put("customConfigKey", "myClient");
        ctx.put("customStaticHeaders", List.of());
        ctx.put("customDynamicHeaders", List.of());
        ctx.put("customOperationConfigs", Map.of());

        String rendered = render(ctx);

        int annotationIdx = rendered.indexOf("@prot.soap.RegisteredSoapClient(\"myClient\")");
        int interfaceIdx = rendered.indexOf("public interface");
        assertTrue(annotationIdx >= 0,
                "Rendered output should contain @prot.soap.RegisteredSoapClient(\"myClient\"):\n" + rendered);
        assertTrue(interfaceIdx >= 0, "Rendered output should contain 'public interface'");
        assertTrue(annotationIdx < interfaceIdx,
                "@prot.soap.RegisteredSoapClient should appear before 'public interface'");
    }

    @Test
    void template_usesPortTypeNameWhenConfigKeyMissing() throws Exception {
        VelocityContext ctx = buildMinimalContext();
        ctx.put("customConfigKey", "   ");
        ctx.put("customStaticHeaders", List.of());
        ctx.put("customDynamicHeaders", List.of());
        ctx.put("customOperationConfigs", Map.of());

        String rendered = render(ctx);

        assertTrue(rendered.contains("@prot.soap.RegisteredSoapClient(\"MyService\")"),
                "Port type name should be used as fallback config key");
    }

    @Test
    void template_staticAndDynamicHeaders_areConditional() throws Exception {
        VelocityContext withHeaders = buildMinimalContext();
        withHeaders.put("customConfigKey", "myClient");
        withHeaders.put("customStaticHeaders", List.of(staticHeader("X-Tenant", "demo", true)));
        withHeaders.put("customDynamicHeaders", List.of("com.example.HeaderProvider"));
        withHeaders.put("customOperationConfigs", Map.of());

        String renderedWithHeaders = render(withHeaders);
        assertTrue(renderedWithHeaders.contains("@prot.soap.StaticHeaders"));
        assertTrue(renderedWithHeaders.contains("@prot.soap.StaticHeader(name = \"X-Tenant\", value = \"demo\")"));
        assertTrue(renderedWithHeaders.contains("@prot.soap.DynamicHeaders"));
        assertTrue(renderedWithHeaders.contains("\"com.example.HeaderProvider\""));

        VelocityContext withoutHeaders = buildMinimalContext();
        withoutHeaders.put("customConfigKey", "myClient");
        withoutHeaders.put("customStaticHeaders", List.of());
        withoutHeaders.put("customDynamicHeaders", List.of());
        withoutHeaders.put("customOperationConfigs", Map.of());

        String renderedWithoutHeaders = render(withoutHeaders);
        assertFalse(renderedWithoutHeaders.contains("@prot.soap.StaticHeaders"));
        assertFalse(renderedWithoutHeaders.contains("@prot.soap.DynamicHeaders"));
    }

    // ---- helpers -------------------------------------------------------

    private VelocityContext buildMinimalContext() {
        VelocityContext ctx = new VelocityContext();
        ctx.put("intf", new StubIntf("com.example", "MyService", List.of()));
        ctx.put("markGenerated", "false");
        ctx.put("suppressGeneratedDate", "true");
        ctx.put("fullversion", "test");
        ctx.put("version", "test");
        ctx.put("currentdate", "today");
        ctx.put("seiSuperinterfaceString", "");
        return ctx;
    }

    private StaticHeader staticHeader(String name, String value, Boolean ifExisting) {
        StaticHeader header = new StaticHeader();
        header.setName(name);
        header.setValue(value);
        header.setIfExisting(ifExisting);
        return header;
    }

    private String render(VelocityContext ctx) throws Exception {
        StringWriter writer = new StringWriter();
        engine.mergeTemplate("prot-cxf-sei.vm", "UTF-8", ctx, writer);
        return writer.toString();
    }

    // ---- stub models ---------------------------------------------------

    public static class StubIntf {
        private final String packageName;
        private final String name;
        private final List<StubMethod> methods;

        public StubIntf(String packageName, String name, List<StubMethod> methods) {
            this.packageName = packageName;
            this.name = name;
            this.methods = methods;
        }

        public String getPackageName() { return packageName; }
        public String getName() { return name; }
        public List<String> getImports() { return List.of(); }
        public List<String> getAnnotations() { return List.of(); }
        public List<StubMethod> getMethods() { return methods; }
        public String getPackageJavaDoc() { return ""; }
        public String getClassJavaDoc() { return ""; }
    }

    public static class StubMethod {
        private final String name;

        public StubMethod(String name) { this.name = name; }

        public String getName() { return name; }
        public String getJavaDoc() { return ""; }
        public List<String> getAnnotations() { return List.of(); }
        public String getReturnValue() { return "void"; }
        public List<String> getParameterList() { return List.of(); }
        public List<StubException> getExceptions() { return List.of(); }
    }

    public static class StubException {
        public String getClassName() { return "Exception"; }
    }
}

