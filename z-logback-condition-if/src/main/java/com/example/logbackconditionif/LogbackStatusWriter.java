package com.example.logbackconditionif;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class LogbackStatusWriter {

    private static final String STATUS_FILE_PROPERTY = "status.file";

    private LogbackStatusWriter() {
    }

    static void write(LoggerContext loggerContext) {
        String statusFile = System.getProperty(STATUS_FILE_PROPERTY);
        if (statusFile == null || statusFile.isBlank()) {
            return;
        }

        StatusManager statusManager = loggerContext.getStatusManager();
        List<String> lines = new ArrayList<>();
        if (statusManager != null) {
            for (Status status : statusManager.getCopyOfStatusList()) {
                lines.add(status.toString());
            }
        }

        try {
            Files.write(Path.of(statusFile), lines);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write Logback status output", exception);
        }
    }
}
