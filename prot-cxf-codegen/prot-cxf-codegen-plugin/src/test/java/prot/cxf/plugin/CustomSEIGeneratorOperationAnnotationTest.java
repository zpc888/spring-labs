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
 * Tests that the prot-cxf-sei.vm template correctly emits per-operation annotations (FR-015).
 */
class CustomSEIGeneratorOperationAnnotationTest {

    private VelocityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        engine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        engine.init();
    }

    @Test
    void template_rendersOperationAnnotation_onlyForMatchingMethod() throws Exception {
        List<CustomSEIGeneratorSeiAnnotationTest.StubMethod> methods = List.of(
                new CustomSEIGeneratorSeiAnnotationTest.StubMethod("ping"),
                new CustomSEIGeneratorSeiAnnotationTest.StubMethod("echo")
        );

        VelocityContext ctx = buildContext(methods);
        ctx.put("customSeiAnnotations", Collections.emptyList());
        ctx.put("customOperationAnnotations", Map.of("ping", List.of("com.example.PingHandler")));

        String rendered = render(ctx);

        assertTrue(rendered.contains("@com.example.PingHandler"),
                "Rendered output should contain @com.example.PingHandler:\n" + rendered);

        // Find @com.example.PingHandler position and check it's near "ping" not "echo"
        int annotIdx = rendered.indexOf("@com.example.PingHandler");
        int pingIdx = rendered.indexOf("public void ping(");
        int echoIdx = rendered.indexOf("public void echo(");

        assertTrue(pingIdx >= 0, "Should contain ping method");
        assertTrue(echoIdx >= 0, "Should contain echo method");
        // Annotation should come before ping method signature
        assertTrue(annotIdx < pingIdx,
                "@com.example.PingHandler should appear before ping method declaration");
        // Annotation should NOT appear after echo or in echo's section
        // Check there's no second occurrence of the annotation after echo
        int secondAnnotIdx = rendered.indexOf("@com.example.PingHandler", annotIdx + 1);
        assertTrue(secondAnnotIdx < 0 || secondAnnotIdx < echoIdx,
                "@com.example.PingHandler should not appear in echo's section");
    }

    @Test
    void template_noOperationAnnotations_noneEmitted() throws Exception {
        List<CustomSEIGeneratorSeiAnnotationTest.StubMethod> methods = List.of(
                new CustomSEIGeneratorSeiAnnotationTest.StubMethod("ping"),
                new CustomSEIGeneratorSeiAnnotationTest.StubMethod("echo")
        );

        VelocityContext ctx = buildContext(methods);
        ctx.put("customSeiAnnotations", Collections.emptyList());
        ctx.put("customOperationAnnotations", Map.of());

        String rendered = render(ctx);

        assertFalse(rendered.contains("@com.example.PingHandler"),
                "No PingHandler annotation should appear when operation map is empty");
    }

    private VelocityContext buildContext(List<CustomSEIGeneratorSeiAnnotationTest.StubMethod> methods) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("intf", new CustomSEIGeneratorSeiAnnotationTest.StubIntf("com.example", "MyService", methods));
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
}

