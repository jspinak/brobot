---
sidebar_position: 25
---

# Modular Logging System Guide

A practical guide to using Brobot's Modular Logging System for clean, configurable action logging.

## Quick Start

### Enable Modular Logging

Add to your `application.yml`:

```yaml
brobot:
  logging:
    verbosity: QUIET  # Options: QUIET, NORMAL, VERBOSE
    console:
      enabled: true
```

### View Different Output Formats

#### QUIET Mode (Default)
Perfect for CI/CD and production monitoring:
```
✓ Find Working.ClaudeIcon • 234ms
✗ Click Button.Submit • 156ms
✓ Type "username" • 89ms
```

#### NORMAL Mode
Balanced output with timestamps and context:
```
[12:34:56] ✓ Find Working.ClaudeIcon completed in 234ms (1 match)
[12:34:57] ✗ Click Button.Submit failed in 156ms (No matches found)
[12:34:58] ✓ Type TextInput.Username completed in 89ms
```

#### VERBOSE Mode
Comprehensive debugging information:
```
=== ACTION EXECUTION ===
Started:    2024-01-20 12:34:56.123
Completed:  2024-01-20 12:34:56.357
Action ID:  a1b2c3d4-e5f6-7890-abcd-ef1234567890
Type:       FIND
Status:     SUCCESS ✓
Duration:   234ms
...
```

## Common Use Cases

### Production Monitoring

Use QUIET mode for minimal overhead:

```yaml
# production.yml
brobot:
  logging:
    verbosity: QUIET
    console:
      enabled: true
    file:
      enabled: true
      path: /var/log/brobot/actions.log
```

Monitor output:
```bash
tail -f /var/log/brobot/actions.log | grep "✗"  # Watch for failures
```

### Development Debugging

Use VERBOSE mode during development:

```yaml
# application-dev.yml
brobot:
  logging:
    verbosity: VERBOSE
    console:
      enabled: true
```

### CI/CD Pipeline

Configure for clean test output:

```yaml
# application-test.yml
brobot:
  logging:
    verbosity: QUIET
    console:
      enabled: true
      capture-enabled: false  # Prevent output interception
```

## Configuration Options

### Basic Configuration

```yaml
brobot:
  logging:
    # Verbosity level for all formatters
    verbosity: QUIET  # QUIET | NORMAL | VERBOSE
    
    # Console output settings
    console:
      enabled: true
      capture-enabled: false  # Set false to prevent output capture
    
    # File output settings (future feature)
    file:
      enabled: false
      path: logs/actions.log
      max-size: 10MB
      max-files: 5
```

### Environment-Specific Configuration

Create profile-specific configurations:

```yaml
# application-dev.yml
brobot:
  logging:
    verbosity: VERBOSE

# application-prod.yml
brobot:
  logging:
    verbosity: QUIET
```

Activate with Spring profiles:
```bash
java -jar app.jar --spring.profiles.active=prod
```

### Runtime Configuration

Change verbosity programmatically:

```java
@Autowired
private ActionLoggingService loggingService;

// For debugging a specific issue
loggingService.setVerbosityLevel(VerbosityLevel.VERBOSE);

// After debugging
loggingService.setVerbosityLevel(VerbosityLevel.QUIET);
```

## Output Format Reference

### QUIET Format

**Pattern**: `[symbol] [action] [target] • [duration]ms`

**Examples**:
```
✓ Find Working.ClaudeIcon • 234ms
✗ Click Button.Submit • 156ms
✓ Type • 89ms
✓ Wait • 1000ms
```

**Components**:
- **Symbol**: ✓ (success) or ✗ (failure)
- **Action**: Cleaned action type (Find, Click, Type, etc.)
- **Target**: State.Object format when available
- **Duration**: Execution time in milliseconds

### NORMAL Format

**Pattern**: `[timestamp] [symbol] [action] [target] [status] in [duration]ms ([details])`

**Examples**:
```
[12:34:56] ✓ Find Working.ClaudeIcon completed in 234ms (1 match)
[12:34:57] ✗ Click Button.Submit failed in 156ms (No matches found)
[12:34:58] ✓ Type TextInput.Username completed in 89ms
[12:34:59] ✗ Wait failed in 5000ms (Timeout exceeded)
```

**Additional Components**:
- **Timestamp**: HH:mm:ss format
- **Status**: "completed" or "failed"
- **Details**: Match count, error messages, etc.

