package com.example.migration.examples;

import com.example.migration.core.Transformer;
import java.util.List;
import java.util.Map;

public class PassThroughTransformer implements Transformer {
    @Override
    public List<Map<String, Object>> transform(List<Map<String, Object>> records) {
        return records;
    }
}
