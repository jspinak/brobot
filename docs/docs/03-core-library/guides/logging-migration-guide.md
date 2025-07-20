---
sidebar_position: 7
---

# Logging Migration Guide

This guide helps you migrate from Brobot's legacy logging systems to the new Unified Logging System.

## Overview

The Unified Logging System replaces three separate logging mechanisms:
- SLF4J direct usage
- ActionLogger for structured logs  
- ConsoleReporter for console output

All functionality is now available through a single `BrobotLogger` interface.

## Migration Checklist

- [ ] Add BrobotLogger dependency injection
- [ ] Replace ConsoleReporter static calls
- [ ] Update ActionLogger usage
- [ ] Migrate SLF4J logger statements
- [ ] Add session scoping to tests
- [ ] Update configuration files
- [ ] Remove legacy logger imports

## Step-by-Step Migration

### Step 1: Add BrobotLogger Injection

Replace static logger declarations with injected BrobotLogger:

```java
// Old way - multiple loggers
public class MyAutomation {
    private static final Logger log = LoggerFactory.getLogger(MyAutomation.class);
    @Autowired
    private ActionLogger actionLogger;
    
    // ...
}

// New way - single logger
public class MyAutomation {
    @Autowired
    private BrobotLogger logger;
    
    // ...
}
```

### Step 2: Migrate ConsoleReporter Calls

ConsoleReporter static methods should be replaced with BrobotLogger instance methods:

#### Basic Output

```java
// Old way
ConsoleReporter.print("Processing started");
ConsoleReporter.println("Processing complete");

// New way
logger.observation("Processing started");
logger.observation("Processing complete");
```

#### Level-based Output

```java
// Old way
ConsoleReporter.print(OutputLevel.HIGH, "Detailed info");
ConsoleReporter.println(OutputLevel.LOW, "Important message");

// New way
logger.observation("Detailed info"); // Respects console level setting
logger.observation("Important message", "WARNING"); // Always shown at LOW
```

#### Action Reporting

```java
// Old way
ConsoleReporter.print(match, stateObject, actionOptions);

// New way
logger.action(actionOptions.getAction().toString(), stateObject);
```

#### Formatted Output

```java
// Old way
ConsoleReporter.format("Found %d matches in %.2f seconds", count, time);

// New way
logger.observation(String.format("Found %d matches in %.2f seconds", count, time));
// Or with metadata
logger.log()
    .observation("Search complete")
    .metadata("matchCount", count)
    .metadata("duration", time)
    .log();
```

#### Colored Output

```java
// Old way
ConsoleReporter.println("Success!", AnsiColor.GREEN);
ConsoleReporter.print("Error!", AnsiColor.RED);

// New way
logger.log()
    .observation("Success!")
    .color(AnsiColor.GREEN)
    .log();

logger.log()
    .error(new Exception("Error!"))
    .color(AnsiColor.RED)
    .log();
```

### Step 3: Migrate ActionLogger Usage

Replace ActionLogger method calls with BrobotLogger equivalents:

#### Action Logging

```java
// Old way
actionLogger.logAction(sessionId, actionResult, objectCollection);

// New way
logger.action("CLICK", stateObject, actionResult);
```

#### State Transitions

```java
// Old way
Set<State> from = Set.of(currentState);
Set<State> to = Set.of(targetState);
actionLogger.logStateTransition(sessionId, from, to, from, true, 1500);

// New way
logger.transition(currentState, targetState, true, 1500);
```

#### Observations

```java
// Old way
actionLogger.logObservation(sessionId, "UI_CHECK", 
    "Button is enabled", "INFO");

// New way
logger.observation("Button is enabled", "INFO");
// Or with more context
logger.log()
    .observation("Button is enabled")
    .metadata("elementType", "UI_CHECK")
    .log();
```

#### Performance Metrics

```java
// Old way
actionLogger.logPerformanceMetrics(sessionId, 
    actionDuration, pageLoadTime, totalDuration);

// New way
logger.log()
    .performanceLog()
    .message("Test execution metrics")
    .metadata("actionDuration", actionDuration)
    .metadata("pageLoadTime", pageLoadTime)
    .metadata("totalDuration", totalDuration)
    .log();

// Or use built-in timing
try (var timer = logger.startTimer("TestExecution")) {
    // Perform test
} // Duration logged automatically
```

#### Error Logging

```java
// Old way
actionLogger.logError(sessionId, errorMessage, screenshotPath);

// New way
logger.log()
    .error(exception)
    .message(errorMessage)
    .screenshot(screenshotPath)
    .log();
```

