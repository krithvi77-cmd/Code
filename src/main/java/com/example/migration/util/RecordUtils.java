package com.example.migration.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RecordUtils {
    private RecordUtils() {
    }

    @SuppressWarnings("unchecked")
    public static Object getPath(Map<String, Object> record, String path) {
        if (record == null || path == null || path.isBlank()) {
            return null;
        }
        String[] parts = path.split("\\.");
        Object current = record;
        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<String, Object>) current).get(part);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("value", value);
        return map;
    }
}
