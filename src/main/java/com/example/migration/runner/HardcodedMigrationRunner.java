package com.example.migration.runner;

import com.example.migration.config.MigrationConfig;
import com.example.migration.config.XmlMigrationConfigLoader;
import com.example.migration.core.MigrationEngine;
import java.io.File;

/**
 * Example runner that hard-codes credentials via system properties.
 * Intended for quick local testing only.
 */
public class HardcodedMigrationRunner {
    public static void main(String[] args) {
        System.setProperty("DATADOG_API_KEY", "your_datadog_api_key");
        System.setProperty("DATADOG_APP_KEY", "your_datadog_app_key");
        System.setProperty("DATADOG_DOMAIN", "us5.datadoghq.com");
        System.setProperty("SITE24X7_TOKEN", "your_site24x7_token");

        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: HardcodedMigrationRunner <config.xml>");
        }

        File configFile = new File(args[0]);
        XmlMigrationConfigLoader loader = new XmlMigrationConfigLoader();
        MigrationConfig config = loader.load(configFile);
        new MigrationEngine().run(config);
    }
}
