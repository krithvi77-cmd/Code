package com.example.migration.connectors.http;

import com.example.migration.core.Configurable;
import com.example.migration.core.Loader;
import com.example.migration.util.ParameterUtils;
import com.example.migration.util.TemplateRenderer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpLoader implements Loader, Configurable {
    private Map<String, String> parameters = new LinkedHashMap<>();
    private final TemplateRenderer renderer = new TemplateRenderer();

    @Override
    public void configure(Map<String, String> parameters) {
        this.parameters = new LinkedHashMap<>(parameters);
    }

    @Override
    public void load(List<Map<String, Object>> records) {
        String url = ParameterUtils.getOrDefault(parameters, "url", "");
        String method = ParameterUtils.getOrDefault(parameters, "method", "POST").toUpperCase();
        String bodyTemplate = parameters.get("bodyTemplate");

        Map<String, String> headers = ParameterUtils.withPrefix(parameters, "headers.");
        Map<String, String> queryParams = ParameterUtils.withPrefix(parameters, "query.");

        HttpAuthApplier authApplier = new HttpAuthApplier();
        HttpClient client = HttpClient.newHttpClient();

        for (Map<String, Object> record : records) {
            Map<String, String> renderedHeaders = renderMap(headers, record);
            Map<String, String> renderedQueryParams = renderMap(queryParams, record);
            HttpAuthApplier.AuthResult authResult = authApplier.apply(parameters, URI.create(url),
                renderedHeaders, renderedQueryParams);
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
                String body = renderer.render(bodyTemplate, record);
                builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            }

            sendRequest(client, builder.build());
        }
    }

    private void sendRequest(HttpClient client, HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("HTTP load failed with status " + response.statusCode()
                    + ": " + response.body());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load data via HTTP", ex);
        }
    }

    private Map<String, String> renderMap(Map<String, String> values, Map<String, Object> record) {
        Map<String, String> rendered = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            rendered.put(entry.getKey(), renderer.render(entry.getValue(), record));
        }
        return rendered;
    }
}
