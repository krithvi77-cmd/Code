# Migration Framework

A config-driven ETL migration framework that loads extractor/transformer/loader pipelines from XML
and executes them in Java. This is designed as a reusable JAR for other projects.

## Concepts

- **Extractor**: Pulls source data into an in-memory list of records.
- **Transformer**: Applies mapping or enrichment to the records.
- **Loader**: Writes records to the destination system.
- **Configurable**: Optional interface for components that accept XML parameters.

## XML Configuration

```xml
<migration>
  <pipeline name="customer-migration">
    <extractor class="com.yourco.migration.CustomerApiExtractor">
      <param key="baseUrl">https://api.example.com</param>
      <param key="token">abc123</param>
    </extractor>
    <transformers>
      <transformer class="com.yourco.migration.CustomerMappingTransformer">
        <param key="countryMapping">US=USA,UK=GBR</param>
      </transformer>
    </transformers>
    <loader class="com.yourco.migration.CustomerDbLoader">
      <param key="jdbcUrl">jdbc:postgresql://localhost:5432/app</param>
    </loader>
  </pipeline>
</migration>
```

## Running the Engine

```bash
mvn package
java -jar target/migration-framework-0.1.0.jar src/main/resources/examples/sample-migration.xml
```

## Implementing Components

Each component can optionally implement `Configurable` to receive XML parameters.

```java
public class CustomerApiExtractor implements Extractor, Configurable {
    @Override
    public void configure(Map<String, String> parameters) {
        // read API credentials
    }

    @Override
    public List<Map<String, Object>> extract() {
        // return records
    }
}
```

## Sample Components

The `com.example.migration.examples` package includes a static extractor, passthrough transformer,
and console loader used by the sample XML config.
