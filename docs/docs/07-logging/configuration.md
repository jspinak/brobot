# Logging Configuration Guide

## Configuration Properties

The Brobot logging system is configured through standard Spring Boot application properties.

### Global Configuration

```properties
# Set the default level for all categories
# Values: OFF, ERROR, WARN, INFO, DEBUG, TRACE
brobot.logging.global-level=INFO
```

### Category-Specific Configuration

Override the global level for specific categories:

```properties
brobot.logging.categories.actions=DEBUG
brobot.logging.categories.transitions=INFO
brobot.logging.categories.matching=WARN
brobot.logging.categories.performance=INFO
brobot.logging.categories.state=DEBUG
brobot.logging.categories.lifecycle=INFO
brobot.logging.categories.validation=WARN
brobot.logging.categories.system=ERROR
```

### Output Format Configuration

```properties
# Format for log output
# SIMPLE - Human-readable console format
# STRUCTURED - Key-value pairs for log aggregation
# JSON - Machine-readable JSON format
brobot.logging.output.format=SIMPLE

# Include additional context
brobot.logging.output.include-timestamp=true
brobot.logging.output.include-thread=false
brobot.logging.output.include-correlation-id=true
brobot.logging.output.include-state-context=true
```

### Performance Configuration

```properties
# Enable async logging for better performance
brobot.logging.performance.async=false

# Buffer size for async logging (bytes)
brobot.logging.performance.buffer-size=8192
```

### Data Enrichment

```properties
# Control what additional data is included in logs
brobot.logging.enrichment.include-screenshots=false
brobot.logging.enrichment.include-similarity-scores=true
brobot.logging.enrichment.include-timing-breakdown=false
brobot.logging.enrichment.include-memory-usage=false
```

## Preset Configurations

Use presets for common scenarios:

```properties
# Available presets: PRODUCTION, DEVELOPMENT, TESTING, SILENT
brobot.logging.preset=DEVELOPMENT
```

### Preset Details

#### PRODUCTION
- Global level: WARN
- Format: JSON
- Async: true
- Minimal enrichment

#### DEVELOPMENT
- Global level: DEBUG
- Format: SIMPLE
- Async: false
- Full enrichment including screenshots

#### TESTING
- Global level: INFO
- Actions: DEBUG
- Matching: TRACE
- Focused on test execution

#### SILENT
- Global level: OFF
- No logging output

## SLF4J Backend Configuration

The Brobot logger delegates to SLF4J for actual output:

```properties
# Control console/file output via SLF4J
logging.level.io.github.jspinak.brobot.actions=INFO
logging.level.io.github.jspinak.brobot.transitions=INFO
logging.level.io.github.jspinak.brobot.matching=WARN
logging.level.io.github.jspinak.brobot.performance=INFO
```

## Example Configurations

### Minimal Action Logging
```properties
brobot.logging.global-level=WARN
brobot.logging.categories.actions=INFO
brobot.logging.output.format=SIMPLE
brobot.logging.enrichment.include-similarity-scores=false
```

### Verbose Debugging
```properties
brobot.logging.global-level=DEBUG
brobot.logging.output.format=STRUCTURED
brobot.logging.output.include-correlation-id=true
brobot.logging.enrichment.include-screenshots=true
brobot.logging.enrichment.include-timing-breakdown=true
```

### Production with Monitoring
```properties
brobot.logging.global-level=WARN
brobot.logging.categories.actions=INFO
brobot.logging.categories.performance=INFO
brobot.logging.output.format=JSON
brobot.logging.performance.async=true
brobot.logging.enrichment.include-memory-usage=true
```

## Programmatic Configuration

Configure logging at runtime:

```java
@Autowired
private LoggingConfiguration config;

// Apply a preset
config.applyPreset(LoggingPreset.DEVELOPMENT);

// Set specific levels
config.setGlobalLevel(LogLevel.DEBUG);
config.getCategories().put(LogCategory.ACTIONS, LogLevel.TRACE);

// Change output format
config.getOutput().setFormat(OutputFormat.JSON);
```