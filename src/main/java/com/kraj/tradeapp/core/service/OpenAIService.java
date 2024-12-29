package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.config.OpenAIConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIService {

    private final OpenAIConfiguration openAIConfiguration;

    @Autowired
    public OpenAIService(OpenAIConfiguration openAIConfiguration) {
        this.openAIConfiguration = openAIConfiguration;
    }

    public String analyzeTradeSignal(String tradeData) {
        // Construct the API request body
        String apiUrl = openAIConfiguration.getApi().getUrl();
        String requestBody = getRequestBody(tradeData);

        // Set up headers for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAIConfiguration.getApi().getKey());
        headers.set("Content-Type", "application/json");

        // Create an HttpEntity with the body and headers
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Create a RestTemplate and make the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        // Return the response from OpenAI API
        return response.getBody();
    }

    private String getRequestBody(String tradeData) {
        String model = openAIConfiguration.getRequest().getModel();
        double temperature = openAIConfiguration.getRequest().getTemperature();
        int maxTokens = openAIConfiguration.getRequest().getMaxTokens();
        String systemMessage = openAIConfiguration.getRequest().getSystemMessage();

        String requestBody =
            "{" +
            "\"model\": \"" +
            model +
            "\"," +
            "\"messages\": [" +
            "{ \"role\": \"system\", \"content\": \"" +
            systemMessage +
            "\" }," +
            "{ \"role\": \"user\", \"content\": \"" +
            tradeData +
            "\" }" +
            "]," +
            "\"temperature\": " +
            temperature +
            "," +
            "\"max_tokens\": " +
            maxTokens +
            "}";
        return requestBody;
    }
}
