---
sidebar_position: 10
---

# Logging Architecture

Technical architecture documentation for the Brobot Unified Logging System.

## System Overview

The Unified Logging System consolidates three previously separate logging mechanisms into a cohesive architecture with clear separation of concerns and extensible design.

```
┌─────────────────────────────────────────────────────────────────┐
│                         BrobotLogger                             │
│  (Unified Facade - Single Entry Point for All Logging)          │
├─────────────────────────────────────────────────────────────────┤
│                      LoggingContext                             │
│  (Thread-local: Session, State, Action, Performance)            │
├─────────────────────────────────────────────────────────────────┤
│                     MessageRouter                               │
│  (Routes to appropriate handlers based on type/config)          │
├─────────────┬─────────────┬─────────────┬──────────────────────┤
│   Console   │  Structured │   SLF4J     │    Metrics          │
│   Handler   │   Handler   │  Handler    │    Handler          │
├─────────────┴─────────────┴─────────────┴──────────────────────┤
│                        Sinks/Appenders                           │
│  (Console, File, Database, Cloud, Message Queue, etc.)          │
└─────────────────────────────────────────────────────────────────┘
```

## Core Components

### BrobotLogger

The main facade that provides the unified API:

```java
@Component
public class BrobotLogger {
    // Simple logging methods
    void action(String action, StateObject target);
    void transition(State from, State to);
    void observation(String observation);
    void error(String message, Throwable cause);
    
    // Advanced features
    LogBuilder log();
    AutoCloseable session(String sessionId);
    AutoCloseable operation(String operationName);
    Timer startTimer(String timerName);
}
```

**Responsibilities:**
- Provide simple, intuitive API for common logging needs
- Create LogEvent instances with proper context
- Delegate routing to MessageRouter
- Manage configuration settings

### LoggingContext

Thread-local storage for contextual information:

```java
@Component
public class LoggingContext {
    private static final ThreadLocal<Context> contextHolder;
    
    static class Context {
        String sessionId;
        State currentState;
        String currentAction;
        Deque<String> operationStack;
        Map<String, Object> metadata;
    }
}
```

**Features:**
- Thread isolation for concurrent executions
- Hierarchical operation tracking
- Context snapshots for async operations
- Automatic cleanup to prevent memory leaks

### LogEvent

Immutable data model for log entries:

```java
public class LogEvent {
    enum Type { ACTION, TRANSITION, OBSERVATION, PERFORMANCE, ERROR }
    enum Level { DEBUG, INFO, WARNING, ERROR }
    
    // Core fields
    private final Type type;
    private final Level level;
    private final String message;
    private final long timestamp;
    
    // Contextual data
    private final String sessionId;
    private final String stateId;
    private final Map<String, Object> metadata;
}
```

**Design Principles:**
- Immutable for thread safety
- Builder pattern for flexible construction
- Rich metadata support
- Self-contained (no external dependencies)

### MessageRouter

Routes LogEvent instances to appropriate handlers:

```java
@Component
public class MessageRouter {
    void route(LogEvent event) {
        routeToSlf4j(event);
        routeToConsole(event);
        if (structuredLoggingEnabled) {
            routeToActionLogger(event);
        }
        routeToDiagnosticLogger(event);
    }
}
```

**Routing Rules:**
- All events → SLF4J (with appropriate levels)
- Events → ConsoleReporter (based on output level)
- Events → ActionLogger (if structured logging enabled)
- Pattern matching events → DiagnosticLogger (verbosity-aware)
- Future: Custom handlers via SPI

### DiagnosticLogger

Specialized component for pattern matching diagnostics:

```java
@Component
public class DiagnosticLogger {
    @Autowired(required = false)
    private LoggingVerbosityConfig verbosityConfig;
    
    @Autowired(required = false)
    private BrobotLogger brobotLogger;
    
    // Pattern search logging with verbosity awareness
    void logPatternSearch(Pattern pattern, Scene scene, double similarity);
    void logPatternResult(Pattern pattern, int matchCount, double bestScore);
    void logImageAnalysis(BufferedImage pattern, BufferedImage scene, String name);
    void logSimilarityAnalysis(String pattern, double[] thresholds, Double found, Double score);
}
```

**Features:**
- Verbosity-aware output (QUIET/NORMAL/VERBOSE)
- Pattern caching diagnostics
- Image content analysis (black screen detection)
- Progressive similarity threshold testing
- Integration with both ConsoleReporter and BrobotLogger

### LogBuilder

Fluent API for complex logging scenarios:

```java
public class LogBuilder {
    LogBuilder level(Level level);
    LogBuilder action(String action);
    LogBuilder target(StateObject object);
    LogBuilder result(ActionResult result);
    LogBuilder screenshot(String path);
    LogBuilder metadata(String key, Object value);
    LogBuilder performance(String metric, long value);
    void log();
}
```

## Design Patterns

### 1. Facade Pattern
BrobotLogger acts as a simplified interface to the complex logging subsystem.

### 2. Builder Pattern
LogEvent and LogBuilder use builders for flexible object construction.

### 3. Strategy Pattern
MessageRouter implements different routing strategies based on configuration.

### 4. Thread-Local Pattern
LoggingContext uses ThreadLocal for thread-safe context management.

### 5. Template Method Pattern
Handlers follow a template for processing LogEvents.

## Integration Points

### SLF4J Integration

```java
private void routeToSlf4j(LogEvent event) {
    String message = formatSlf4jMessage(event);
    
    switch (event.getLevel()) {
        case DEBUG: slf4jLogger.debug(message); break;
        case INFO: slf4jLogger.info(message); break;
        case WARNING: slf4jLogger.warn(message); break;
        case ERROR: slf4jLogger.error(message, event.getError()); break;
    }
}
```

