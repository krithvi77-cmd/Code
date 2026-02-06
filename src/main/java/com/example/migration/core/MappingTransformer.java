package com.example.migration.core;

import com.example.migration.util.ParameterUtils;
import com.example.migration.util.RecordUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MappingTransformer implements Transformer, Configurable {
    private Map<String, String> parameters = new LinkedHashMap<>();

    @Override
    public void configure(Map<String, String> parameters) {
        this.parameters = new LinkedHashMap<>(parameters);
    }

    @Override
    public List<Map<String, Object>> transform(List<Map<String, Object>> records) {
        Map<String, String> mappings = ParameterUtils.withPrefix(parameters, "mapping.");
        boolean passThrough = ParameterUtils.getBooleanOrDefault(parameters, "passThrough", false);

        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> record : records) {
            Map<String, Object> output = new LinkedHashMap<>();
            for (Map.Entry<String, String> mapping : mappings.entrySet()) {
                Object value = resolveValue(record, mapping.getValue());
                output.put(mapping.getKey(), value);
            }
            if (passThrough) {
                output.putAll(record);
            }
            transformed.add(output);
        }
        return transformed;
    }

    private Object resolveValue(Map<String, Object> record, String expression) {
        if (expression == null) {
            return null;
        }
        if (expression.startsWith("literal:")) {
            return expression.substring("literal:".length());
        }
        return RecordUtils.getPath(record, expression);
    }
}
