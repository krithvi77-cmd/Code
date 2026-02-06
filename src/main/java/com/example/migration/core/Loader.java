package com.example.migration.core;

import java.util.List;
import java.util.Map;

public interface Loader {
    void load(List<Map<String, Object>> records);
}
