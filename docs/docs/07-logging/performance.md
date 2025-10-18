---
sidebar_position: 4
title: Performance Guide
description: Optimize Brobot logging performance with async configuration, memory management, and benchmarks
---

# Logging Performance Guide

## Performance Impact

The Brobot logging system is designed for minimal performance impact:

- **Disabled logging**: ~0 overhead (early filtering)
- **INFO level logging**: ~1-2ms per action
- **DEBUG level with enrichment**: ~3-5ms per action
- **With screenshots**: ~50-100ms per failed action

## Optimization Strategies

### 1. Early Filtering

The system checks log levels before building log entries:

```java
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;

public class PerformanceExample {
    private final BrobotLogger logger;

    public void performAction() {
        // Level check happens first - no object creation if disabled
        if (logger.isLoggingEnabled(LogCategory.ACTIONS, LogLevel.DEBUG)) {
            // Only build the log entry if it will be logged
            logger.debug(LogCategory.ACTIONS,
                "Expensive operation: " + generateExpensiveReport());
        }
    }

    private String generateExpensiveReport() {
        // Expensive operation
        return "Report data";
    }
}
```

> **Note**: The `isLoggingEnabled()` method in Brobot currently delegates all level checking to SLF4J/Logback. This means actual filtering happens at the SLF4J level based on `logging.level.*` properties. The pattern above is still recommended to avoid expensive string building operations when logging is disabled.

### 2. Async Logging

Enable async logging for production:

```properties
# Async logging improves throughput
brobot.logging.performance.async=true
brobot.logging.performance.buffer-size=16384
```

Benefits:
- Non-blocking log writes
- Better application throughput
- Reduced I/O contention

Trade-offs:
- Potential log loss on crash
- Slightly delayed log visibility
- Additional memory usage

### 3. Selective Enrichment

Only enable enrichment you need:

```properties
# Minimal enrichment for production
brobot.logging.enrichment.include-screenshots=false
brobot.logging.enrichment.include-timing-breakdown=false
brobot.logging.enrichment.include-memory-usage=false
brobot.logging.enrichment.include-similarity-scores=true  # Low overhead
```

> **Note**: `include-timing-breakdown` defaults to `true` in source code, but is typically disabled in production for optimal performance. The default enables comprehensive timing data during development, which can then be selectively disabled for deployment.

### 4. Category-Specific Levels

Reduce noise by setting appropriate levels per package:

```properties
# Production configuration
logging.level.io.github.jspinak.brobot.action=INFO       # Only success/failure
logging.level.io.github.jspinak.brobot.matching=WARN     # Only problems
logging.level.io.github.jspinak.brobot.performance=INFO  # Key metrics only
logging.level.io.github.jspinak.brobot.validation=ERROR  # Only errors
```

**Available Log Levels** (in order of severity):
- **OFF** - Disable all logging (maximum performance)
- **ERROR** - Only errors requiring attention
- **WARN** - Warnings and potential issues
- **INFO** - Key events and action results
- **DEBUG** - Detailed debugging information
- **TRACE** - Most detailed information (highest overhead)

## Memory Management

### Buffer Sizes

Configure buffer sizes based on load:

```properties
# Light load (< 100 actions/min)
brobot.logging.performance.buffer-size=8192

# Medium load (100-1000 actions/min)
brobot.logging.performance.buffer-size=32768

# Heavy load (> 1000 actions/min)
brobot.logging.performance.buffer-size=65536
```

### Memory Usage Estimates

| Configuration | Memory per Thread | Buffer Memory | Total (10 threads) |
|--------------|------------------|---------------|-------------------|
| Minimal | ~1KB | 8KB | ~18KB |
| Standard | ~2KB | 32KB | ~52KB |
| Full Enrichment | ~5KB | 64KB | ~114KB |

## Benchmarks

### Action Logging Performance

Test setup: 1000 find operations, averaged over 10 runs

| Log Level | Sync (ms/action) | Async (ms/action) |
|-----------|-----------------|-------------------|
| OFF | 0.0 | 0.0 |
| ERROR | 0.1 | 0.05 |
| INFO | 1.2 | 0.3 |
| DEBUG | 2.5 | 0.8 |
| TRACE | 4.1 | 1.5 |

### With Enrichment

| Feature | Additional Overhead |
|---------|-------------------|
| Similarity scores | +0.1ms |
| Timing breakdown | +0.5ms |
| Memory usage | +1.0ms |
| Screenshots (on failure) | +50-100ms |

## Best Practices for Performance

### 1. Production Configuration