### VERBOSE Format

Multi-line structured output with sections:

```
=== ACTION EXECUTION ===
Started:    2024-01-20 12:34:56.123
Completed:  2024-01-20 12:34:56.357
Action ID:  a1b2c3d4-e5f6-7890-abcd-ef1234567890
Thread:     main

--- ACTION DETAILS ---
Type:       FIND
Status:     SUCCESS ✓
Duration:   234ms

--- TARGETS ---
Images (1):
  [1] Working.ClaudeIcon

--- RESULTS ---
Matches:    1
  [1] Score: 0.987 Region: R[100,200 300x400]

--- ENVIRONMENT ---
OS:         Mac OS X
Java:       21.0.7
Monitors:   1

--- METRICS ---
Execution Time:   234ms
Match Count:      1
Best Match Score: 0.987
========================
```

## Troubleshooting

### No Output Appearing

1. **Check if logging is enabled**:
   ```yaml
   brobot:
     logging:
       console:
         enabled: true  # Must be true
   ```

2. **Verify AspectJ is enabled**:
   ```java
   @SpringBootApplication
   @EnableAspectJAutoProxy  // Required annotation
   public class MyApplication {
   ```

3. **Check output capture settings**:
   ```yaml
   brobot:
     logging:
       console:
         capture-enabled: false  # Set to false if output is being intercepted
   ```

### Wrong Verbosity Level

1. **Check configuration hierarchy**:
   - Command line args override
   - Environment variables override
   - application-{profile}.yml overrides
   - application.yml defaults

2. **Verify active profile**:
   ```bash
   # Check which profile is active
   echo $SPRING_PROFILES_ACTIVE
   ```

3. **Debug configuration loading**:
   ```java
   @Value("${brobot.logging.verbosity}")
   private String verbosity;
   
   @PostConstruct
   public void logConfig() {
       log.info("Active verbosity: {}", verbosity);
   }
   ```

### Incomplete Output

**Issue**: Missing target information
```
✗ Find • 9ms  # Missing target
```

**Solutions**:
1. Ensure StateImages have proper names:
   ```java
   @StateImage(name = "ClaudeIcon")  // Name is required
   private Image claudeIcon;
   ```

2. Verify state registration:
   ```java
   @State(name = "Working")  // State name is required
   public class WorkingState {
   ```

### Performance Impact

If logging is impacting performance:

1. **Use QUIET mode**:
   ```yaml
   brobot:
     logging:
       verbosity: QUIET  # Minimal overhead
   ```

2. **Disable file logging**:
   ```yaml
   brobot:
     logging:
       file:
         enabled: false  # Reduce I/O
   ```

3. **Consider sampling**:
   ```java
   // Custom formatter that samples
   @Component
   public class SamplingFormatter implements ActionLogFormatter {
       private final AtomicInteger counter = new AtomicInteger();
       
       @Override
       public boolean shouldLog(ActionResult result) {
           // Log every 10th action
           return counter.incrementAndGet() % 10 == 0;
       }
   }
   ```

## Best Practices

### 1. Environment-Appropriate Verbosity

```yaml
# Development
verbosity: VERBOSE  # See everything

# Testing
verbosity: NORMAL   # Balanced information

# Production
verbosity: QUIET    # Minimal overhead
```

### 2. Structured Logging for Analysis

When you need machine-readable logs:

```java
@Component
public class JsonFormatter implements ActionLogFormatter {
    @Override
    public String format(ActionResult result) {
        return String.format(
            "{\"action\":\"%s\",\"success\":%b,\"duration\":%d,\"target\":\"%s\"}",
            result.getExecutionContext().getActionType(),
            result.getExecutionContext().isSuccess(),
            result.getExecutionContext().getExecutionDuration().toMillis(),
            result.getExecutionContext().getPrimaryTargetName()
        );
    }
}
```

### 3. Correlation IDs for Tracing

Track related actions:

```java
// In your automation
String correlationId = UUID.randomUUID().toString();
MDC.put("correlationId", correlationId);

// Actions will include this ID in VERBOSE mode
action.find(image);  // Logs will include correlationId
action.click();      // Same correlationId
```

### 4. Custom Metrics Collection

Extend ActionMetrics for domain-specific metrics:

```java
ActionResult.ActionMetrics metrics = result.getActionMetrics();
metrics.getTimingBreakdown().put("imageProcessing", 45L);
metrics.getTimingBreakdown().put("screenCapture", 189L);
```

