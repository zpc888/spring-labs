package com.example.logbackconditionif;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlainLogbackApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlainLogbackApp.class);

    private PlainLogbackApp() {
    }

    public static void main(String[] args) {
        LOGGER.info("plain-logback-message");

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        LogbackStatusWriter.write(loggerContext);
        loggerContext.stop();
    }
}
