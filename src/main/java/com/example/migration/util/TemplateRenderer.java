package com.example.migration.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateRenderer {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    public String render(String template, Map<String, Object> record) {
        if (template == null) {
            return null;
        }
        Matcher matcher = TOKEN_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String path = matcher.group(1).trim();
            Object value = RecordUtils.getPath(record, path);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value == null ? "" : value.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
