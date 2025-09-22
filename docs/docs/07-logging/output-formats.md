# Output Formats

The Brobot logging system supports three output formats, each optimized for different use cases.

## SIMPLE Format

Human-readable format for development and debugging.

### Configuration
```properties
brobot.logging.output.format=SIMPLE
```

### Example Output
```
[ACTIONS] INFO  FIND submitButton → SUCCESS [25ms] loc:(100,200) sim:0.95
[ACTIONS] DEBUG   Match details: 3 matches found
[TRANSITIONS] INFO  MainMenu → Settings SUCCESS [150ms]
[PERFORMANCE] INFO  batch_process completed [5000ms] memory:50MB
[MATCHING] WARN  Low similarity match: 0.65
[SYSTEM] ERROR  Connection failed: timeout after 30s
```

### When to Use
- During development
- Manual debugging sessions
- Quick console monitoring
- Local testing

## STRUCTURED Format

Key-value format for log aggregation tools.

### Configuration
```properties
brobot.logging.output.format=STRUCTURED
brobot.logging.output.include-timestamp=true
brobot.logging.output.include-correlation-id=true
```

### Example Output
```
[2024-01-15T10:30:45.123] [session:a1b2c3d4] [ACTIONS] FIND submitButton | success=true duration=25 location=(100,200) similarity=0.95
[2024-01-15T10:30:45.148] [session:a1b2c3d4] [ACTIONS] CLICK submitButton | success=true duration=15
[2024-01-15T10:30:45.350] [session:a1b2c3d4] [TRANSITIONS] state_change | from=MainMenu to=Settings success=true duration=150
```

### Key-Value Pairs
- Easy to parse with tools like grep, awk, sed
- Compatible with log aggregation systems
- Maintains readability while being machine-parseable

### When to Use
- Test environments
- Log aggregation with tools like Splunk, ELK
- When you need both human and machine readability
- Debugging with correlation tracking

## JSON Format

Machine-readable format for automated processing and monitoring.

### Configuration
```properties
brobot.logging.output.format=JSON
brobot.logging.output.include-timestamp=true
brobot.logging.output.include-correlation-id=true
brobot.logging.output.include-state-context=true
```

### Example Output
```json
{
  "timestamp": "2024-01-15T10:30:45.123Z",
  "correlationId": "a1b2c3d4",
  "sessionId": "session_12345",
  "category": "ACTIONS",
  "level": "INFO",
  "message": "FIND submitButton → SUCCESS",
  "context": {
    "action_type": "FIND",
    "target": "submitButton",
    "success": true,
    "duration_ms": 25,
    "location": {
      "x": 100,
      "y": 200
    },
    "similarity": 0.95,
    "matches_found": 1,
    "state_context": "MainMenu"
  }
}
```

### Complex Event Example
```json
{
  "timestamp": "2024-01-15T10:30:50.456Z",
  "correlationId": "a1b2c3d4",
  "category": "PERFORMANCE",
  "level": "INFO",
  "message": "Batch processing completed",
  "context": {
    "operation": "batch_process",
    "duration_ms": 5000,
    "memory_used_bytes": 52428800,
    "items_processed": 150,
    "breakdown": {
      "initialization_ms": 100,
      "processing_ms": 4500,
      "cleanup_ms": 400
    },
    "throughput": 30.0,
    "peak_memory_mb": 75
  }
}
```

### When to Use
- Production environments
- Integration with monitoring tools (Datadog, New Relic, CloudWatch)
- Automated log analysis
- Metrics collection and alerting
- Long-term log storage

## Format Comparison

| Feature | SIMPLE | STRUCTURED | JSON |
|---------|--------|------------|------|
| Human Readable | ✅ Excellent | ✅ Good | ⚠️ Moderate |
| Machine Parseable | ❌ Poor | ✅ Good | ✅ Excellent |
| Storage Efficiency | ✅ High | ⚠️ Medium | ❌ Low |
| Tool Compatibility | ❌ Limited | ✅ Good | ✅ Excellent |
| Debugging Ease | ✅ Excellent | ✅ Good | ⚠️ Moderate |
| Production Ready | ❌ No | ✅ Yes | ✅ Yes |

## Custom Formatting

You can implement custom formatters by implementing the `LogFormatter` interface:

```java
@Component
public class CustomFormatter implements LogFormatter {
    @Override
    public String format(LogEntry entry) {
        // Custom formatting logic
        return String.format("[%s] %s: %s",
            entry.getCategory(),
            entry.getLevel(),
            entry.getMessage());
    }
}
```

Register your custom formatter in the configuration:

```java
@Bean
public BrobotLogger brobotLogger(LoggingConfiguration config) {
    LogFormatter formatter = new CustomFormatter();
    return new BrobotLoggerImpl(config, formatter, correlationContext);
}
```

## Output Examples by Category

### Actions Category
```
# SIMPLE
[ACTIONS] INFO  CLICK loginButton → SUCCESS [15ms] loc:(500,300)

# STRUCTURED
[ACTIONS] CLICK loginButton | success=true duration=15 location=(500,300)

# JSON
{"category":"ACTIONS","level":"INFO","action_type":"CLICK","target":"loginButton","success":true,"duration_ms":15}
```

### Transitions Category
```
# SIMPLE
[TRANSITIONS] INFO  MainMenu → Settings SUCCESS [200ms]

# STRUCTURED
[TRANSITIONS] state_change | from=MainMenu to=Settings success=true duration=200

# JSON
{"category":"TRANSITIONS","from":"MainMenu","to":"Settings","success":true,"duration_ms":200}
```

### Performance Category
```
# SIMPLE
[PERFORMANCE] INFO  batch_process [5000ms] items:150 throughput:30/s

# STRUCTURED
[PERFORMANCE] batch_process | duration=5000 items=150 throughput=30

# JSON
{"category":"PERFORMANCE","operation":"batch_process","duration_ms":5000,"items":150,"throughput":30}
```