### Step 4: Migrate SLF4J Usage

Replace direct SLF4J logger calls:

#### Basic Logging

```java
// Old way
log.info("Starting automation");
log.debug("Current state: {}", state);
log.warn("Retry attempt {} of {}", attempt, maxRetries);
log.error("Failed to find element", exception);

// New way
logger.observation("Starting automation");
logger.observation("Current state: " + state); 
logger.observation("Retry attempt " + attempt + " of " + maxRetries, "WARNING");
logger.error("Failed to find element", exception);
```

#### Conditional Logging

```java
// Old way
if (log.isDebugEnabled()) {
    log.debug("Expensive computation: {}", computeDebugInfo());
}

// New way
if (logger.getConsoleLevel() == OutputLevel.HIGH) {
    logger.observation("Expensive computation: " + computeDebugInfo());
}
```

### Step 5: Add Session Management

Wrap test and automation code in sessions:

```java
// Old way
public void runTest() {
    String sessionId = UUID.randomUUID().toString();
    // Pass sessionId to every log call
    actionLogger.logAction(sessionId, result, collection);
}

// New way
public void runTest() {
    try (var session = logger.session(UUID.randomUUID().toString())) {
        // Session ID automatically included in all logs
        logger.action("START", testCase);
    }
}
```

### Step 6: Update Test Classes

Migrate test class logging:

```java
// Old way
@Test
public class LoginTest {
    private static final Logger log = LoggerFactory.getLogger(LoginTest.class);
    @Autowired
    private ActionLogger actionLogger;
    
    @Test
    public void testLogin() {
        log.info("Starting login test");
        String sessionId = "test-" + System.currentTimeMillis();
        
        // Perform test
        ActionResult result = performLogin();
        actionLogger.logAction(sessionId, result, null);
        
        if (!result.isSuccess()) {
            log.error("Login failed");
        }
    }
}

// New way
@Test
public class LoginTest {
    @Autowired
    private BrobotLogger logger;
    
    @Test
    public void testLogin() {
        try (var session = logger.session("test-" + System.currentTimeMillis())) {
            logger.observation("Starting login test");
            
            // Perform test
            ActionResult result = performLogin();
            logger.action("LOGIN", loginPage, result);
            
            if (!result.isSuccess()) {
                logger.error("Login failed", null);
            }
        }
    }
}
```

## Configuration Migration

### Application Properties

Update your application.yml or application.properties:

```yaml
# Old configuration
logging:
  level:
    io.github.jspinak.brobot: DEBUG
    
console:
  reporter:
    level: HIGH

# New configuration
brobot:
  logging:
    console:
      level: HIGH
      colors: true
    structured:
      enabled: true
    slf4j:
      level: DEBUG
```

### Logback Configuration

The unified logger still uses SLF4J internally, so your logback.xml continues to work:

```xml
<!-- This still works with unified logging -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/brobot.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="io.github.jspinak.brobot" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## Common Migration Patterns

### Pattern 1: Action with Retry

```java
// Old way
for (int i = 0; i < maxRetries; i++) {
    log.debug("Attempt {} of {}", i + 1, maxRetries);
    ActionResult result = performAction();
    
    ConsoleReporter.print(match, stateObject, actionOptions);
    actionLogger.logAction(sessionId, result, objectCollection);
    
    if (result.isSuccess()) {
        log.info("Action succeeded on attempt {}", i + 1);
        break;
    }
    
    if (i == maxRetries - 1) {
        log.error("Action failed after {} attempts", maxRetries);
        actionLogger.logError(sessionId, "Max retries exceeded", screenshot);
    }
}

// New way
for (int i = 0; i < maxRetries; i++) {
    logger.log()
        .action("CLICK")
        .target(stateObject)
        .metadata("attempt", i + 1)
        .metadata("maxRetries", maxRetries)
        .log();
    
    ActionResult result = performAction();
    logger.action("CLICK", stateObject, result);
    
    if (result.isSuccess()) {
        logger.observation("Action succeeded on attempt " + (i + 1));
        break;
    }
    
    if (i == maxRetries - 1) {
        logger.log()
            .error(new Exception("Max retries exceeded"))
            .screenshot(screenshot)
            .metadata("attempts", maxRetries)
            .log();
    }
}
```

### Pattern 2: State Validation

```java
// Old way
log.info("Validating state: {}", expectedState.getName());
Set<State> currentStates = detectStates();

