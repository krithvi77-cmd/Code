package com.example.migration.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MigrationConfig {
    private final List<PipelineConfig> pipelines;

    public MigrationConfig(List<PipelineConfig> pipelines) {
        this.pipelines = new ArrayList<>(pipelines);
    }

    public List<PipelineConfig> getPipelines() {
        return Collections.unmodifiableList(pipelines);
    }
}
