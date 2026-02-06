package com.example.migration.config;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlMigrationConfigLoader {

    public MigrationConfig load(File xmlFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            Element root = document.getDocumentElement();
            List<PipelineConfig> pipelines = new ArrayList<>();

            NodeList pipelineNodes = root.getElementsByTagName("pipeline");
            for (int i = 0; i < pipelineNodes.getLength(); i++) {
                Element pipelineElement = (Element) pipelineNodes.item(i);
                pipelines.add(parsePipeline(pipelineElement));
            }

            return new MigrationConfig(pipelines);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load migration config: " + xmlFile, ex);
        }
    }

    private PipelineConfig parsePipeline(Element pipelineElement) {
        String name = pipelineElement.getAttribute("name");
        ComponentConfig extractor = parseComponent(singleChild(pipelineElement, "extractor"));
        ComponentConfig loader = parseComponent(singleChild(pipelineElement, "loader"));

        List<ComponentConfig> transformers = new ArrayList<>();
        Element transformersElement = singleChild(pipelineElement, "transformers");
        if (transformersElement != null) {
            NodeList transformerNodes = transformersElement.getElementsByTagName("transformer");
            for (int i = 0; i < transformerNodes.getLength(); i++) {
                transformers.add(parseComponent((Element) transformerNodes.item(i)));
            }
        }

        return new PipelineConfig(name, extractor, transformers, loader);
    }

    private ComponentConfig parseComponent(Element element) {
        if (element == null) {
            return null;
        }
        String className = element.getAttribute("class");
        Map<String, String> parameters = new LinkedHashMap<>();
        NodeList paramNodes = element.getElementsByTagName("param");
        for (int i = 0; i < paramNodes.getLength(); i++) {
            Element paramElement = (Element) paramNodes.item(i);
            String key = paramElement.getAttribute("key");
            String value = paramElement.getTextContent();
            parameters.put(key, value);
        }
        return new ComponentConfig(className, parameters);
    }

    private Element singleChild(Element parent, String name) {
        NodeList nodes = parent.getElementsByTagName(name);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getParentNode() == parent) {
                return (Element) node;
            }
        }
        return null;
    }
}
