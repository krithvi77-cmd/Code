package com.example.migration.examples;

import com.example.migration.core.Loader;
import java.util.List;
import java.util.Map;

public class ConsoleLoader implements Loader {
    @Override
    public void load(List<Map<String, Object>> records) {
        for (Map<String, Object> record : records) {
            System.out.println(record);
        }
    }
}
