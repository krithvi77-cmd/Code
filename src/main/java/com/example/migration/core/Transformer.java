package com.example.migration.core;

import java.util.List;
import java.util.Map;

public interface Transformer {
    List<Map<String, Object>> transform(List<Map<String, Object>> records);
}
