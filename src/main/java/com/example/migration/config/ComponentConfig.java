package com.example.migration.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ComponentConfig {
    private final String className;
    private final Map<String, String> parameters;

    public ComponentConfig(String className, Map<String, String> parameters) {
        this.className = className;
        this.parameters = new LinkedHashMap<>(parameters);
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
}
