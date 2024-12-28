package com.kraj.tradeapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfiguration {

    // Getters and Setters for the outer class
    private Api api;
    private Request request;

    @Setter
    @Getter
    public static class Api {

        // Getters and Setters
        private String key;
        private String url;
    }

    @Setter
    @Getter
    public static class Request {

        // Getters and Setters
        private String model;
        private double temperature;
        private int maxTokens;
        private String systemMessage;
    }
}