if (currentStates.contains(expectedState)) {
    ConsoleReporter.println("State validation passed", AnsiColor.GREEN);
    actionLogger.logObservation(sessionId, "STATE_CHECK", 
        "Correct state detected", "INFO");
} else {
    ConsoleReporter.println("State validation failed", AnsiColor.RED);
    log.error("Expected state {} but found {}", expectedState, currentStates);
    actionLogger.logError(sessionId, "Wrong state detected", screenshot);
}

// New way
logger.observation("Validating state: " + expectedState.getName());
Set<State> currentStates = detectStates();

if (currentStates.contains(expectedState)) {
    logger.log()
        .observation("State validation passed")
        .color(AnsiColor.GREEN)
        .metadata("expectedState", expectedState.getName())
        .metadata("actualStates", currentStates)
        .log();
} else {
    logger.log()
        .error(new Exception("Wrong state detected"))
        .screenshot(screenshot)
        .metadata("expected", expectedState)
        .metadata("actual", currentStates)
        .color(AnsiColor.RED)
        .log();
}
```

### Pattern 3: Performance Tracking

```java
// Old way
long startTime = System.currentTimeMillis();
log.info("Starting batch process");

processBatch();

long duration = System.currentTimeMillis() - startTime;
ConsoleReporter.format("Batch completed in %d ms", duration);
actionLogger.logPerformanceMetrics(sessionId, 0, 0, duration);

// New way
try (var timer = logger.startTimer("BatchProcess")) {
    logger.observation("Starting batch process");
    processBatch();
    logger.observation("Batch completed");
} // Duration automatically logged
```

## Gradual Migration Strategy

If you need to migrate gradually:

### Phase 1: Add Unified Logger Alongside Existing

```java
public class MyClass {
    // Keep existing loggers temporarily
    private static final Logger log = LoggerFactory.getLogger(MyClass.class);
    @Autowired
    private ActionLogger actionLogger;
    
    // Add new logger
    @Autowired
    private BrobotLogger logger;
    
    // Use both during transition
    public void myMethod() {
        log.info("Old style logging");
        logger.observation("New style logging");
    }
}
```

### Phase 2: Create Adapter Methods

```java
public class LoggingAdapter {
    @Autowired
    private BrobotLogger logger;
    
    // Adapter for ConsoleReporter
    public static void print(String message) {
        // Get singleton instance and delegate
        getInstance().logger.observation(message);
    }
    
    // Adapter for ActionLogger
    public void logAction(String sessionId, ActionResult result) {
        try (var session = logger.session(sessionId)) {
            logger.action("LEGACY", null, result);
        }
    }
}
```

### Phase 3: Replace Usage Incrementally

1. Start with new code - use only BrobotLogger
2. Migrate test classes one at a time
3. Update utility classes
4. Migrate core automation classes
5. Remove legacy logger declarations

### Phase 4: Clean Up

1. Remove legacy logger imports
2. Delete adapter methods
3. Remove old configuration
4. Update documentation

## Troubleshooting

### Issue: Lost Session Context

**Symptom**: Session ID not appearing in logs

**Solution**: Ensure code is wrapped in session scope
```java
try (var session = logger.session("test-123")) {
    // All logging here includes session ID
}
```

### Issue: No Console Output

**Symptom**: Logs appear in files but not console

**Solution**: Check console level setting
```java
logger.setConsoleLevel(OutputLevel.HIGH);
```

### Issue: Lombok Compilation Errors

**Symptom**: IDE shows errors for getName(), isSuccess() methods

**Solution**: 
1. Ensure Lombok plugin is installed
2. Enable annotation processing
3. Clean and rebuild project

### Issue: Missing Metadata

**Symptom**: Custom metadata not appearing in logs

**Solution**: Add metadata to context or use builder
```java
// Context metadata (persistent)
logger.getContext().addMetadata("env", "test");

// Per-log metadata
logger.log()
    .observation("Test")
    .metadata("custom", value)
    .log();
```

## Verification

After migration, verify:

1. **Console Output**: Run tests and check console formatting
2. **Log Files**: Verify SLF4J file output still works
3. **Structured Logs**: Check ActionLogger compatibility
4. **Performance**: Ensure no significant overhead
5. **Thread Safety**: Test concurrent execution

## Benefits After Migration

- **Cleaner Code**: Single logger injection
- **Better Context**: Automatic session tracking
- **Richer Logs**: Easy metadata addition
- **Performance**: Built-in timing utilities
- **Flexibility**: Fluent API for complex scenarios
- **Future Proof**: Ready for new logging features