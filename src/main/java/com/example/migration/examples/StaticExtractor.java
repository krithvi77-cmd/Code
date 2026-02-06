package com.example.migration.examples;

import com.example.migration.core.Configurable;
import com.example.migration.core.Extractor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaticExtractor implements Extractor, Configurable {
    private int count = 1;

    @Override
    public void configure(Map<String, String> parameters) {
        if (parameters.containsKey("count")) {
            this.count = Integer.parseInt(parameters.get("count"));
        }
    }

    @Override
    public List<Map<String, Object>> extract() {
        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("id", i);
            record.put("value", "row-" + i);
            records.add(record);
        }
        return records;
    }
}
