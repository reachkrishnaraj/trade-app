package com.kraj.tradeapp.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

@Slf4j
public class EnvironmentPreparedListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment environment = event.getEnvironment();

        // Test for environment variable availability
        String datasourceUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        String telegramBotToken = environment.getProperty("TELEGRAMBOT_TOKEN");
        String username = environment.getProperty("SPRING_DATASOURCE_USERNAME");
        String password = environment.getProperty("SPRING_DATASOURCE_PASSWORD");

        log.info("SPRING_DATASOURCE_URL: {}", datasourceUrl);
        log.info("SPRING_DATASOURCE_USERNAME: {}", username);
        log.info("SPRING_DATASOURCE_PASSWORD: {}", password);
        log.info("TELEGRAMBOT_TOKEN: {}", telegramBotToken);

        System.out.println("SPRING_DATASOURCE_URL: " + datasourceUrl);
        System.out.println("SPRING_DATASOURCE_USERNAME: " + username);
        System.out.println("SPRING_DATASOURCE_PASSWORD: " + password);
        System.out.println("TELEGRAMBOT_TOKEN: " + telegramBotToken);

        if (datasourceUrl == null || telegramBotToken == null) {
            throw new IllegalStateException("Critical environment variables are missing!");
        }
    }
}
