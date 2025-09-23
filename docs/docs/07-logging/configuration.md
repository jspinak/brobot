# Logging Configuration Guide

## Configuration Properties

The Brobot logging system uses standard Spring Boot logging configuration for level control.

### Logging Level Configuration

Use standard Spring Boot properties to control logging levels:

```properties
# Set the root logging level
# Values: OFF, ERROR, WARN, INFO, DEBUG, TRACE
logging.level.root=INFO

# Control specific packages or classes
logging.level.io.github.jspinak.brobot=INFO
logging.level.io.github.jspinak.brobot.action=DEBUG
logging.level.io.github.jspinak.brobot.statemanagement=WARN
logging.level.com.bdo.automation=DEBUG  # Your application package
```

**Note:** The Brobot library uses standard SLF4J logging, so all Spring Boot logging features apply.

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
- Recommended level: `logging.level.root=WARN`
- Format: JSON
- Async: true
- Minimal enrichment

#### DEVELOPMENT
- Recommended level: `logging.level.root=DEBUG`
- Format: SIMPLE
- Async: false
- Full enrichment including screenshots

#### TESTING
- Recommended level: `logging.level.root=INFO`
- Additional: `logging.level.io.github.jspinak.brobot.action=DEBUG`
- Focused on test execution

#### SILENT
- Recommended level: `logging.level.root=OFF`
- No logging output

## Logback Configuration

The Brobot library includes a default logback-spring.xml that reduces verbosity for certain components. You can override these in your application.properties:

```properties
# Override specific Brobot components if needed
logging.level.io.github.jspinak.brobot.action.basic.find.FindPipeline=DEBUG
logging.level.io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver=INFO
```

## Example Configurations

### Minimal Action Logging
```properties
logging.level.root=WARN
logging.level.io.github.jspinak.brobot.action=INFO
brobot.logging.output.format=SIMPLE
brobot.logging.enrichment.include-similarity-scores=false
```

### Verbose Debugging
```properties
logging.level.root=DEBUG
logging.level.io.github.jspinak.brobot=DEBUG
brobot.logging.output.format=STRUCTURED
brobot.logging.output.include-correlation-id=true
brobot.logging.enrichment.include-screenshots=true
brobot.logging.enrichment.include-timing-breakdown=true
```

### Production with Monitoring
```properties
logging.level.root=WARN
logging.level.io.github.jspinak.brobot.action=INFO
logging.level.io.github.jspinak.brobot.performance=INFO
brobot.logging.output.format=JSON
brobot.logging.performance.async=true
brobot.logging.enrichment.include-memory-usage=true
```

## Image and History Saving Configuration

Control whether debug images and action history are saved to disk:

```properties
# Image saving is DISABLED by default to avoid filling disk space
# Set to true only when debugging or analyzing automation behavior
brobot.screenshot.save-history=false  # Default: false

# Configure where images are saved when enabled
brobot.screenshot.history-path=history/
brobot.screenshot.history-filename=hist

# Additional debug image settings
brobot.debug.image.enabled=false  # Default: false
brobot.debug.image.output-dir=debug/image-finding
```

**Important Notes:**
- **Images are NOT saved by default** - both `save-history` and `debug.image.enabled` default to `false`
- Enable image saving only when actively debugging to avoid disk space issues
- Images include action visualizations, match highlights, and search regions
- When enabled at INFO log level, you may see `[SIDEBAR]` and `[IMAGE_WRITE]` messages

### When to Enable Image Saving

Enable image saving in these scenarios:
- Debugging pattern matching issues
- Analyzing why actions fail
- Creating documentation of automation behavior
- Training new patterns

```properties
# Development/debugging configuration
brobot.screenshot.save-history=true
brobot.debug.image.enabled=true
logging.level.io.github.jspinak.brobot.tools.history.visual=DEBUG
logging.level.io.github.jspinak.brobot.util.image.io=DEBUG
```

## Programmatic Configuration

Configure output format and enrichment at runtime:

```java
@Autowired
private LoggingConfiguration config;

// Apply a preset for format and performance settings
config.applyPreset(LoggingPreset.DEVELOPMENT);

// Change output format
config.getOutput().setFormat(OutputFormat.JSON);

// Note: Logging levels are controlled via Spring Boot's
// LoggingSystem and cannot be changed via LoggingConfiguration
```