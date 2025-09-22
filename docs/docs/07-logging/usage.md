# Logging Usage Guide

## Automatic Action Logging

All Brobot actions automatically log their execution and results. No additional code is required.

### Example Action Execution
```java
// This automatically logs the action
ActionResult result = action.find(stateImage);
```

### Generated Log Output
```
[ACTIONS] INFO  FIND submitButton → SUCCESS [25ms] loc:(100,200) sim:0.95
```

## Programmatic Logging

For custom logging needs, use the BrobotLogger directly:

### Inject the Logger
```java
@Autowired
private BrobotLogger logger;
```

### Simple Logging
```java
// Log a simple message
logger.info(LogCategory.ACTIONS, "Processing form submission");

// Log with format parameters
logger.debug(LogCategory.MATCHING, "Found {} matches in {}ms", 3, 150);

// Log errors
logger.error(LogCategory.SYSTEM, "Failed to connect: {}", exception.getMessage());
```

### Fluent API
```java
// Build complex log entries
logger.builder(LogCategory.ACTIONS)
    .level(LogLevel.INFO)
    .message("Processing batch operation")
    .context("batch_id", batchId)
    .context("items_count", items.size())
    .duration(Duration.ofMillis(500))
    .log();
```

### Event-Based Logging
```java
// Log action events
ActionEvent event = ActionEvent.builder()
    .type("CLICK")
    .target("submitButton")
    .success(true)
    .duration(Duration.ofMillis(25))
    .location(new Location(100, 200))
    .similarity(0.95)
    .build();

logger.logAction(event);
```

## Correlation Tracking

Track related operations with correlation IDs:

```java
@Autowired
private CorrelationContext correlation;

// Start a new session
correlation.startSession("user_registration");

// All subsequent logs include the session ID
action.click(submitButton);  // Logs include correlation ID
action.type(emailField, email);  // Same correlation ID

// End the session
correlation.endSession();
```

## Structured Event Types

### ActionEvent
```java
ActionEvent event = ActionEvent.builder()
    .type("FIND")
    .target("loginButton")
    .success(true)
    .duration(Duration.ofMillis(50))
    .location(new Location(500, 300))
    .similarity(0.92)
    .metadata("attempts", 2)
    .build();

logger.logAction(event);
```

### TransitionEvent
```java
TransitionEvent event = TransitionEvent.builder()
    .fromState("MainMenu")
    .toState("Settings")
    .success(true)
    .duration(Duration.ofMillis(200))
    .method(TransitionMethod.CLICK)
    .build();

logger.logTransition(event);
```

### MatchEvent
```java
MatchEvent event = MatchEvent.builder()
    .pattern("submitButton.png")
    .matches(matchList)
    .searchTime(Duration.ofMillis(75))
    .strategy(SearchStrategy.BEST)
    .searchRegion(region)
    .build();

logger.logMatch(event);
```

### PerformanceEvent
```java
PerformanceEvent event = PerformanceEvent.builder()
    .operation("batch_process")
    .duration(Duration.ofSeconds(5))
    .memoryUsed(1024 * 1024 * 50) // 50MB
    .breakdown(Map.of(
        "initialization", Duration.ofMillis(100),
        "processing", Duration.ofMillis(4500),
        "cleanup", Duration.ofMillis(400)
    ))
    .build();

logger.logPerformance(event);
```

## Conditional Logging

Check log levels before expensive operations:

```java
if (logger.shouldLog(LogCategory.MATCHING, LogLevel.DEBUG)) {
    // Expensive operation only when DEBUG is enabled
    String details = generateDetailedReport();
    logger.debug(LogCategory.MATCHING, details);
}
```

## Best Practices

### 1. Use Appropriate Categories
```java
// Good - use specific category
logger.info(LogCategory.ACTIONS, "Button clicked");

// Bad - wrong category
logger.info(LogCategory.SYSTEM, "Button clicked");
```

### 2. Use Appropriate Levels
```java
// ERROR - Something failed that needs attention
logger.error(LogCategory.ACTIONS, "Critical action failed");

// WARN - Something unexpected but recoverable
logger.warn(LogCategory.MATCHING, "Low similarity match: 0.65");

// INFO - Normal operation events
logger.info(LogCategory.TRANSITIONS, "Navigation completed");

// DEBUG - Detailed information for debugging
logger.debug(LogCategory.MATCHING, "Search region: {}", region);

// TRACE - Most detailed, typically method entry/exit
logger.trace(LogCategory.LIFECYCLE, "Entering processAction()");
```

### 3. Include Context
```java
// Good - includes context
logger.builder(LogCategory.ACTIONS)
    .message("Processing failed")
    .context("item_id", itemId)
    .context("error", exception.getMessage())
    .log();

// Bad - no context
logger.error(LogCategory.ACTIONS, "Processing failed");
```

### 4. Use Structured Events
```java
// Good - structured event with metadata
ActionEvent event = ActionEvent.success("CLICK", "button", Duration.ofMillis(50));
logger.logAction(event);

// Less ideal - plain text
logger.info(LogCategory.ACTIONS, "Clicked button in 50ms");
```