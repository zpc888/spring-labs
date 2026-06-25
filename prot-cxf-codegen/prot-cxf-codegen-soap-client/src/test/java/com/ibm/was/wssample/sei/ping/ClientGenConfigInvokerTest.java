package com.ibm.was.wssample.sei.ping;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientGenConfigInvokerTest {

    @Test
    void shouldGenerateCustomAnnotationsFromClientConfig() throws Exception {
        Path moduleDir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path generatedSei = moduleDir.resolve("target/generated-sources/com/ibm/was/wssample/sei/ping/PingServicePortType.java");
        assertTrue(Files.exists(generatedSei), "Expected generated SEI source to exist");
        String source = readString(generatedSei);

        assertTrue(source.contains("@prot.soap.SoapClient"));
        assertTrue(source.contains("@prot.soap.SoapAction"));
    }

    private static String readString(Path path) throws IOException {
        return Files.readString(path);
    }
}

