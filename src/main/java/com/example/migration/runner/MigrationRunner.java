package com.example.migration.runner;

import com.example.migration.config.MigrationConfig;
import com.example.migration.config.XmlMigrationConfigLoader;
import com.example.migration.core.MigrationEngine;
import java.io.File;

public class MigrationRunner {
    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: MigrationRunner <config.xml>");
        }
        File configFile = new File(args[0]);
        XmlMigrationConfigLoader loader = new XmlMigrationConfigLoader();
        MigrationConfig config = loader.load(configFile);
        new MigrationEngine().run(config);
    }
}
