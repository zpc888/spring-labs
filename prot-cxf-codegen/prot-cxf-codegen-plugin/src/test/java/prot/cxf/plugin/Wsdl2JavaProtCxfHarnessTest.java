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
import java.util.stream.Stream;

class Wsdl2JavaProtCxfHarnessTest {

    @TempDir
    Path tempDir;

    @Test
    void configA_addsExpectedCustomAnnotations() throws Exception {
        Path scenarioDir = copyFixtureProject("scenario-a");

        runWsdlToJava(scenarioDir, true);
        String source = readGeneratedSeiSource(scenarioDir);

        Assertions.assertTrue(source.contains("@com.example.ConfigASei"));
        Assertions.assertTrue(source.contains("@com.example.ConfigAOperation"));
    }

    @Test
    void configB_addsDifferentCustomAnnotations() throws Exception {
        Path scenarioDir = copyFixtureProject("scenario-b");

        runWsdlToJava(scenarioDir, true);
        String source = readGeneratedSeiSource(scenarioDir);

        Assertions.assertTrue(source.contains("@com.example.ConfigBSei"));
        Assertions.assertTrue(source.contains("@com.example.ConfigBOperation"));
        Assertions.assertFalse(source.contains("@com.example.ConfigASei"));
        Assertions.assertFalse(source.contains("@com.example.ConfigAOperation"));
    }

    @Test
    void noClientGenConfig_keepsBaselineWithoutCustomAnnotations() throws Exception {
        Path scenarioDir = copyFixtureProject("scenario-no-config");

        runWsdlToJava(scenarioDir, false);
        String source = readGeneratedSeiSource(scenarioDir);

        Assertions.assertFalse(source.contains("@com.example.ConfigASei"));
        Assertions.assertFalse(source.contains("@com.example.ConfigBSei"));
        Assertions.assertFalse(source.contains("@com.example.ConfigAOperation"));
        Assertions.assertFalse(source.contains("@com.example.ConfigBOperation"));
    }

    private void runWsdlToJava(Path scenarioDir, boolean withClientGenConfig) throws Exception {
        MavenProjectStub project = new MavenProjectStub();
        Build build = new Build();
        build.setDirectory(scenarioDir.resolve("target").toString());
        project.setBuild(build);
        Assertions.assertNotNull(project.getBuild().getDirectory());

        Path outputDir = Path.of(project.getBuild().getDirectory(), "generated-sources");
        Path wsdl = scenarioDir.resolve("../../backend/soap/ping.wsdl").normalize();

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

    private String readGeneratedSeiSource(Path scenarioDir) throws IOException {
        Path sei = scenarioDir.resolve("target/generated-sources/com/ibm/was/wssample/sei/ping/PingServicePortType.java");
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
                        Files.copy(path, destination);
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
}
