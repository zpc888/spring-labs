package prot.cxf.plugin;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the prot-cxf-sei.vm template correctly emits custom SEI-level annotations (FR-014).
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
    void template_rendersSeiAnnotation_beforeInterfaceDeclaration() throws Exception {
        VelocityContext ctx = buildMinimalContext();
        ctx.put("customSeiAnnotations", List.of("com.example.CustomAnnotation"));
        ctx.put("customOperationAnnotations", Map.of());

        String rendered = render(ctx);

        int annotationIdx = rendered.indexOf("@com.example.CustomAnnotation");
        int interfaceIdx = rendered.indexOf("public interface");
        assertTrue(annotationIdx >= 0, "Rendered output should contain @com.example.CustomAnnotation:\n" + rendered);
        assertTrue(interfaceIdx >= 0, "Rendered output should contain 'public interface'");
        assertTrue(annotationIdx < interfaceIdx,
                "@com.example.CustomAnnotation should appear before 'public interface'");
    }

    @Test
    void template_noSeiAnnotations_doesNotEmitAnnotation() throws Exception {
        VelocityContext ctx = buildMinimalContext();
        ctx.put("customSeiAnnotations", Collections.emptyList());
        ctx.put("customOperationAnnotations", Map.of());

        String rendered = render(ctx);

        assertFalse(rendered.contains("@com.example.CustomAnnotation"),
                "No annotation should appear when customSeiAnnotations is empty");
    }

    // ---- helpers -------------------------------------------------------

    private VelocityContext buildMinimalContext() {
        VelocityContext ctx = new VelocityContext();
        ctx.put("intf", new StubIntf("com.example", "MyService", Collections.emptyList()));
        ctx.put("markGenerated", "false");
        ctx.put("suppressGeneratedDate", "true");
        ctx.put("fullversion", "test");
        ctx.put("version", "test");
        ctx.put("currentdate", "today");
        ctx.put("seiSuperinterfaceString", "");
        return ctx;
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
        public List<String> getImports() { return Collections.emptyList(); }
        public List<String> getAnnotations() { return Collections.emptyList(); }
        public List<StubMethod> getMethods() { return methods; }
        public String getPackageJavaDoc() { return ""; }
        public String getClassJavaDoc() { return ""; }
    }

    public static class StubMethod {
        private final String name;

        public StubMethod(String name) { this.name = name; }

        public String getName() { return name; }
        public String getJavaDoc() { return ""; }
        public List<String> getAnnotations() { return Collections.emptyList(); }
        public String getReturnValue() { return "void"; }
        public List<String> getParameterList() { return Collections.emptyList(); }
        public List<StubException> getExceptions() { return Collections.emptyList(); }
    }

    public static class StubException {
        public String getClassName() { return "Exception"; }
    }
}

