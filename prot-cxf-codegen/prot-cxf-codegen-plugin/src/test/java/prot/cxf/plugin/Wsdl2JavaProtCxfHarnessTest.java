package prot.cxf.plugin;

import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.WSDLToJava;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

class Wsdl2JavaProtCxfHarnessTest {

    @TempDir
    Path tempDir;

    @Test
    void configA_addsExpectedCustomAnnotations() throws Exception {
        Path scenarioDir = copyFixtureProject("scenario-a");

        runWsdlToJava(scenarioDir, "ping.wsdl", true);
        String source = readGeneratedSeiSource(scenarioDir, "PingServicePortType.java");

        Assertions.assertTrue(source.contains("@prot.soap.RegisteredSoapClient(\"pingClientA\")"));
        Assertions.assertTrue(source.contains("@prot.soap.StaticHeaders"));
        Assertions.assertTrue(source.contains("@prot.soap.DynamicHeaders"));
        Assertions.assertTrue(source.contains("@prot.soap.SoapAction(\"PingActionA\")"));
    }

    @Test
    void configB_addsDifferentCustomAnnotations() throws Exception {
        Path scenarioDir = copyFixtureProject("scenario-b");

        runWsdlToJava(scenarioDir, "ping.wsdl", true);
        String source = readGeneratedSeiSource(scenarioDir, "PingServicePortType.java");

        Assertions.assertTrue(source.contains("@prot.soap.RegisteredSoapClient(\"pingClientB\")"));
        Assertions.assertFalse(source.contains("@prot.soap.StaticHeaders"));
        Assertions.assertFalse(source.contains("@prot.soap.DynamicHeaders"));
        Assertions.assertTrue(source.contains("@prot.soap.SoapAction(\"pingOperation\")"));
    }

    @Test
    void noClientGenConfig_keepsBaselineWithoutCustomAnnotations() throws Exception {
        Path scenarioDir = copyFixtureProject("scenario-no-config");

        runWsdlToJava(scenarioDir, "ping.wsdl", false);
        String source = readGeneratedSeiSource(scenarioDir, "PingServicePortType.java");

        Assertions.assertTrue(source.contains("@prot.soap.RegisteredSoapClient(\"PingServicePortType\")"));
        Assertions.assertTrue(source.contains("@prot.soap.SoapAction(\"pingOperation\")"));
        Assertions.assertFalse(source.contains("@prot.soap.StaticHeaders"));
        Assertions.assertFalse(source.contains("@prot.soap.DynamicHeaders"));
    }

    @Test
    void calculatorConfigA_supportsConfiguredAndFallbackActionsWithHeaders() throws Exception {
        Path scenarioDir = copyFixtureProject("scenario-calculator-a");

        runWsdlToJava(scenarioDir, "calculator.wsdl", true);
        String source = readGeneratedSeiSource(scenarioDir, "CalculatorPortType.java");

        Assertions.assertTrue(source.contains("@prot.soap.RegisteredSoapClient(\"calculatorClientA\")"));
        Assertions.assertTrue(source.contains("@prot.soap.StaticHeaders"));
        Assertions.assertTrue(source.contains("@prot.soap.DynamicHeaders"));
        Assertions.assertTrue(source.contains("@prot.soap.SoapAction(\"AddConfiguredAction\")"));
        Assertions.assertTrue(containsAnySoapAction(source, "Divide", "divide"));
        Assertions.assertTrue(containsAnySoapAction(source, "Subtract", "subtract"));
        Assertions.assertTrue(containsAnySoapAction(source, "Multiply", "multiply"));
    }

    @Test
    void calculatorConfigB_emitsMandatoryAnnotationsWithoutOptionalHeaders() throws Exception {
        Path scenarioDir = copyFixtureProject("scenario-calculator-b");

        runWsdlToJava(scenarioDir, "calculator.wsdl", true);
        String source = readGeneratedSeiSource(scenarioDir, "CalculatorPortType.java");

        Assertions.assertTrue(source.contains("@prot.soap.RegisteredSoapClient(\"calculatorClientB\")"));
        Assertions.assertFalse(source.contains("@prot.soap.StaticHeaders"));
        Assertions.assertFalse(source.contains("@prot.soap.DynamicHeaders"));
        Assertions.assertTrue(source.contains("@prot.soap.SoapAction(\"AddActionB\")"));
        Assertions.assertTrue(containsAnySoapAction(source, "Divide", "divide"));
    }

    private void runWsdlToJava(Path scenarioDir, String wsdlName, boolean withClientGenConfig) throws Exception {
        MavenProjectStub project = new MavenProjectStub();
        Build build = new Build();
        build.setDirectory(scenarioDir.resolve("target").toString());
        project.setBuild(build);
        Assertions.assertNotNull(project.getBuild().getDirectory());

        Path outputDir = Path.of(project.getBuild().getDirectory(), "generated-sources");
        Path wsdl = scenarioDir.resolve("../../backend/soap/" + wsdlName).normalize();

        String[] args;
        if (withClientGenConfig) {
            Path config = scenarioDir.resolve("client-gen-config.yaml");
            args = new String[] {
                "-d", outputDir.toString(),
                "-fe", "prot-cxf",
                "-clientGenConfig", config.toString(),
                wsdl.toString()
            };
        } else {
            args = new String[] {
                "-d", outputDir.toString(),
                "-fe", "prot-cxf",
                wsdl.toString()
            };
        }

        new WSDLToJava(args).run(new ToolContext());
    }

    private String readGeneratedSeiSource(Path scenarioDir, String fileName) throws IOException {
        Path generatedSources = scenarioDir.resolve("target/generated-sources");
        Path sei = findFileByName(generatedSources, fileName);
        Assertions.assertTrue(Files.exists(sei), "Expected generated SEI source to exist: " + sei);
        return Files.readString(sei);
    }

    private Path copyFixtureProject(String scenarioName) throws IOException {
        Path sourceRoot = Path.of("src", "test", "resources").toAbsolutePath();
        Path targetRoot = tempDir.resolve("fixtures");
        copyDirectory(sourceRoot, targetRoot);
        return targetRoot.resolve("harness").resolve(scenarioName);
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(path -> {
                try {
                    Path relative = source.relativize(path);
                    Path destination = target.resolve(relative);
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.createDirectories(destination.getParent());
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw ex;
        }
    }

    private static Path findFileByName(Path root, String fileName) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .sorted(Comparator.comparing(Path::toString))
                    .findFirst()
                    .orElseThrow(() -> new IOException("Expected generated file not found: " + fileName));
        }
    }

    private static boolean containsAnySoapAction(String source, String... actionValues) {
        for (String actionValue : actionValues) {
            if (source.contains("@prot.soap.SoapAction(\"" + actionValue + "\")")) {
                return true;
            }
        }
        return false;
    }
}
