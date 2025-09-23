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
// Level check happens first - no object creation if disabled
if (logger.shouldLog(LogCategory.ACTIONS, LogLevel.DEBUG)) {
    // Only build the log entry if it will be logged
    logger.debug(LogCategory.ACTIONS, "Expensive operation: {}",
        generateExpensiveReport());
}
```

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

### 4. Category-Specific Levels

Reduce noise by setting appropriate levels per package:

```properties
# Production configuration
logging.level.io.github.jspinak.brobot.action=INFO       # Only success/failure
logging.level.io.github.jspinak.brobot.matching=WARN     # Only problems
logging.level.io.github.jspinak.brobot.performance=INFO  # Key metrics only
logging.level.io.github.jspinak.brobot.validation=ERROR  # Only errors
```

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

### Enable Performance Metrics

```properties
logging.level.io.github.jspinak.brobot.performance=DEBUG
brobot.logging.enrichment.include-timing-breakdown=true
```

### Sample Performance Log

```json
{
  "category": "PERFORMANCE",
  "level": "DEBUG",
  "message": "Logging overhead analysis",
  "context": {
    "log_build_time_ms": 0.5,
    "format_time_ms": 0.2,
    "write_time_ms": 0.8,
    "total_overhead_ms": 1.5,
    "entries_per_second": 650
  }
}
```

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