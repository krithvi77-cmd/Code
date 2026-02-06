# Migration Framework

A configuration-driven ETL migration framework that executes pipelines defined entirely in XML.
It ships with generic HTTP-based connectors and a mapping transformer so developers can add new
sources/targets by editing XML only (no Java code changes).
Environment variables in XML values (for example `${DATADOG_API_KEY}`) are resolved at runtime.

## Concepts

- **Extractor**: Pulls source data into an in-memory list of records.
- **Transformer**: Applies mapping or enrichment to the records.
- **Loader**: Writes records to the destination system.
- **Configurable**: Optional interface for components that accept XML parameters.

## Included Components

- `HttpExtractor`: fetches data from any HTTP API (JSON response) with API key, bearer, basic, or
  OAuth2 client-credentials auth.
- `MappingTransformer`: maps fields by path to a new output shape.
- `HttpLoader`: sends records to any HTTP API with templated JSON bodies.

## XML Configuration

### Pipeline example (Datadog -> Site24x7)

```xml
<migration>
  <pipeline name="datadog-to-site24x7">
    <extractor class="com.example.migration.connectors.http.HttpExtractor">
      <param key="url">https://api.datadoghq.com/api/v1/monitor</param>
      <param key="method">GET</param>
      <param key="headers.DD-API-KEY">${DATADOG_API_KEY}</param>
      <param key="headers.DD-APPLICATION-KEY">${DATADOG_APP_KEY}</param>
      <param key="recordsPointer">/monitors</param>
    </extractor>

    <transformers>
      <transformer class="com.example.migration.core.MappingTransformer">
        <param key="mapping.displayName">name</param>
        <param key="mapping.type">type</param>
        <param key="mapping.query">query</param>
        <param key="mapping.tags">tags</param>
      </transformer>
    </transformers>

    <loader class="com.example.migration.connectors.http.HttpLoader">
      <param key="url">https://www.site24x7.com/api/monitors</param>
      <param key="method">POST</param>
      <param key="auth.type">apiKey</param>
      <param key="auth.apiKey.in">header</param>
      <param key="auth.apiKey.name">Authorization</param>
      <param key="auth.apiKey.value">Zoho-oauthtoken ${SITE24X7_TOKEN}</param>
      <param key="headers.Content-Type">application/json</param>
      <param key="bodyTemplate">{"display_name":"${displayName}","type":"${type}","query":"${query}","tags":${tags}}</param>
    </loader>
  </pipeline>
</migration>
```

### Pipeline example (Salesforce -> Zoho CRM)

```xml
<migration>
  <pipeline name="salesforce-to-zoho">
    <extractor class="com.example.migration.connectors.http.HttpExtractor">
      <param key="url">https://yourInstance.my.salesforce.com/services/data/v60.0/query</param>
      <param key="method">GET</param>
      <param key="auth.type">bearer</param>
      <param key="auth.bearer.token">${SALESFORCE_TOKEN}</param>
      <param key="query.q">SELECT Id, Name, Email FROM Contact</param>
      <param key="recordsPointer">/records</param>
    </extractor>

    <transformers>
      <transformer class="com.example.migration.core.MappingTransformer">
        <param key="mapping.Last_Name">Name</param>
        <param key="mapping.Email">Email</param>
      </transformer>
    </transformers>

    <loader class="com.example.migration.connectors.http.HttpLoader">
      <param key="url">https://www.zohoapis.com/crm/v2/Contacts</param>
      <param key="method">POST</param>
      <param key="auth.type">oauth2</param>
      <param key="auth.oauth.tokenUrl">https://accounts.zoho.com/oauth/v2/token</param>
      <param key="auth.oauth.clientId">${ZOHO_CLIENT_ID}</param>
      <param key="auth.oauth.clientSecret">${ZOHO_CLIENT_SECRET}</param>
      <param key="headers.Content-Type">application/json</param>
      <param key="bodyTemplate">{"data":[{"Last_Name":"${Last_Name}","Email":"${Email}"}]}</param>
    </loader>
  </pipeline>
</migration>
```

### Pipeline example (Custom app -> Custom app)

```xml
<migration>
  <pipeline name="custom-to-custom">
    <extractor class="com.example.migration.connectors.http.HttpExtractor">
      <param key="url">https://api.source-app.com/v1/items</param>
      <param key="method">GET</param>
      <param key="auth.type">apiKey</param>
      <param key="auth.apiKey.in">query</param>
      <param key="auth.apiKey.name">token</param>
      <param key="auth.apiKey.value">${SOURCE_TOKEN}</param>
      <param key="recordsPointer">/data</param>
    </extractor>

    <transformers>
      <transformer class="com.example.migration.core.MappingTransformer">
        <param key="mapping.item_id">id</param>
        <param key="mapping.description">details.description</param>
        <param key="mapping.status">literal:ACTIVE</param>
      </transformer>
    </transformers>

    <loader class="com.example.migration.connectors.http.HttpLoader">
      <param key="url">https://api.target-app.com/v2/items</param>
      <param key="method">POST</param>
      <param key="auth.type">basic</param>
      <param key="auth.basic.username">${TARGET_USER}</param>
      <param key="auth.basic.password">${TARGET_PASS}</param>
      <param key="headers.Content-Type">application/json</param>
      <param key="bodyTemplate">{"external_id":"${item_id}","description":"${description}","status":"${status}"}</param>
    </loader>
  </pipeline>
</migration>
```

## Running the Engine

```bash
mvn package
java -jar target/migration-framework-0.1.0.jar path/to/migration.xml
```

## Hardcoded Credentials (Local Testing Only)

If you need to run quickly without setting environment variables, use
`HardcodedMigrationRunner`, which sets credentials via system properties.
**Do not use this in production.**

```bash
mvn -q -DskipTests package
mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp target/classes:$(cat cp.txt) com.example.migration.runner.HardcodedMigrationRunner \
  src/main/resources/examples/datadog-site24x7-normalized.xml
```

## Example Configs

Sample configurations are available in `src/main/resources/examples`:

- `datadog-to-site24x7.xml`
- `datadog-site24x7-normalized.xml` (derived from normalization-rules.xml and site24x7 mappings)
- `salesforce-to-zoho.xml`
- `custom-to-custom.xml`

## Component Parameters

### HttpExtractor

| Parameter | Description |
| --- | --- |
| `url` | Endpoint URL. |
| `method` | HTTP method (default `GET`). |
| `headers.*` | Request headers. |
| `query.*` | Query parameters. |
| `recordsPointer` | JSON pointer to the array of records (e.g., `/data`). |
| `recordMode` | `array` (default) or `object`. |
| `auth.type` | `none`, `apiKey`, `bearer`, `basic`, `oauth2`. |

### MappingTransformer

| Parameter | Description |
| --- | --- |
| `mapping.<field>` | Source field path or `literal:<value>`. |
| `passThrough` | Include original fields (`true`/`false`). |

### HttpLoader

| Parameter | Description |
| --- | --- |
| `url` | Destination endpoint URL. |
| `method` | HTTP method (default `POST`). |
| `headers.*` | Request headers. |
| `query.*` | Query parameters. |
| `bodyTemplate` | JSON string with `${field}` tokens. |
| `auth.type` | `none`, `apiKey`, `bearer`, `basic`, `oauth2`. |

## Implementing Custom Components

You can still implement custom Java components when needed, but most migrations can be configured
entirely in XML using the built-in connectors and mapping transformer.
