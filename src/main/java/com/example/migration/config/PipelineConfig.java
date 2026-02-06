package com.example.migration.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipelineConfig {
    private final String name;
    private final ComponentConfig extractor;
    private final List<ComponentConfig> transformers;
    private final ComponentConfig loader;

    public PipelineConfig(String name, ComponentConfig extractor, List<ComponentConfig> transformers,
                          ComponentConfig loader) {
        this.name = name;
        this.extractor = extractor;
        this.transformers = new ArrayList<>(transformers);
        this.loader = loader;
    }

    public String getName() {
        return name;
    }

    public ComponentConfig getExtractor() {
        return extractor;
    }

    public List<ComponentConfig> getTransformers() {
        return Collections.unmodifiableList(transformers);
    }

    public ComponentConfig getLoader() {
        return loader;
    }
}
