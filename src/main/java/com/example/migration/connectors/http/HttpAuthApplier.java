package com.example.migration.connectors.http;

import com.example.migration.util.ParameterUtils;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

public class HttpAuthApplier {

    public AuthResult apply(Map<String, String> parameters, URI uri, Map<String, String> headers,
                            Map<String, String> queryParams) {
        String type = ParameterUtils.getOrDefault(parameters, "auth.type", "none");
        switch (type) {
            case "apiKey":
                return applyApiKey(parameters, uri, headers, queryParams);
            case "bearer":
                return applyBearer(parameters, uri, headers, queryParams);
            case "basic":
                return applyBasic(parameters, uri, headers, queryParams);
            case "oauth2":
                return applyOAuth(parameters, uri, headers, queryParams);
            default:
                return new AuthResult(uri, headers, queryParams);
        }
    }

    private AuthResult applyApiKey(Map<String, String> parameters, URI uri, Map<String, String> headers,
                                   Map<String, String> queryParams) {
        String name = ParameterUtils.getOrDefault(parameters, "auth.apiKey.name", "api_key");
        String value = ParameterUtils.getOrDefault(parameters, "auth.apiKey.value", "");
        String in = ParameterUtils.getOrDefault(parameters, "auth.apiKey.in", "header");
        if ("query".equalsIgnoreCase(in)) {
            queryParams.put(name, value);
        } else {
            headers.put(name, value);
        }
        return new AuthResult(uri, headers, queryParams);
    }

    private AuthResult applyBearer(Map<String, String> parameters, URI uri, Map<String, String> headers,
                                   Map<String, String> queryParams) {
        String token = ParameterUtils.getOrDefault(parameters, "auth.bearer.token", "");
        if (!token.isBlank()) {
            headers.put("Authorization", "Bearer " + token);
        }
        return new AuthResult(uri, headers, queryParams);
    }

    private AuthResult applyBasic(Map<String, String> parameters, URI uri, Map<String, String> headers,
                                  Map<String, String> queryParams) {
        String username = ParameterUtils.getOrDefault(parameters, "auth.basic.username", "");
        String password = ParameterUtils.getOrDefault(parameters, "auth.basic.password", "");
        if (!username.isBlank() || !password.isBlank()) {
            String token = java.util.Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            headers.put("Authorization", "Basic " + token);
        }
        return new AuthResult(uri, headers, queryParams);
    }

    private AuthResult applyOAuth(Map<String, String> parameters, URI uri, Map<String, String> headers,
                                  Map<String, String> queryParams) {
        OAuthTokenClient tokenClient = new OAuthTokenClient();
        String accessToken = tokenClient.fetchAccessToken(parameters);
        headers.put("Authorization", "Bearer " + accessToken);
        return new AuthResult(uri, headers, queryParams);
    }

    public static String applyQueryParams(URI uri, Map<String, String> queryParams) {
        if (queryParams.isEmpty()) {
            return uri.toString();
        }
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        String existing = uri.getQuery();
        StringBuilder builder = new StringBuilder(uri.toString());
        if (existing == null || existing.isBlank()) {
            builder.append("?").append(joiner);
        } else {
            builder.append("&").append(joiner);
        }
        return builder.toString();
    }

    public static class AuthResult {
        private final URI uri;
        private final Map<String, String> headers;
        private final Map<String, String> queryParams;

        public AuthResult(URI uri, Map<String, String> headers, Map<String, String> queryParams) {
            this.uri = uri;
            this.headers = headers;
            this.queryParams = queryParams;
        }

        public URI getUri() {
            return uri;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Map<String, String> getQueryParams() {
            return queryParams;
        }
    }
}
