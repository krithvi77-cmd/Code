package com.example.migration.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ParameterUtils {
    private ParameterUtils() {
    }

    public static Map<String, String> withPrefix(Map<String, String> parameters, String prefix) {
        Map<String, String> scoped = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                scoped.put(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        return scoped;
    }

    public static String getOrDefault(Map<String, String> parameters, String key, String defaultValue) {
        String value = parameters.get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static int getIntOrDefault(Map<String, String> parameters, String key, int defaultValue) {
        String value = parameters.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public static boolean getBooleanOrDefault(Map<String, String> parameters, String key, boolean defaultValue) {
        String value = parameters.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}
