package com.example.migration.core;

import com.example.migration.config.ComponentConfig;
import com.example.migration.config.MigrationConfig;
import com.example.migration.config.PipelineConfig;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MigrationEngine {

    public void run(MigrationConfig config) {
        for (PipelineConfig pipeline : config.getPipelines()) {
            runPipeline(pipeline);
        }
    }

    private void runPipeline(PipelineConfig pipeline) {
        if (pipeline.getExtractor() == null || pipeline.getLoader() == null) {
            throw new IllegalArgumentException("Pipeline must define extractor and loader: " + pipeline.getName());
        }
        Extractor extractor = instantiate(pipeline.getExtractor(), Extractor.class);
        List<Transformer> transformers = new ArrayList<>();
        for (ComponentConfig transformerConfig : pipeline.getTransformers()) {
            transformers.add(instantiate(transformerConfig, Transformer.class));
        }
        Loader loader = instantiate(pipeline.getLoader(), Loader.class);

        List<Map<String, Object>> records = extractor.extract();
        for (Transformer transformer : transformers) {
            records = transformer.transform(records);
        }
        loader.load(records);
    }

    private <T> T instantiate(ComponentConfig config, Class<T> expectedType) {
        try {
            Class<?> componentClass = Class.forName(config.getClassName());
            if (!expectedType.isAssignableFrom(componentClass)) {
                throw new IllegalArgumentException("Class " + config.getClassName()
                    + " does not implement " + expectedType.getSimpleName());
            }
            Constructor<?> constructor = componentClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            if (instance instanceof Configurable) {
                ((Configurable) instance).configure(config.getParameters());
            }
            return expectedType.cast(instance);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate component: " + config.getClassName(), ex);
        }
    }
}