## Integration Examples

### With ELK Stack

Configure file output for Logstash:

```yaml
brobot:
  logging:
    file:
      enabled: true
      path: /var/log/brobot/actions.json
      formatter: json  # Future feature
```

Logstash configuration:
```ruby
input {
  file {
    path => "/var/log/brobot/actions.json"
    codec => "json"
  }
}

filter {
  if [action] == "FIND" and [success] == false {
    mutate {
      add_tag => [ "find_failure" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "brobot-actions-%{+YYYY.MM.dd}"
  }
}
```

### With Monitoring Systems

Export metrics to Prometheus:

```java
@Component
public class PrometheusExporter {
    private final Counter actionCounter = Counter.builder("brobot_actions_total")
        .help("Total number of actions executed")
        .labelNames("action", "success")
        .register();
    
    @EventListener
    public void onActionLogged(ActionLoggedEvent event) {
        actionCounter.labels(
            event.getActionType(),
            String.valueOf(event.isSuccess())
        ).inc();
    }
}
```

### With CI/CD Pipelines

Parse QUIET output in Jenkins:

```groovy
pipeline {
    stages {
        stage('Test') {
            steps {
                script {
                    def output = sh(
                        script: 'java -jar app.jar',
                        returnStdout: true
                    )
                    
                    def failures = output.findAll(/✗.*/)
                    if (failures) {
                        error "Found ${failures.size()} failed actions"
                    }
                }
            }
        }
    }
}
```

## Migration Guide

### From Old Logging System

**Before**:
```java
// Scattered logging
System.out.println("Found: " + matches.size());
ConsoleReporter.log(OutputLevel.HIGH, "Click successful");
actionLogger.logAction(sessionId, result, collection);
```

**After**:
```java
// Unified logging - happens automatically
action.find(image);  // Logged by ActionLoggingService
```

### Custom Output Requirements

If you need custom output format:

1. **Create custom formatter**:
   ```java
   @Component
   public class TeamCityFormatter implements ActionLogFormatter {
       @Override
       public String format(ActionResult result) {
           return String.format(
               "##teamcity[testStarted name='%s' captureStandardOutput='true']",
               result.getExecutionContext().getActionType()
           );
       }
   }
   ```

2. **Register with Spring**:
   The `@Component` annotation automatically registers it.

3. **Configure to use it**:
   ```yaml
   brobot:
     logging:
       formatter: teamcity  # Future feature
   ```

## Advanced Topics

### Performance Optimization

For high-frequency actions:

```java
@Component
public class BatchingLogService extends ActionLoggingService {
    private final BlockingQueue<ActionResult> queue = new LinkedBlockingQueue<>();
    
    @Scheduled(fixedDelay = 1000)
    public void flushBatch() {
        List<ActionResult> batch = new ArrayList<>();
        queue.drainTo(batch);
        
        if (!batch.isEmpty()) {
            String combined = batch.stream()
                .map(this::formatSingle)
                .collect(Collectors.joining("\n"));
            System.out.println(combined);
        }
    }
}
```

### Multi-Destination Logging

Log to multiple destinations:

```java
@Component
public class MultiDestinationLogger extends ActionLoggingService {
    @Override
    protected void outputFormattedMessage(String message, VerbosityLevel level) {
        // Console
        System.out.println(message);
        
        // File
        fileWriter.write(message);
        
        // Remote
        if (level == VerbosityLevel.VERBOSE) {
            remoteLogger.send(message);
        }
    }
}
```

### Conditional Logging

Log based on conditions:

```java
@Component
public class ConditionalFormatter implements ActionLogFormatter {
    @Value("${app.debug.actions:}")
    private Set<String> debugActions;
    
    @Override
    public boolean shouldLog(ActionResult result) {
        String actionType = result.getExecutionContext().getActionType();
        
        // Always log failures
        if (!result.getExecutionContext().isSuccess()) {
            return true;
        }
        
        // Log specific actions in debug mode
        return debugActions.contains(actionType);
    }
}
```

## Summary

The Modular Logging System provides:

- **Clean Output**: Single-line format in QUIET mode
- **Flexibility**: Three verbosity levels for different needs
- **Extensibility**: Easy to add custom formatters
- **Performance**: Minimal overhead in production
- **Maintainability**: Centralized logging logic

Start with QUIET mode for clean output, use NORMAL for development, and switch to VERBOSE when debugging issues.