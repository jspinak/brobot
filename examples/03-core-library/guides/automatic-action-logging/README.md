# Automatic Action Logging Examples

This project demonstrates best practices for logging Brobot automation actions using standard Java logging frameworks (SLF4J/Logback).

## Overview

While Brobot doesn't have built-in logging methods, it integrates seamlessly with standard logging frameworks. This example shows:

- **Basic logging patterns** - Log levels, timing, and results
- **Action chain logging** - Track multi-step workflows
- **Contextual logging with MDC** - Add workflow context
- **Real-world workflows** - Login and form automation
- **Performance tracking** - Measure action durations
- **Structured logging** - Machine-readable formats

## Project Structure

```
automatic-action-logging/
├── src/main/java/com/example/logging/
│   ├── ActionLoggingApplication.java        # Spring Boot main
│   ├── ActionLoggingRunner.java             # Runs all examples
│   ├── examples/
│   │   ├── BasicLoggingExample.java        # Basic patterns
│   │   ├── ActionChainLoggingExample.java  # Chain logging
│   │   └── ContextualLoggingExample.java   # MDC usage
│   └── workflows/
│       ├── LoginWorkflow.java              # Login example
│       └── FormAutomation.java             # Form filling
├── src/main/resources/
│   ├── application.yml                      # Spring config
│   └── logback-spring.xml                  # Logging config
├── logs/                                    # Log output
│   ├── automation.log                       # Main log
│   ├── actions.log                          # Action-specific
│   └── errors.log                          # Error log
├── images/                                  # Test images
├── build.gradle
└── settings.gradle
```

## Examples Demonstrated

### 1. Basic Logging Patterns

**Simple action logging:**
```java
log.info("Searching for submit button...");

ActionResult result = action.perform(findOptions, submitButton);

if (result.isSuccess()) {
    log.info("Submit button found at {} (took {}ms)", 
        result.getBestMatch().getRegion(), 
        result.getDuration().toMillis());
} else {
    log.warn("Submit button not found after {}ms", 
        result.getDuration().toMillis());
}
```

**Debug-level details:**
```java
log.debug("Configuration: similarity={}, maxWait={}s", 0.9, 5);
log.debug("Target object: name={}, patterns={}", 
    searchField.getName(), 
    searchField.getStateImages().size());
```

**Error handling:**
```java
try {
    ActionResult result = action.perform(clickOptions, submitButton);
    if (!result.isSuccess()) {
        log.error("Failed to click submit button - form submission failed");
        log.error("Action details: duration={}ms, error={}", 
            result.getDuration().toMillis(),
            result.getErrorMessage());
    }
} catch (Exception e) {
    log.error("Unexpected error during form submission", e);
}
```

### 2. Action Chain Logging

**Building and executing chains:**
```java
// Build the action chain
ActionChainOptions fillForm = new ActionChainOptions.Builder(findFirstName)
    .then(new ClickOptions.Builder().build())
    .then(new TypeOptions.Builder().build())
    .then(new PatternFindOptions.Builder().build())
    .then(new ClickOptions.Builder().build())
    .build();

log.info("Executing form fill chain...");
ActionResult result = action.perform(fillForm, formData);

if (result.isSuccess()) {
    log.info("✓ Form chain completed successfully in {}ms", duration);
} else {
    log.error("✗ Form chain failed at step: {}", result.getFailedStep());
}
```

**Chain strategies:**
- **NESTED**: Each action searches within previous results
- **CONFIRM**: Each action validates previous results

### 3. Contextual Logging with MDC

**Workflow context:**
```java
try {
    MDC.put("workflowId", UUID.randomUUID().toString());
    MDC.put("workflow", "user-login");
    MDC.put("step", "find-login");
    
    log.info("Looking for login button");
    // ... perform actions ...
    
} finally {
    MDC.clear(); // Always clear to prevent context leakage
}
```

**Multi-user tracking:**
```java
MDC.put("userId", username);
MDC.put("taskId", taskId);
log.info("Starting task for user");
// ... user-specific actions ...
```

### 4. Real-World Workflows

**Login workflow with comprehensive logging:**
- Track each step (username, password, submit)
- Log timing information
- Include error recovery
- Verify success/failure

**Form automation with chains:**
- Build complex action chains
- Handle form filling sequentially
- Implement fallback strategies
- Log submission results

## Logging Configuration

### Application Configuration (application.yml)

```yaml
logging:
  level:
    com.example.logging: DEBUG
    io.github.jspinak.brobot: INFO
  pattern:
    console: '%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{workflow}] [%X{step}] - %msg%n'
  file:
    name: logs/automation.log
```

### Logback Configuration (logback-spring.xml)

The project includes advanced Logback configuration with:
- **Console appender** - Color-coded output
- **File appenders** - Separate files for different log types
- **Rolling policies** - Automatic log rotation
- **Async appenders** - Performance optimization
- **Error filtering** - Dedicated error log

## Running the Examples

1. **In Mock Mode** (default):
   ```bash
   ./gradlew bootRun
   ```
   Runs with simulated actions to demonstrate logging patterns.

2. **With Real UI**:
   - Add screenshots to `images/` directory
   - Set `brobot.core.mock: false` in `application.yml`
   - Run the application

3. **View Logs**:
   - Console: Real-time colored output
   - `logs/automation.log`: Complete log history
   - `logs/actions.log`: Action-specific logs
   - `logs/errors.log`: Error tracking

## Best Practices

### 1. **Log Levels**
- **DEBUG**: Detailed execution flow
- **INFO**: Normal operation progress
- **WARN**: Recoverable issues
- **ERROR**: Failures requiring attention

### 2. **Include Context**
- Use MDC for workflow tracking
- Log action parameters
- Include timing information
- Add success/failure status

### 3. **Structure Logs**
```java
log.info("action_complete",
    "action", "find",
    "target", targetName,
    "success", result.isSuccess(),
    "duration_ms", duration
);
```

### 4. **Performance Tracking**
- Log start and end times
- Calculate durations
- Track slow operations
- Monitor resource usage

### 5. **Error Information**
- Log full stack traces for exceptions
- Include recovery attempts
- Document failure reasons
- Add diagnostic information

## Output Examples

### Console Output
```
10:23:45.123 [main] INFO  c.e.l.workflows.LoginWorkflow [user-login] [find-login] - Looking for login button
10:23:45.456 [main] DEBUG c.e.l.workflows.LoginWorkflow [user-login] [find-login] - Login button found at Region[100,200,150,50]
10:23:45.789 [main] INFO  c.e.l.workflows.LoginWorkflow [user-login] [submit-login] - Login successful - user authenticated
```

### Structured Log Output
```
action_start action=find target=SearchField similarity=0.85 maxWait=3
action_complete action=find target=SearchField success=true duration_ms=234 matches=1
match_details best_score=0.92 region_x=100 region_y=200 region_w=300 region_h=40
```

## Troubleshooting

### Missing Logs
- Check log level configuration
- Verify file permissions for log directory
- Ensure logback configuration is loaded

### Performance Issues
- Use async appenders for high-volume logging
- Adjust log levels in production
- Consider log aggregation tools

### Context Leakage
- Always clear MDC in finally blocks
- Use try-with-resources patterns
- Test multi-threaded scenarios

## Next Steps

1. Customize logging patterns for your needs
2. Integrate with log aggregation systems
3. Add custom MDC fields for your workflows
4. Implement performance monitoring
5. Create dashboards from structured logs

## Related Documentation

- [Automatic Action Logging Guide](../../automatic-action-logging.md)
- [Action Configuration](../../../action-config/README.md)
- [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)