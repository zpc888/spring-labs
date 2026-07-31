package com.example.logbackconditionif;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringBootLogbackApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootLogbackApp.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(SpringBootLogbackApp.class)
                .web(WebApplicationType.NONE)
                .run(args);

        LOGGER.info("spring-boot-logback-message");

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        LogbackStatusWriter.write(loggerContext);
        loggerContext.stop();
        context.close();
    }
}
