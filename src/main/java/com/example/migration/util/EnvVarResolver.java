package com.example.migration.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnvVarResolver {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private EnvVarResolver() {
    }

    public static String resolve(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = TOKEN_PATTERN.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String env = System.getenv(key);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(env == null ? "" : env));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
