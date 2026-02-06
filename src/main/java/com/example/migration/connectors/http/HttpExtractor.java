package com.example.migration.connectors.http;

import com.example.migration.core.Configurable;
import com.example.migration.core.Extractor;
import com.example.migration.util.ParameterUtils;
import com.example.migration.util.RecordUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpExtractor implements Extractor, Configurable {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Map<String, String> parameters = new LinkedHashMap<>();

    @Override
    public void configure(Map<String, String> parameters) {
        this.parameters = new LinkedHashMap<>(parameters);
    }

    @Override
    public List<Map<String, Object>> extract() {
        try {
            String url = ParameterUtils.getOrDefault(parameters, "url", "");
            String method = ParameterUtils.getOrDefault(parameters, "method", "GET").toUpperCase();
            String bodyTemplate = parameters.get("bodyTemplate");
            String recordsPointer = ParameterUtils.getOrDefault(parameters, "recordsPointer", "");
            String recordMode = ParameterUtils.getOrDefault(parameters, "recordMode", "array");

            Map<String, String> headers = ParameterUtils.withPrefix(parameters, "headers.");
            Map<String, String> queryParams = ParameterUtils.withPrefix(parameters, "query.");

            HttpAuthApplier authApplier = new HttpAuthApplier();
            HttpAuthApplier.AuthResult authResult = authApplier.apply(parameters, URI.create(url), headers, queryParams);
            String finalUrl = HttpAuthApplier.applyQueryParams(authResult.getUri(), authResult.getQueryParams());

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .timeout(Duration.ofSeconds(ParameterUtils.getIntOrDefault(parameters, "timeoutSeconds", 30)));

            for (Map.Entry<String, String> entry : authResult.getHeaders().entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }

            if ("GET".equals(method)) {
                builder.GET();
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(bodyTemplate == null ? "" : bodyTemplate));
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("HTTP extract failed with status " + response.statusCode());
            }

            JsonNode root = MAPPER.readTree(response.body());
            JsonNode recordsNode = recordsPointer.isBlank() ? root : root.at(recordsPointer);

            List<Map<String, Object>> records = new ArrayList<>();
            if (recordsNode.isMissingNode() || recordsNode.isNull()) {
                return records;
            }

            if (recordsNode.isArray()) {
                for (JsonNode node : recordsNode) {
                    Map<String, Object> record = MAPPER.convertValue(node, Map.class);
                    records.add(record);
                }
            } else if ("object".equalsIgnoreCase(recordMode)) {
                records.add(MAPPER.convertValue(recordsNode, Map.class));
            } else {
                records.add(RecordUtils.toMap(MAPPER.convertValue(recordsNode, Object.class)));
            }
            return records;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to extract data via HTTP", ex);
        }
    }
}
