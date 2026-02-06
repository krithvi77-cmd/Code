package com.example.migration.connectors.http;

import com.example.migration.util.ParameterUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class OAuthTokenClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String cachedToken;

    public String fetchAccessToken(Map<String, String> parameters) {
        if (cachedToken != null) {
            return cachedToken;
        }
        try {
            String tokenUrl = ParameterUtils.getOrDefault(parameters, "auth.oauth.tokenUrl", "");
            String clientId = ParameterUtils.getOrDefault(parameters, "auth.oauth.clientId", "");
            String clientSecret = ParameterUtils.getOrDefault(parameters, "auth.oauth.clientSecret", "");
            String scope = ParameterUtils.getOrDefault(parameters, "auth.oauth.scope", "");

            Map<String, String> form = new LinkedHashMap<>();
            form.put("grant_type", "client_credentials");
            form.put("client_id", clientId);
            form.put("client_secret", clientSecret);
            if (!scope.isBlank()) {
                form.put("scope", scope);
            }

            StringJoiner joiner = new StringJoiner("&");
            for (Map.Entry<String, String> entry : form.entrySet()) {
                joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(joiner.toString()))
                .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Token endpoint returned " + response.statusCode());
            }
            JsonNode node = MAPPER.readTree(response.body());
            String token = node.path("access_token").asText();
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("Token endpoint response missing access_token");
            }
            cachedToken = token;
            return token;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to fetch OAuth2 token", ex);
        }
    }
}
