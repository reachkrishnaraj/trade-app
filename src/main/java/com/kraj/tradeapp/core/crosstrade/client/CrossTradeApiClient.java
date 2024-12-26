package com.kraj.tradeapp.core.crosstrade.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraj.tradeapp.core.model.dto.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CrossTradeApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String secretKey;
    private final ObjectMapper objectMapper;
    private final HttpHeaders headers;

    public CrossTradeApiClient(
        RestTemplate restTemplate,
        @Value("${crosstrade.api.base-url}") String baseUrl,
        @Value("${crosstrade.api.secret-key}") String secretKey,
        ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
        this.headers = createHeaders();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public Account getAccount(String accountName) {
        String url = baseUrl + "/accounts/" + accountName;
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, Account.class);
    }

    public List<Account> getAccounts() {
        String url = baseUrl + "/accounts";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, "accounts", new TypeReference<List<Account>>() {});
    }

    // Positions
    public List<Position> getPositions(String accountId) {
        String url = baseUrl + "/accounts/" + accountId + "/positions";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, "positions", new TypeReference<List<Position>>() {});
    }

    public Position getPosition(String accountId, String instrument) {
        String url = baseUrl + "/accounts/" + accountId + "/position?account=" + accountId + "&instrument=" + instrument;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, Position.class);
    }

    public Position closePosition(String accountId, String instrument) {
        String url = baseUrl + "/accounts/" + accountId + "/positions/close?account=" + accountId;
        String body = "{\"instrument\":\"" + instrument + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, Position.class);
    }

    public OrderResponse reversePosition(String accountId, ReversePositionRequest request) {
        String url = baseUrl + "/v1/api/accounts/" + accountId + "/positions/reverse";
        HttpEntity<ReversePositionRequest> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<OrderResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, OrderResponse.class);
        return response.getBody();
    }

    public List<Position> flattenPositions(String accountId, String instrument) {
        String url = baseUrl + "/accounts/" + accountId + "/positions/flatten";
        String body = "{\"instrument\":\"" + instrument + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "closedPositions", new TypeReference<List<Position>>() {});
    }

    public List<Position> flattenEverything(String accountId) {
        String url = baseUrl + "/accounts/" + accountId + "/positions/flatten";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "closedPositions", new TypeReference<List<Position>>() {});
    }

    // GET /orders
    public List<Order> getOrders(String accountId) {
        String url = baseUrl + "/accounts/" + accountId + "/orders";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, "orders", new TypeReference<List<Order>>() {});
    }

    // GET /orders/{orderId}
    public Order getOrder(String accountId, String orderId) {
        String url = baseUrl + "/accounts/" + accountId + "/orders/" + orderId;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, Order.class);
    }

    // GET /orders/{orderId}/status
    public String getOrderStatus(String accountId, String orderId) {
        String url = baseUrl + "/accounts/" + accountId + "/orders/" + orderId + "/status";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, "status", new TypeReference<String>() {});
    }

    // POST /orders
    public String placeOrder(String accountId, PlaceOrderRequest request) {
        String url = baseUrl + "/v1/api/accounts/" + accountId + "/orders/place";
        HttpEntity<PlaceOrderRequest> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "orderId", new TypeReference<String>() {});
    }

    // POST /orders/flat
    public String placeFlatOrder(String accountId, PlaceOrderRequest request) {
        String url = baseUrl + "/v1/api/accounts/" + accountId + "/orders/flat";
        HttpEntity<PlaceOrderRequest> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "orderId", new TypeReference<String>() {});
    }

    // POST /orders/{orderId}/cancel
    public String cancelOrder(String accountId, String orderId) {
        String url = baseUrl + "/accounts/" + accountId + "/orders/" + orderId + "/cancel";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "orderId", new TypeReference<String>() {});
    }

    // POST /orders/cancel
    public List<String> cancelOrders(String accountId, String[] orderIds) {
        String url = baseUrl + "/accounts/" + accountId + "/orders/cancel";
        HttpEntity<String> entity = new HttpEntity<>(serialize(orderIds), createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "orderIds", new TypeReference<List<String>>() {});
    }

    // POST /orders/cancel-all
    public List<String> cancelAllOrders(String accountId) {
        String url = baseUrl + "/accounts/" + accountId + "/orders/cancelall";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "orderIds", new TypeReference<List<String>>() {});
    }

    // POST /orders/{orderId}/replace
    public String replaceOrder(String accountId, String orderId, PlaceOrderRequest newOrder) {
        String url = baseUrl + "/accounts/" + accountId + "/orders/" + orderId + "/replace";
        HttpEntity<PlaceOrderRequest> entity = new HttpEntity<>(newOrder, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "orderId", new TypeReference<String>() {});
    }

    // PUT /orders/{orderId}
    public String changeOrder(String accountId, String orderId, ChangeOrderRequest request) {
        String url = baseUrl + "/v1/api/accounts/" + accountId + "/orders/" + orderId + "/change";
        HttpEntity<ChangeOrderRequest> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        return parseResponse(response, "orderId", new TypeReference<String>() {});
    }

    // Strategies
    public List<Strategy> getStrategies(String accountId) {
        String url = baseUrl + "/accounts/" + accountId + "/strategies";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, "strategies", new TypeReference<List<Strategy>>() {});
    }

    public Strategy getStrategy(String accountId, String strategyId) {
        String url = baseUrl + "/accounts/" + accountId + "/strategies/" + strategyId;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, Strategy.class);
    }

    public String closeStrategy(String accountId, String strategyId) {
        String url = baseUrl + "/accounts/" + accountId + "/strategies/" + strategyId + "/close";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return parseResponse(response, "strategyId", new TypeReference<String>() {});
    }

    // Executions
    public List<Execution> getExecutions(String accountId) {
        String url = baseUrl + "/accounts/" + accountId + "/executions";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, "executions", new TypeReference<List<Execution>>() {});
    }

    public Execution getExecution(String accountId, String executionId) {
        String url = baseUrl + "/accounts/" + accountId + "/executions/" + executionId;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, Execution.class);
    }

    // Quotes
    public Quote getQuote(String accountId, String instrument) {
        String url =
            baseUrl + "/v1/api/accounts/" + accountId + "/quote?instrument=" + URLEncoder.encode(instrument, StandardCharsets.UTF_8);
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return parseResponse(response, Quote.class);
    }

    // Helper methods
    private <T> T parseResponse(ResponseEntity<String> response, Class<T> valueType) {
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                return objectMapper.readValue(response.getBody(), valueType);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse response", e);
            }
        } else {
            throw new RuntimeException("Request failed with status code: " + response.getStatusCode());
        }
    }

    private <T> T parseResponse(ResponseEntity<String> response, String rootField, TypeReference<T> valueTypeRef) {
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode targetNode = rootNode.get(rootField);
                if (targetNode == null) {
                    throw new RuntimeException("Root field '" + rootField + "' not found in response.");
                }
                return objectMapper.convertValue(targetNode, valueTypeRef);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse response", e);
            }
        } else {
            throw new RuntimeException("Request failed with status code: " + response.getStatusCode());
        }
    }

    private String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }
}