### ConsoleReporter Integration

```java
private void routeToConsole(LogEvent event) {
    OutputLevel level = determineConsoleLevel(event);
    if (ConsoleReporter.minReportingLevel(level)) {
        String output = event.toFormattedString();
        String[] colors = determineColors(event);
        ConsoleReporter.println(output, colors);
    }
}
```

### ActionLogger Integration

```java
private void routeToActionLogger(LogEvent event) {
    switch (event.getType()) {
        case ACTION:
            actionLogger.logAction(sessionId, toActionResult(event), 
                                 toObjectCollection(event));
            break;
        case TRANSITION:
            actionLogger.logStateTransition(sessionId, 
                                          extractStates(event));
            break;
        // ... other types
    }
}
```

## Extension Points

### Custom Log Sinks

Implement the LogSink interface for custom persistence:

```java
@FunctionalInterface
public interface LogSink {
    void save(LogData logData);
}

// Example implementation
public class ElasticsearchLogSink implements LogSink {
    @Override
    public void save(LogData logData) {
        // Send to Elasticsearch
    }
}
```

### Custom Handlers

Add new handlers to MessageRouter:

```java
public interface LogHandler {
    void handle(LogEvent event);
    boolean canHandle(LogEvent event);
}

@Component
public class MetricsHandler implements LogHandler {
    @Override
    public void handle(LogEvent event) {
        if (event.getType() == PERFORMANCE) {
            // Export to Prometheus/Grafana
        }
    }
}
```

### Event Processors

Pre-process events before routing:

```java
public interface EventProcessor {
    LogEvent process(LogEvent event);
}

@Component
public class SanitizationProcessor implements EventProcessor {
    @Override
    public LogEvent process(LogEvent event) {
        // Remove sensitive data from metadata
        return sanitize(event);
    }
}
```

## Performance Characteristics

### Memory Usage
- **ThreadLocal Context**: ~1KB per thread
- **LogEvent**: ~500 bytes average (depends on metadata)
- **Buffer Size**: Configurable, default 1000 events

### Processing Time
- **Simple Log**: < 100 microseconds
- **With Routing**: < 500 microseconds
- **With Persistence**: Depends on sink (async recommended)

### Optimization Strategies

1. **Lazy Evaluation**
   ```java
   logger.log()
       .message(() -> expensiveComputation())
       .log();
   ```

2. **Async Processing**
   ```java
   @Async
   public void handleStructuredLog(LogEvent event) {
       // Process in background thread
   }
   ```

3. **Batch Operations**
   ```java
   public class BatchingLogSink implements LogSink {
       private final BlockingQueue<LogData> queue;
       // Batch write every N events or T seconds
   }
   ```

## Configuration Architecture

### Hierarchical Configuration

```yaml
brobot:
  logging:
    defaults:           # Global defaults
      level: INFO
      format: json
    
    console:           # Console-specific
      inherit: defaults
      level: HIGH
      
    structured:        # Structured logging
      inherit: defaults
      sinks:
        - file
        - elasticsearch
```

### Dynamic Reconfiguration

```java
@Component
public class LoggingConfigManager {
    @EventListener(ConfigChangeEvent.class)
    public void onConfigChange(ConfigChangeEvent event) {
        // Update logging configuration without restart
        reconfigureLoggers(event.getNewConfig());
    }
}
```

## Security Considerations

### Sensitive Data Handling

```java
@Component
public class SensitiveDataFilter {
    private final Set<String> sensitiveKeys = Set.of(
        "password", "token", "apiKey", "ssn"
    );
    
    public LogEvent filter(LogEvent event) {
        Map<String, Object> filtered = new HashMap<>();
        event.getMetadata().forEach((k, v) -> {
            if (sensitiveKeys.contains(k.toLowerCase())) {
                filtered.put(k, "***REDACTED***");
            } else {
                filtered.put(k, v);
            }
        });
        return event.withMetadata(filtered);
    }
}
```

### Access Control

```java
@Component
public class LogAccessController {
    @PreAuthorize("hasRole('ADMIN')")
    public List<LogEvent> getSecurityLogs() {
        // Only admins can access security logs
    }
}
```

## Testing Strategy

### Unit Tests
- Test each component in isolation
- Mock dependencies
- Verify routing logic

### Integration Tests
- Test full logging pipeline
- Verify handler interactions
- Check configuration loading

### Performance Tests
- Measure logging overhead
- Test under high concurrency
- Validate memory usage

## Migration Architecture

### Compatibility Layer

```java
@Component
public class LegacyLoggerAdapter {
    private final BrobotLogger logger;
    
    // Adapts old ActionLogger calls
    public void logAction(String sessionId, ActionResult result) {
        try (var session = logger.session(sessionId)) {
            logger.action("LEGACY", null, result);
        }
    }
}
```

### Gradual Migration

1. **Phase 1**: Deploy unified logger alongside existing
2. **Phase 2**: Route existing loggers through unified system
3. **Phase 3**: Migrate code to use unified API
4. **Phase 4**: Remove legacy logging code

## Future Architecture

### Planned Enhancements

1. **Distributed Tracing**
   - OpenTelemetry integration
   - Trace ID propagation
   - Span management

2. **Real-time Streaming**
   - WebSocket log streaming
   - Server-sent events
   - Log tailing API

3. **Machine Learning**
   - Anomaly detection
   - Pattern recognition
   - Predictive alerts

4. **Cloud Native**
   - Kubernetes operator
   - Service mesh integration
   - Multi-cluster support