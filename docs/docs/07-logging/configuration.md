---
sidebar_position: 2
title: Configuration Guide
description: Comprehensive guide to configuring Brobot's logging system
---

# Logging Configuration Guide

## Configuration Properties

Brobot extends standard Spring Boot logging with custom formatters, enrichment features, and correlation tracking. You can use both standard Spring Boot logging properties (`logging.level.*`) and Brobot-specific properties for advanced features.

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

Brobot provides logging presets for common scenarios. Presets must be applied programmatically (they cannot be set via properties):

```properties
# Presets are applied programmatically, not via properties
# Use Spring profiles to trigger preset application:
spring.profiles.active=development  # Triggers DEVELOPMENT preset via LoggingPresetManager
```

**Available Presets**: PRODUCTION, DEVELOPMENT, TESTING, SILENT

**Note**: Presets configure output format, async performance, and enrichment settings. You must still configure Spring Boot logging levels separately via `logging.level.*` properties.

### Preset Details

**Important**: Presets configure output format, async settings, and enrichment. They do NOT automatically set logging levels - you must configure `logging.level.*` properties manually in `application.properties`.

#### PRODUCTION
- **Sets**: Format=JSON, Async=true, Screenshots=false
- **Recommended level** (set manually): `logging.level.root=WARN`
- **Use case**: Production environments with log aggregation

#### DEVELOPMENT
- **Sets**: Format=SIMPLE, Async=false, Screenshots=true
- **Recommended level** (set manually): `logging.level.root=DEBUG`
- **Use case**: Local development with full debugging

#### TESTING
- **Sets**: Format=STRUCTURED, Screenshots=false
- **Recommended level** (set manually): `logging.level.root=INFO` and `logging.level.io.github.jspinak.brobot.action=DEBUG`
- **Use case**: Automated test execution

#### SILENT
- **Sets**: Format=SIMPLE, minimal enrichment
- **Recommended level** (set manually): `logging.level.root=OFF`
- **Use case**: Suppressing all logging output

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
- **Images are NOT saved by default** - both `save-history` and `debug.image.enabled` default to `false` (set in `brobot-defaults.properties`)
- **Logging defaults** are defined in `brobot-logging-defaults.properties` (separate from main `brobot-defaults.properties`)
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
import io.github.jspinak.brobot.logging.LoggingConfiguration;
import io.github.jspinak.brobot.logging.LoggingConfiguration.LoggingPreset;
import io.github.jspinak.brobot.logging.LoggingConfiguration.OutputFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggingSetup {

    @Autowired
    private LoggingConfiguration config;

    public void configureLogging() {
        // Apply a preset for format and performance settings
        config.applyPreset(LoggingPreset.DEVELOPMENT);

        // Change output format
        config.getOutput().setFormat(OutputFormat.JSON);

        // Configure enrichment
        config.getEnrichment().setIncludeScreenshots(true);
        config.getEnrichment().setIncludeSimilarityScores(true);

        // Configure performance
        config.getPerformance().setAsync(false);

        // Note: Logging levels are controlled via Spring Boot's
        // LoggingSystem and cannot be changed via LoggingConfiguration
    }
}
```

**Alternative**: Use `LoggingPresetManager` with Spring profiles:

```java
import io.github.jspinak.brobot.logging.config.LoggingAutoConfiguration.LoggingPresetManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileBasedLogging {

    @Autowired
    private LoggingPresetManager presetManager;

    @Autowired
    private Environment environment;

    public void applyProfilePreset() {
        // Automatically applies preset based on active Spring profile
        presetManager.applyProfilePreset(environment.getActiveProfiles());
    }
}
```

## Related Documentation

### Logging System
- **[Logging Overview](index.md)** - Introduction to Brobot's logging system and key concepts
- **[Usage Guide](usage.md)** - How to use logging in your code with ActionConfig and custom messages
- **[Output Formats](output-formats.md)** - Detailed guide to SIMPLE, STRUCTURED, and JSON output formats
- **[Performance Guide](performance.md)** - Performance considerations and optimization strategies

### Configuration & Properties
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete reference for all Brobot configuration properties
- **[BrobotProperties Usage](../03-core-library/configuration/brobot-properties-usage.md)** - How to access and use configuration in your code
- **[Auto-Configuration Guide](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration and property loading

### Testing & Debugging
- **[Testing Introduction](../04-testing/testing-intro.md)** - Overview of Brobot testing capabilities
- **[Mock Mode Guide](../04-testing/mock-mode-guide.md)** - Testing without GUI using mock mode
- **[Profile-Based Testing](../04-testing/profile-based-testing.md)** - Using Spring profiles for test configuration
- **[Integration Testing](../04-testing/integration-testing.md)** - Full workflow testing patterns
- **[Debugging Pattern Matching](../04-testing/debugging-pattern-matching.md)** - Image debugging and capture configuration

### Action Configuration
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - Modern ActionConfig API introduction
- **[ActionConfig Reference](../03-core-library/action-config/05-reference.md)** - Complete API reference including all Options classes
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical examples of using ActionConfig

### Getting Started
- **[Quick Start Guide](../01-getting-started/quick-start.md)** - Getting started with Brobot
- **[Installation Guide](../01-getting-started/installation.md)** - Platform setup and dependencies
- **[AI Project Creation](../01-getting-started/ai-brobot-project-creation.md)** - Comprehensive project setup guide

### Advanced Topics
- **[AspectJ Usage Guide](../03-core-library/advanced/aspectj-usage-guide.md)** - Aspect-oriented programming with Brobot
- **[Advanced Illustration System](../03-core-library/advanced/advanced-illustration-system.md)** - Visual feedback and illustration features
- **[Persistence User Guide](../03-core-library/user-guides/persistence-user-guide.md)** - Persisting automation data and history

### External Resources
- **[Spring Boot Logging Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)** - Official Spring Boot logging guide
- **[Logback Documentation](https://logback.qos.ch/manual/)** - Logback configuration and usage
- **[SLF4J Documentation](https://www.slf4j.org/manual.html)** - SLF4J logging facade guide