```properties
# Optimized for production
logging.level.root=WARN
logging.level.io.github.jspinak.brobot.action=INFO
logging.level.io.github.jspinak.brobot.performance=INFO
brobot.logging.output.format=JSON
brobot.logging.performance.async=true
brobot.logging.performance.buffer-size=32768
brobot.logging.enrichment.include-screenshots=false
brobot.logging.enrichment.include-timing-breakdown=false
```

### 2. Development Configuration

```properties
# Full visibility for development
logging.level.root=DEBUG
logging.level.io.github.jspinak.brobot=DEBUG
brobot.logging.output.format=SIMPLE
brobot.logging.performance.async=false
brobot.logging.enrichment.include-screenshots=true
brobot.logging.enrichment.include-timing-breakdown=true
```

### 3. High-Performance Configuration

```properties
# Maximum performance
logging.level.root=ERROR
brobot.logging.performance.async=true
brobot.logging.performance.buffer-size=65536
brobot.logging.output.format=JSON
# Disable all enrichment
brobot.logging.enrichment.include-screenshots=false
brobot.logging.enrichment.include-similarity-scores=false
brobot.logging.enrichment.include-timing-breakdown=false
brobot.logging.enrichment.include-memory-usage=false
```

## Monitoring Logging Performance

### Enable Performance Logging

To monitor application performance (not logging system overhead):

```properties
logging.level.io.github.jspinak.brobot.performance=DEBUG
brobot.logging.enrichment.include-timing-breakdown=true
```

This enables logging of:
- Action execution times
- State transition durations
- Pattern matching performance
- Memory usage statistics

> **Note**: Brobot currently logs application performance metrics (action durations, memory usage) but does not yet track logging system overhead itself (log formatting time, write time, etc.). The performance benchmarks in this document are based on external measurements.

## Troubleshooting Performance Issues

### Symptoms: High CPU Usage

**Cause**: Too much DEBUG/TRACE logging
**Solution**: Reduce log levels
```properties
logging.level.root=INFO
logging.level.io.github.jspinak.brobot=WARN
```

### Symptoms: Memory Growth

**Cause**: Large async buffers with slow I/O
**Solution**: Reduce buffer size or disable async
```properties
brobot.logging.performance.async=false
# OR
brobot.logging.performance.buffer-size=8192
```

### Symptoms: Slow Action Execution

**Cause**: Screenshot capture on every action
**Solution**: Disable screenshots
```properties
brobot.logging.enrichment.include-screenshots=false
```

### Symptoms: Log File Growth

**Cause**: TRACE level or JSON format
**Solution**: Use appropriate levels and consider log rotation
```properties
logging.level.root=INFO
# Configure logback for rotation
```

## Related Documentation

### Logging System
- **[Logging Overview](index.md)** - Introduction to Brobot's transparent, configuration-driven logging system
- **[Logging Configuration Guide](configuration.md)** - Comprehensive configuration options for output formats, enrichment, and performance settings
- **[Logging Usage Guide](usage.md)** - How to use custom logging with ActionConfig methods and session management
- **[Output Formats](output-formats.md)** - Detailed guide to SIMPLE, STRUCTURED, and JSON output formats with performance implications

### Configuration & Properties
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete reference for all `brobot.logging.performance.*` and `brobot.logging.enrichment.*` properties
- **[BrobotProperties Usage Guide](../03-core-library/configuration/brobot-properties-usage.md)** - How to access logging properties programmatically
- **[Auto-Configuration Guide](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration and logging system initialization
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Logging in headless environments and performance considerations

### Testing & Debugging
- **[Testing Introduction](../04-testing/testing-intro.md)** - Testing with logging enabled and performance expectations
- **[Mock Mode Guide](../04-testing/mock-mode-guide.md)** - Mock mode has different logging performance characteristics (0.01-0.04s vs real timing)
- **[Profile-Based Testing](../04-testing/profile-based-testing.md)** - Using test profiles to configure logging levels and performance settings
- **[CI/CD Testing](../04-testing/advanced/ci-cd-testing.md)** - Logging performance in CI/CD environments

### Action Configuration
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - ActionConfig logging methods and their performance impact
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical examples of ActionConfig with custom logging

### Getting Started
- **[Quick Start Guide](../01-getting-started/quick-start.md)** - Basic logging setup and configuration
- **[Installation Guide](../01-getting-started/installation.md)** - Installation considerations

### Advanced Topics
- **[Builder Performance Guide](../03-core-library/advanced/builder-performance-guide.md)** - Optimizing ActionConfig builder usage to reduce logging overhead
- **[Image Find Debugging](../03-core-library/tools/image-find-debugging.md)** - Debug features with performance impact considerations

### Integration
- **[MCP Server Configuration](../06-integrations/mcp-server/configuration.md)** - Logging configuration for integrations and external monitoring