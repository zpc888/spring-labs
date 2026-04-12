package com.example.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GatewayRandomPortsIntegrationTest {

    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(60);

    private TestCluster cluster;
    private HttpClient client;

    @BeforeAll
    void setUp() throws Exception {
        cluster = TestCluster.start();
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    @AfterAll
    void tearDown() {
        if (cluster != null) {
            cluster.stop();
        }
    }

    @Test
    void app1RouteShouldForwardToHelloDev1() throws Exception {
        HttpResponse<String> response = request("GET", "/app1/hello?name=alice", Map.of(), null);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("port: " + cluster.helloDev1Port), response.body());
        assertTrue(response.body().contains("name=alice"), response.body());
    }

    @Test
    void app2RouteShouldForwardToHelloDev2() throws Exception {
        HttpResponse<String> response = request("GET", "/app2/hello?name=bob", Map.of(), null);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("port: " + cluster.helloDev2Port), response.body());
        assertTrue(response.body().contains("name=bob"), response.body());
    }

    @Test
    void tenantRouteShouldMatchGoldAndAddLatencyHeader() throws Exception {
        HttpResponse<String> response = request(
                "GET",
                "/tenant/app2/hello?name=carol",
                Map.of("X-Tenant", "gold"),
                null
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("port: " + cluster.helloDev2Port), response.body());
        String latency = response.headers().firstValue("X-Latency-Ms").orElse("");
        assertTrue(!latency.isBlank() && latency.matches("\\d+"), "Expected numeric X-Latency-Ms, got: " + latency);
    }

    @Test
    void tenantRouteShouldRejectNonGoldTenant() throws Exception {
        HttpResponse<String> response = request(
                "GET",
                "/tenant/app2/hello?name=carol",
                Map.of("X-Tenant", "silver"),
                null
        );

        assertEquals(404, response.statusCode());
    }

    @Test
    void taskCrudShouldBeIsolatedBetweenDev1AndDev2() throws Exception {
        String unique = "task-" + UUID.randomUUID();
        String payload = "{\"id\":0,\"name\":\"" + unique + "\",\"description\":\"created-via-gateway\"}";

        HttpResponse<String> created = request(
                "POST",
                "/app1/tasks",
                Map.of("Content-Type", "application/json"),
                payload
        );

        assertEquals(200, created.statusCode(), created.body());
        assertTrue(created.body().contains(unique), created.body());

        HttpResponse<String> app1Tasks = request("GET", "/app1/tasks", Map.of(), null);
        HttpResponse<String> app2Tasks = request("GET", "/app2/tasks", Map.of(), null);

        assertEquals(200, app1Tasks.statusCode(), app1Tasks.body());
        assertEquals(200, app2Tasks.statusCode(), app2Tasks.body());
        assertTrue(app1Tasks.body().contains(unique), app1Tasks.body());
        assertTrue(!app2Tasks.body().contains(unique), app2Tasks.body());
    }

    private HttpResponse<String> request(String method, String path, Map<String, String> headers, String body)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(cluster.gatewayBaseUrl + path))
                .timeout(Duration.ofSeconds(10));

        headers.forEach(builder::header);

        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static final class TestCluster {
        private final int helloDev1Port;
        private final int helloDev2Port;
        private final int gatewayPort;
        private final String gatewayBaseUrl;
        private final Process helloDev1Process;
        private final Process helloDev2Process;
        private final Process gatewayProcess;
        private final Path logDir;

        private TestCluster(
                int helloDev1Port,
                int helloDev2Port,
                int gatewayPort,
                Process helloDev1Process,
                Process helloDev2Process,
                Process gatewayProcess,
                Path logDir
        ) {
            this.helloDev1Port = helloDev1Port;
            this.helloDev2Port = helloDev2Port;
            this.gatewayPort = gatewayPort;
            this.gatewayBaseUrl = "http://localhost:" + gatewayPort;
            this.helloDev1Process = helloDev1Process;
            this.helloDev2Process = helloDev2Process;
            this.gatewayProcess = gatewayProcess;
            this.logDir = logDir;
        }

        static TestCluster start() throws Exception {
            Path projectRoot = findProjectRoot();
            Path javaBin = Path.of(System.getProperty("java.home"), "bin", "java");
            Path helloJar = projectRoot.resolve("hello-service/build/libs/hello-service-1.0.0.jar");
            Path gatewayJar = projectRoot.resolve("gateway/build/libs/gateway-1.0.0.jar");

            if (!Files.exists(helloJar) || !Files.exists(gatewayJar)) {
                throw new IllegalStateException("Expected jars are missing. Run :hello-service:bootJar and :gateway:bootJar first.");
            }

            int helloDev1Port = randomPort();
            int helloDev2Port = randomPort();
            while (helloDev2Port == helloDev1Port) {
                helloDev2Port = randomPort();
            }
            int gatewayPort = randomPort();
            while (gatewayPort == helloDev1Port || gatewayPort == helloDev2Port) {
                gatewayPort = randomPort();
            }

            Path logDir = Files.createTempDirectory("gateway-it-");
            Process helloDev1 = startProcess(
                    projectRoot,
                    logDir.resolve("hello-dev1.log"),
                    javaBin.toString(),
                    "-jar",
                    helloJar.toString(),
                    "--spring.profiles.active=dev1",
                    "--server.port=" + helloDev1Port
            );
            waitForHealth(helloDev1Port, STARTUP_TIMEOUT, helloDev1, logDir.resolve("hello-dev1.log"));

            Process helloDev2 = startProcess(
                    projectRoot,
                    logDir.resolve("hello-dev2.log"),
                    javaBin.toString(),
                    "-jar",
                    helloJar.toString(),
                    "--spring.profiles.active=dev2",
                    "--server.port=" + helloDev2Port
            );
            waitForHealth(helloDev2Port, STARTUP_TIMEOUT, helloDev2, logDir.resolve("hello-dev2.log"));

            Path gatewayConfigPath = logDir.resolve("gateway-it.yml");
            writeGatewayConfig(gatewayConfigPath, gatewayPort, helloDev1Port, helloDev2Port);

            Process gateway = startProcess(
                    projectRoot,
                    logDir.resolve("gateway.log"),
                    javaBin.toString(),
                    "-jar",
                    gatewayJar.toString(),
                    "--spring.config.location=" + gatewayConfigPath.toUri()
            );
            waitForHealth(gatewayPort, STARTUP_TIMEOUT, gateway, logDir.resolve("gateway.log"));

            return new TestCluster(helloDev1Port, helloDev2Port, gatewayPort, helloDev1, helloDev2, gateway, logDir);
        }

        void stop() {
            stopProcess(gatewayProcess);
            stopProcess(helloDev2Process);
            stopProcess(helloDev1Process);
            deleteLogsQuietly();
        }

        private void deleteLogsQuietly() {
            try {
                if (Files.exists(logDir)) {
                    Files.walk(logDir)
                            .sorted((a, b) -> b.compareTo(a))
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                    // Ignore cleanup failures in tests.
                                }
                            });
                }
            } catch (IOException ignored) {
                // Ignore cleanup failures in tests.
            }
        }

        private static Process startProcess(Path workingDir, Path logPath, String... command) throws IOException {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(workingDir.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(logPath.toFile());
            return processBuilder.start();
        }

        private static void writeGatewayConfig(Path configPath, int gatewayPort, int helloDev1Port, int helloDev2Port)
                throws IOException {
            String yaml = "server:\n"
                    + "  port: " + gatewayPort + "\n"
                    + "\n"
                    + "spring:\n"
                    + "  application:\n"
                    + "    name: gateway\n"
                    + "  cloud:\n"
                    + "    gateway:\n"
                    + "      routes:\n"
                    + "        - id: app1-route\n"
                    + "          uri: http://localhost:" + helloDev1Port + "\n"
                    + "          predicates:\n"
                    + "            - Path=/app1/**\n"
                    + "          filters:\n"
                    + "            - StripPrefix=1\n"
                    + "        - id: app2-route\n"
                    + "          uri: http://localhost:" + helloDev2Port + "\n"
                    + "          predicates:\n"
                    + "            - Path=/app2/**\n"
                    + "          filters:\n"
                    + "            - StripPrefix=1\n"
                    + "        - id: tenant-app2-route\n"
                    + "          uri: http://localhost:" + helloDev2Port + "\n"
                    + "          predicates:\n"
                    + "            - Path=/tenant/app2/**\n"
                    + "            - TenantHeader=gold\n"
                    + "          filters:\n"
                    + "            - StripPrefix=2\n"
                    + "            - RequestTiming=X-Latency-Ms\n"
                    + "\n"
                    + "management:\n"
                    + "  endpoints:\n"
                    + "    web:\n"
                    + "      exposure:\n"
                    + "        include: health,info,metrics,prometheus\n";
            Files.writeString(configPath, yaml, StandardCharsets.UTF_8);
        }

        private static void waitForHealth(int port, Duration timeout, Process process, Path logPath) throws Exception {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            Instant deadline = Instant.now().plus(timeout);
            URI healthUri = URI.create("http://localhost:" + port + "/actuator/health");

            while (Instant.now().isBefore(deadline)) {
                ensureStillRunning(process, logPath);
                try {
                    HttpRequest request = HttpRequest.newBuilder(healthUri)
                            .timeout(Duration.ofSeconds(2))
                            .GET()
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200 && response.body().contains("UP")) {
                        return;
                    }
                } catch (IOException | InterruptedException ignored) {
                    // Retry until timeout.
                }
                Thread.sleep(200L);
            }

            throw new IllegalStateException("Timed out waiting for health endpoint on port " + port + "\n" + tail(logPath));
        }

        private static void ensureStillRunning(Process process, Path logPath) {
            if (!process.isAlive()) {
                throw new IllegalStateException("Process exited early.\n" + tail(logPath));
            }
        }

        private static String tail(Path logPath) {
            try {
                if (!Files.exists(logPath)) {
                    return "<log file missing: " + logPath + ">";
                }
                List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
                int from = Math.max(0, lines.size() - 120);
                return String.join("\n", lines.subList(from, lines.size()));
            } catch (IOException e) {
                return "<failed reading log: " + e.getMessage() + ">";
            }
        }

        private static void stopProcess(Process process) {
            if (process == null) {
                return;
            }
            process.destroy();
            try {
                if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }

        private static int randomPort() {
            try (ServerSocket socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to allocate random port", e);
            }
        }

        private static Path findProjectRoot() {
            Path current = Path.of("").toAbsolutePath();
            Path cursor = current;
            while (cursor != null) {
                if (Files.exists(cursor.resolve("settings.gradle"))) {
                    return cursor;
                }
                cursor = cursor.getParent();
            }
            throw new IllegalStateException("Could not find project root from " + current);
        }
    }
}

