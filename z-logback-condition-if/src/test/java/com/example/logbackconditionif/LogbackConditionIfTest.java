package com.example.logbackconditionif;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogbackConditionIfTest {

    @Test
    void plainLogbackTopLevelConditionRoutesToFile1() throws Exception {
        Path tempDir = Files.createTempDirectory("logback-condition-if");
        Path configFile = tempDir.resolve("logback-control.xml");
        Path file1 = tempDir.resolve("file1.log");
        Path file2 = tempDir.resolve("file2.log");

        Files.writeString(configFile, topLevelConditionConfig(file1, file2));

        ProcessResult result = runJavaProcess(
                PlainLogbackApp.class.getName(),
                List.of(
                        "-Dlogback.configurationFile=" + configFile,
                        "-DTARGET=file1"
                ),
                List.of(),
                List.of()
        );

        assertEquals(0, result.exitCode());
        assertTrue(Files.exists(file1));
        assertFalse(Files.exists(file2));
        assertTrue(Files.readString(file1).contains("plain-logback-message"));
    }

    @Test
    void springBootTopLevelConditionRoutesToFile1() throws Exception {
        Path tempDir = Files.createTempDirectory("logback-condition-if");
        Path configFile = tempDir.resolve("logback-control.xml");
        Path file1 = tempDir.resolve("file1.log");
        Path file2 = tempDir.resolve("file2.log");

        Files.writeString(configFile, topLevelConditionConfig(file1, file2));

        ProcessResult result = runJavaProcess(
                SpringBootLogbackApp.class.getName(),
                List.of(
                        "-Dlogging.config=" + configFile,
                        "-DTARGET=file1"
                ),
                List.of(
                        "--spring.main.web-application-type=none",
                        "--spring.main.banner-mode=off"
                ),
                List.of()
        );

        assertEquals(0, result.exitCode());
        assertTrue(Files.exists(file1));
        assertFalse(Files.exists(file2));
        assertTrue(Files.readString(file1).contains("spring-boot-logback-message"));
    }

    @Test
    void plainLogbackNestedConditionInsideAppenderStillCreatesFile1ButReportsWarnings() throws Exception {
        Path tempDir = Files.createTempDirectory("logback-condition-if");
        Path configFile = tempDir.resolve("logback.xml");
        Path statusFile = tempDir.resolve("status.txt");
        Path file1 = tempDir.resolve("file1.log");
        Path file2 = tempDir.resolve("file2.log");

        Files.writeString(configFile, nestedAppenderConditionConfig(file1, file2));

        ProcessResult result = runJavaProcess(
                PlainLogbackApp.class.getName(),
                List.of(
                        "-Dstatus.file=" + statusFile,
                        "-DTARGET=file1"
                ),
                List.of(),
                List.of(tempDir)
        );

        assertEquals(0, result.exitCode(), result.output());
        assertTrue(Files.exists(file1), result.output());
        assertFalse(Files.exists(file2), result.output());
        assertTrue(Files.readString(file1).contains("plain-logback-message"));
        assertTrue(Files.exists(statusFile));
        assertTrue(Files.readString(statusFile).contains("<if> elements cannot be nested within an <appender>"));
    }

    @Test
    void springBootDefaultLoggingSystemWithLogbackSpringXmlStillCreatesFile1ButReportsWarnings() throws Exception {
        Path tempDir = Files.createTempDirectory("logback-condition-if");
        Path configFile = tempDir.resolve("logback-spring.xml");
        Path file1 = tempDir.resolve("file1.log");
        Path file2 = tempDir.resolve("file2.log");

        Files.writeString(configFile, nestedAppenderConditionConfig(file1, file2));

        ProcessResult result = runJavaProcess(
                SpringBootLogbackApp.class.getName(),
                List.of(
                        "-DTARGET=file1"
                ),
                List.of(
                        "--spring.main.web-application-type=none",
                        "--spring.main.banner-mode=off"
                ),
                List.of(tempDir)
        );

        assertEquals(0, result.exitCode(), result.output());
        assertTrue(Files.exists(file1), result.output());
        assertFalse(Files.exists(file2), result.output());
        assertTrue(Files.readString(file1).contains("spring-boot-logback-message"));
        assertTrue(result.output().contains("<if> elements cannot be nested within an <appender>"));
    }

    @Test
    void springBootWithLoggingSystemNoneAllowsNestedConditionInsideAppenderButReportsWarnings() throws Exception {
        Path tempDir = Files.createTempDirectory("logback-condition-if");
        Path configFile = tempDir.resolve("logback.xml");
        Path statusFile = tempDir.resolve("status.txt");
        Path file1 = tempDir.resolve("file1.log");
        Path file2 = tempDir.resolve("file2.log");

        Files.writeString(configFile, nestedAppenderConditionConfig(file1, file2));

        ProcessResult result = runJavaProcess(
                SpringBootLogbackApp.class.getName(),
                List.of(
                        "-Dorg.springframework.boot.logging.LoggingSystem=none",
                        "-Dstatus.file=" + statusFile,
                        "-DTARGET=file1"
                ),
                List.of(
                        "--spring.main.web-application-type=none",
                        "--spring.main.banner-mode=off"
                ),
                List.of(tempDir)
        );

        assertEquals(0, result.exitCode(), result.output());
        assertTrue(Files.exists(file1), result.output());
        assertFalse(Files.exists(file2), result.output());
        assertTrue(Files.readString(file1).contains("spring-boot-logback-message"));
        assertTrue(Files.exists(statusFile));
        assertTrue(Files.readString(statusFile).contains("<if> elements cannot be nested within an <appender>"));
    }

    private String topLevelConditionConfig(Path file1, Path file2) {
        return """
                <configuration>
                  <appender name="FILE1" class="ch.qos.logback.core.FileAppender">
                    <file>%s</file>
                    <append>false</append>
                    <encoder>
                      <pattern>%%msg%%n</pattern>
                    </encoder>
                  </appender>
                  <appender name="FILE2" class="ch.qos.logback.core.FileAppender">
                    <file>%s</file>
                    <append>false</append>
                    <encoder>
                      <pattern>%%msg%%n</pattern>
                    </encoder>
                  </appender>
                  <condition class="ch.qos.logback.core.boolex.ExpressionPropertyCondition">
                    <expression>propertyEquals("TARGET", "file1")</expression>
                  </condition>
                  <if>
                    <then>
                      <root level="INFO">
                        <appender-ref ref="FILE1"/>
                      </root>
                    </then>
                    <else>
                      <root level="INFO">
                        <appender-ref ref="FILE2"/>
                      </root>
                    </else>
                  </if>
                </configuration>
                """.formatted(file1.toAbsolutePath(), file2.toAbsolutePath());
    }

    private String nestedAppenderConditionConfig(Path file1, Path file2) {
        return """
                <configuration>
                  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
                    <condition class="ch.qos.logback.core.boolex.ExpressionPropertyCondition">
                      <expression>propertyEquals("TARGET", "file1")</expression>
                    </condition>
                    <if>
                      <then>
                        <file>%s</file>
                      </then>
                      <else>
                        <file>%s</file>
                      </else>
                    </if>
                    <append>false</append>
                    <encoder>
                      <pattern>%%msg%%n</pattern>
                    </encoder>
                  </appender>
                  <root level="INFO">
                    <appender-ref ref="FILE"/>
                  </root>
                </configuration>
                """.formatted(file1.toAbsolutePath(), file2.toAbsolutePath());
    }

    private ProcessResult runJavaProcess(String mainClass, List<String> jvmArgs, List<String> programArgs, List<Path> additionalClasspathEntries) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(buildClasspath(additionalClasspathEntries));
        command.add(mainClass);
        command.addAll(programArgs);

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        if (!process.waitFor(20, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("Child JVM timed out for " + mainClass);
        }

        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.exitValue();
        return new ProcessResult(exitCode, output);
    }

    private String buildClasspath(List<Path> additionalClasspathEntries) {
        String runtimeClasspath = System.getProperty("test.runtime.classpath");
        if (additionalClasspathEntries.isEmpty()) {
            return runtimeClasspath;
        }

        StringBuilder classpath = new StringBuilder();
        for (Path entry : additionalClasspathEntries) {
            classpath.append(entry.toAbsolutePath()).append(System.getProperty("path.separator"));
        }
        classpath.append(runtimeClasspath);
        return classpath.toString();
    }

    private record ProcessResult(int exitCode, String output) {
    }
}
