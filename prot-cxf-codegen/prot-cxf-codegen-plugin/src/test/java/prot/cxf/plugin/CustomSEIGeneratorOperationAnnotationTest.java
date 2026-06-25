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
 * Tests that the prot-cxf-sei.vm template emits mandatory per-operation SoapAction annotations.
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
        CustomSEIGeneratorSeiAnnotationTest.StubMethod ping = new CustomSEIGeneratorSeiAnnotationTest.StubMethod("ping");
        ping.setSoapAction("wsdlPingAction");
        CustomSEIGeneratorSeiAnnotationTest.StubMethod echo = new CustomSEIGeneratorSeiAnnotationTest.StubMethod("echo");
        echo.setSoapAction("wsdlEchoAction");
        List<CustomSEIGeneratorSeiAnnotationTest.StubMethod> methods = List.of(ping, echo);

        VelocityContext ctx = buildContext(methods);
        ctx.put("customConfigKey", "client");
        ctx.put("customBaseUrl", null);
        ctx.put("customJaxbContextPaths", List.of());
        ctx.put("customStaticHeaders", List.of());
        ctx.put("customStaticHeadersAsStrings", List.of());
        ctx.put("customDynamicHeaders", List.of());
        OperationConfig operationConfig = new OperationConfig();
        operationConfig.setAction("customPingAction");
        operationConfig.setStaticHeaders(List.of(staticHeader("X-Op", "ping", null)));
        operationConfig.setDynamicHeaders(List.of("com.example.OpHeader"));
        ctx.put("customOperationConfigs", Map.of("ping", operationConfig));

        String rendered = render(ctx);

        assertTrue(rendered.contains("@prot.soap.SoapAction(\"customPingAction\")"),
                "Rendered output should contain configured ping SoapAction:\n" + rendered);
        assertTrue(rendered.contains("@prot.soap.SoapAction(\"wsdlEchoAction\")"),
                "Rendered output should contain wsdl fallback echo SoapAction:\n" + rendered);
        assertTrue(rendered.contains("@prot.soap.SoapMethodHeader("));
        assertTrue(rendered.contains("\"X-Op=ping\""));
        assertTrue(rendered.contains("com.example.OpHeader.class"));

        int annotIdx = rendered.indexOf("@prot.soap.SoapAction(\"customPingAction\")");
        int pingIdx = rendered.indexOf("public void ping(");

        assertTrue(pingIdx >= 0, "Should contain ping method");
        assertTrue(annotIdx < pingIdx,
                "Configured SoapAction should appear before ping method declaration");
    }

    @Test
    void template_blankOperationAction_usesMethodNameFallback() throws Exception {
        CustomSEIGeneratorSeiAnnotationTest.StubMethod ping = new CustomSEIGeneratorSeiAnnotationTest.StubMethod("ping");
        CustomSEIGeneratorSeiAnnotationTest.StubMethod echo = new CustomSEIGeneratorSeiAnnotationTest.StubMethod("echo");
        List<CustomSEIGeneratorSeiAnnotationTest.StubMethod> methods = List.of(ping, echo);

        VelocityContext ctx = buildContext(methods);
        ctx.put("customConfigKey", "client");
        ctx.put("customBaseUrl", null);
        ctx.put("customJaxbContextPaths", List.of());
        ctx.put("customStaticHeaders", List.of());
        ctx.put("customStaticHeadersAsStrings", List.of());
        ctx.put("customDynamicHeaders", List.of());
        OperationConfig operationConfig = new OperationConfig();
        operationConfig.setAction("   ");
        ctx.put("customOperationConfigs", Map.of("ping", operationConfig));

        String rendered = render(ctx);

        assertTrue(rendered.contains("@prot.soap.SoapAction(\"ping\")"),
                "Blank configured action should fallback to method name");
        assertTrue(rendered.contains("@prot.soap.SoapAction(\"echo\")"),
                "Unconfigured operation should fallback to method name");
    }

    private StaticHeader staticHeader(String name, String value, Boolean ifExisting) {
        StaticHeader header = new StaticHeader();
        header.setName(name);
        header.setValue(value);
        header.setIfExisting(ifExisting);
        return header;
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

