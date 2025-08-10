---
sidebar_position: 5
---

# Unified Logging System

The Brobot Unified Logging System consolidates all logging functionality into a single, cohesive API that simplifies developer experience while providing powerful capabilities for debugging, monitoring, and analysis.

## Overview

Previously, Brobot used three separate logging systems:
- **SLF4J** - Traditional application logging
- **ActionLogger** - Structured automation event logging
- **ConsoleReporter** - Real-time console feedback

The new unified system combines these into a single `BrobotLogger` facade that automatically handles routing to appropriate backends while providing a consistent API.

## Key Features

- **Single Entry Point** - One logger for all logging needs
- **Automatic Context** - Session, state, and action context propagation
- **Fluent API** - Simple methods for common cases, builder pattern for complex scenarios
- **Performance Tracking** - Built-in timers and metrics
- **Backward Compatible** - Works with existing logging infrastructure
- **Thread Safe** - Thread-local context management
- **Modular Formatting** - Configurable output formats (QUIET, NORMAL, VERBOSE) via the [Modular Logging System](../architecture/modular-logging-system.md)

## Quick Start

### Basic Usage

```java
@Component
public class LoginAutomation {
    @Autowired
    private BrobotLogger logger;
    
    public void performLogin(String username, String password) {
        // Simple action logging
        logger.action("CLICK", loginButton);
        
        // Log with result
        ActionResult result = click(loginButton);
        logger.action("CLICK", loginButton, result);
        
        // State transition
        logger.transition(loginState, dashboardState);
        
        // Observation
        logger.observation("Login form displayed correctly");
        
        // Error logging
        logger.error("Failed to find login button", exception);
    }
}
```

### Session Scoped Logging

```java
// All logs within the session automatically include session ID
try (var session = logger.session("test-session-123")) {
    logger.action("START", testCase);
    
    // Perform test steps...
    logger.transition(state1, state2);
    
    logger.observation("Test completed successfully");
}
```

### Performance Tracking

```java
// Using operation scope
try (var operation = logger.operation("ComplexOperation")) {
    performComplexTask();
    // Duration automatically logged when scope closes
}

// Using explicit timer
try (var timer = logger.startTimer("DataProcessing")) {
    processData();
    // Timer stops and logs duration when closed
}

// Manual timer control
BrobotLogger.Timer timer = logger.startTimer("CustomTimer");
doWork();
long duration = timer.stop(); // Returns duration in ms
```

## Advanced Usage

### Fluent Builder API

The builder API provides fine-grained control for complex logging scenarios:

```java
logger.log()
    .type(LogEvent.Type.ACTION)
    .level(LogEvent.Level.INFO)
    .action("CLICK")
    .target(submitButton)
    .result(actionResult)
    .success(true)
    .duration(250)
    .screenshot("/tmp/screenshot.png")
    .metadata("retryCount", 3)
    .metadata("elementVisible", true)
    .performance("responseTime", 150)
    .color(AnsiColor.GREEN)
    .log();
```

### Custom Metadata

Add contextual information that will be included in all subsequent logs:

```java
// Add metadata to context
logger.getContext().addMetadata("testSuite", "regression");
logger.getContext().addMetadata("environment", "staging");

// All subsequent logs will include this metadata
logger.action("CLICK", button); // Includes testSuite and environment
```

### Error Handling with Screenshots

```java
try {
    clickElement(targetElement);
} catch (ElementNotFoundException e) {
    String screenshotPath = captureScreenshot();
    
    logger.log()
        .error(e)
        .message("Element not found after retries")
        .screenshot(screenshotPath)
        .metadata("lastKnownLocation", element.getLocation())
        .log();
}
```

### State Transition Logging

```java
// Simple transition
logger.transition(currentState, targetState);

// Transition with details
logger.transition(currentState, targetState, success, duration);

// Complex transition with builder
logger.log()
    .transition(currentState.getName(), targetState.getName())
    .success(false)
    .duration(5000)
    .message("Transition timeout - retrying")
    .metadata("attemptNumber", 2)
    .log();
```

## Configuration

### Application Properties

Configure the logging system through `application.yml`:

```yaml
brobot:
  logging:
    console:
      level: HIGH          # NONE, LOW, HIGH
      colors: true         # Enable ANSI colors
      format: compact      # compact or detailed
    structured:
      enabled: true        # Enable structured logging
      format: json         # json, xml, csv
      sink: file          # file, database, cloud
      file:
        path: logs/brobot-structured.json
        rotation: daily    # daily, size, none
        max-size: 100MB
    performance:
      enabled: true
      metrics-export: prometheus
    context:
      include-thread-name: true
      include-timestamp: true
      include-hostname: false
```

### Programmatic Configuration

```java
// Configure console output
logger.setConsoleLevel(ConsoleReporter.OutputLevel.LOW);

// Enable/disable structured logging
logger.enableStructuredLogging(true);

// Custom configuration
BrobotLogger.configure()
    .consoleLevel(OutputLevel.HIGH)
    .enableColors(true)
    .structuredLogging(true)
    .addSink(new CustomLogSink())
    .apply();
```

## Context Management

### Thread-Local Context

The logging context is thread-local, ensuring isolation between concurrent executions:

```java
@Component
public class LoggingContext {
    // Set context information
    void setSessionId(String sessionId);
    void setCurrentState(State state);
    void setCurrentAction(String action);
    
    // Hierarchical operations
    void pushOperation(String operationName);
    String popOperation();
    
    // Custom metadata
    void addMetadata(String key, Object value);
    Map<String, Object> getAllMetadata();
    
    // Context snapshots for async operations
    Context snapshot();
    void restore(Context snapshot);
}
```

### Async Operations

For asynchronous operations, capture and restore context:

```java
// Capture context before async operation
LoggingContext.Context snapshot = logger.getContext().snapshot();

CompletableFuture.runAsync(() -> {
    // Restore context in async thread
    logger.getContext().restore(snapshot);
    
    logger.action("ASYNC_PROCESS", dataObject);
});
```

## Diagnostic Logging

### DiagnosticLogger

The `DiagnosticLogger` component provides specialized logging for pattern matching and image analysis, with full verbosity awareness:

```java
@Autowired
private DiagnosticLogger diagnosticLogger;

// Pattern search logging
diagnosticLogger.logPatternSearch(pattern, scene, similarity);

// Match result logging
diagnosticLogger.logPatternResult(pattern, matchCount, bestScore);

// Image analysis for failed matches
diagnosticLogger.logImageAnalysis(patternImg, sceneImg, patternName);

// Similarity threshold analysis
diagnosticLogger.logSimilarityAnalysis(patternName, thresholds, foundThreshold, foundScore);
```

### Verbosity Levels

The diagnostic logger respects the configured verbosity level:

- **QUIET**: Minimal output (✓/✗ symbols only)
- **NORMAL**: Concise diagnostic information with [SEARCH], [RESULT], [IMAGE ANALYSIS] tags
- **VERBOSE**: Full details including all matches, metadata, caching information, and performance metrics

### Pattern Matching Diagnostics

In VERBOSE mode, you'll see comprehensive pattern matching information:

```
[SEARCH] Pattern: 'login-button' (64x32) | Similarity: 0.70 | Scene: 1920x1080
  [Pattern.sikuli()] Using CACHED SikuliX Pattern for: login-button
  [FOUND #1] Score: 0.852 at (450, 320)
  [FOUND #2] Score: 0.743 at (450, 520)
  [RESULT] 2 matches for 'login-button' | Best score: 0.852
```

### Failed Match Analysis

When patterns aren't found, detailed diagnostics help identify the issue:

```
[RESULT] NO MATCHES for 'submit-button'
  [IMAGE ANALYSIS]
    Pattern: 128x48 type=RGB bytes=24KB
    Pattern content: 2.3% black, 45.6% white, avg RGB=(127,189,210)
    Scene: 1920x1080 type=RGB bytes=8MB
    Scene content: 95.2% black, 0.1% white, avg RGB=(5,5,5)
    WARNING: Scene is mostly BLACK - possible capture failure!
  [SIMILARITY ANALYSIS]
    Threshold 0.9: No match
    Threshold 0.8: No match
    Threshold 0.7: No match
    Threshold 0.6: FOUND with score 0.624
```

This immediately shows that:
1. The scene is mostly black (screen capture failure)
2. The pattern would match at 0.6 similarity but not at the configured 0.7

## Output Formats

### Console Output

The console output is formatted for readability with optional ANSI colors:

```
[session-123] CLICK → LoginButton (success) [250ms]
[session-123] STATE: Login → Dashboard [1.5s]
[session-123] OBSERVE: Form validation passed
[session-123] ERROR: Element not found - ElementNotFoundException
[session-123] PERF: Operation completed: DataLoad (3.2s)
```

### Structured Output (JSON)

```json
{
  "timestamp": 1704067200000,
  "sessionId": "session-123",
  "type": "ACTION",
  "level": "INFO",
  "action": "CLICK",
  "target": "LoginButton",
  "success": true,
  "duration": 250,
  "stateId": "LoginState",
  "metadata": {
    "matchCount": 1,
    "screenshot": "/tmp/screenshot.png"
  }
}
```

## Integration with Existing Systems

### SLF4J Integration

All logs are automatically routed to SLF4J with appropriate levels:

```java
// BrobotLogger call
logger.error("Critical failure", exception);

// Automatically logs to SLF4J as:
// ERROR [BrobotLogger] [session-123] Critical failure
```

### ActionLogger Compatibility

The unified logger maintains compatibility with the existing ActionLogger interface:

```java
// Actions are automatically logged to ActionLogger
logger.action("CLICK", button, result);
// Routes to: actionLogger.logAction(sessionId, result, objectCollection)

// State transitions
logger.transition(from, to, success, duration);
// Routes to: actionLogger.logStateTransition(...)
```

### ConsoleReporter Integration

Console output respects existing ConsoleReporter settings:

```java
// Set global console level
ConsoleReporter.outputLevel = OutputLevel.LOW;

// Or through unified logger
logger.setConsoleLevel(OutputLevel.HIGH);
```

## Best Practices

### 1. Use Session Scopes

Always wrap test executions in session scopes for proper correlation:

```java
@Test
public void testUserFlow() {
    try (var session = logger.session(generateSessionId())) {
        // All logs in this block are correlated
        performUserFlow();
    }
}
```

### 2. Log at Appropriate Levels

- **Actions**: User interactions (clicks, types, etc.)
- **Transitions**: State changes in the application
- **Observations**: Notable conditions or validations
- **Performance**: Timing-sensitive operations
- **Errors**: Failures and exceptions

### 3. Include Relevant Context

```java
logger.log()
    .action("SUBMIT_FORM")
    .target(form)
    .metadata("formData", sanitizeFormData(data))
    .metadata("validationErrors", errors)
    .screenshot(captureOnError ? screenshotPath : null)
    .log();
```

### 4. Use Timers for Performance Metrics

```java
try (var timer = logger.startTimer("DatabaseQuery")) {
    return executeQuery(sql);
} // Timer automatically logs duration
```

### 5. Leverage Metadata for Debugging

```java
// Add test context
logger.getContext().addMetadata("testCase", testName);
logger.getContext().addMetadata("dataSet", dataSetId);

// Add environment context
logger.getContext().addMetadata("browser", getBrowserInfo());
logger.getContext().addMetadata("viewport", getViewportSize());
```

## Migration Guide

### From ConsoleReporter

```java
// Old way
ConsoleReporter.print(match, stateObject, actionOptions);
ConsoleReporter.println(OutputLevel.HIGH, "Processing complete");

// New way
logger.action(actionOptions.getAction().toString(), stateObject);
logger.observation("Processing complete");
```

### From ActionLogger

```java
// Old way
actionLogger.logAction(sessionId, result, objectCollection);
actionLogger.logStateTransition(sessionId, from, to, before, success, time);

// New way
logger.action("CLICK", stateObject, result);
logger.transition(fromState, toState, success, time);
```

### From SLF4J

```java
// Old way
private static final Logger log = LoggerFactory.getLogger(MyClass.class);
log.info("Starting process");
log.error("Process failed", exception);

// New way
@Autowired
private BrobotLogger logger;

logger.observation("Starting process");
logger.error("Process failed", exception);
```

## Performance Considerations

- **Minimal Overhead**: The unified logger adds < 1% overhead compared to direct logging
- **Lazy Evaluation**: Expensive operations (like screenshots) are only performed when needed
- **Async Options**: Structured logging can be configured for async operation
- **Buffering**: File and network sinks support buffering for better performance

## Troubleshooting

### Common Issues

1. **Missing Context**: Ensure session is set before logging
   ```java
   // Always set session first
   try (var session = logger.session("test-123")) {
       // Now context is available
   }
   ```

2. **No Console Output**: Check console level setting
   ```java
   logger.setConsoleLevel(OutputLevel.HIGH);
   ```

3. **Lombok Errors**: IDE-specific Lombok issues don't affect runtime
   - Ensure Lombok plugin is installed
   - Enable annotation processing

4. **Thread Context Lost**: Use snapshots for async operations
   ```java
   var snapshot = logger.getContext().snapshot();
   // Pass snapshot to async operation
   ```

## Future Enhancements

- Cloud logging service integration (CloudWatch, Stackdriver)
- Real-time log streaming
- Advanced filtering and search capabilities
- Machine learning-based anomaly detection
- Distributed tracing support

## Action Logging with Console Output and Visual Feedback

The unified logging system now includes enhanced action logging with real-time console output and visual highlighting capabilities. This provides immediate feedback during automation development and debugging.

### Console Action Reporting

Get real-time feedback about action execution in the console:

```
🔍 FIND: login-button → ✓ FOUND (234ms)
   └─ Location: (450,320) Score: 98.5%

✓ CLICK login-button (156ms)

⌨️ TYPE: "test@example.com" ✓

✗ FIND submit-button (2003ms)
   └─ Search regions: 3 areas checked

⚠️ Performance Warning: FIND took 2003ms (threshold: 1000ms)
```

### Visual Highlighting

See exactly what Brobot is doing with configurable visual feedback:

- **Green borders** for successful finds
- **Blue borders** for search regions
- **Yellow ripple effects** for clicks
- **Red indicators** for errors

### GUI Access Detection

Automatic detection and reporting of GUI access problems:

```
❌ GUI Problem: No DISPLAY environment variable set
💡 Possible solutions:
   • Set DISPLAY=:0 for local display
   • For SSH: use -X or -Y flag for X11 forwarding
   • For Docker: pass --env DISPLAY=$DISPLAY
   • For WSL: install and configure X server (VcXsrv, Xming)
```

### Configuration

Configure action logging through properties:

```yaml
brobot:
  # Console output configuration
  console:
    actions:
      enabled: true
      level: NORMAL        # QUIET, NORMAL, VERBOSE
      show-match-details: true
      use-icons: true
  
  # Visual highlighting configuration
  highlight:
    enabled: true
    auto-highlight-finds: true
    find:
      color: "#00FF00"     # Green
      duration: 2.0        # seconds
    search-region:
      color: "#0000FF"     # Blue
      duration: 1.0
```

### Using Enhanced Action Logging

```java
@Autowired
private EnhancedActionLogger logger;

// Log with visual feedback
logger.logActionWithVisuals(
    "FIND", 
    target, 
    result,
    VisualFeedbackOptions.debug()  // Maximum visibility
);

// Check GUI access
if (!logger.checkAndLogGuiAccess()) {
    // Handle GUI access problems
}

// Custom visual options
VisualFeedbackOptions options = VisualFeedbackOptions.builder()
    .highlightFinds(true)
    .findHighlightColor(Color.YELLOW)
    .findHighlightDuration(3.0)
    .showMatchScore(true)
    .build();

logger.logActionWithVisuals("CLICK", button, result, options);
```

### Profile-Based Configuration

Use predefined profiles for different environments:

#### Development Profile
```bash
# Maximum visibility for debugging
java -jar app.jar --spring.profiles.active=visual-debug
```

#### CI/CD Profile
```yaml
# Minimal output, no visual distractions
brobot:
  console.actions.level: QUIET
  highlight.enabled: false
  gui-access.continue-on-error: true
```

#### Production Profile
```yaml
# Disable console output, keep error reporting
brobot:
  console.actions.enabled: false
  highlight.enabled: false
  gui-access.report-problems: true
```

### Visual Feedback Options

Create custom visual feedback for specific scenarios:

```java
// Highlight only successful finds
VisualFeedbackOptions.findsOnly()

// Debug mode - highlight everything
VisualFeedbackOptions.debug()

// No visual feedback
VisualFeedbackOptions.none()

// Custom configuration
VisualFeedbackOptions.builder()
    .highlightFinds(true)
    .highlightSearchRegions(true)
    .flashHighlight(true)
    .flashCount(3)
    .persistHighlight(true)  // Keep highlight until cleared
    .highlightLabel("Target Element")
    .build()
```

### Integration with Desktop Runner

The desktop runner automatically displays console output and visual feedback when configured. The `ConsoleActionEventListener` bridges the event system with the console reporter for real-time updates.

## API Reference

The main classes for the unified logging system are:

- `BrobotLogger` - The main logging facade
- `LogEvent` - The event model for structured logging
- `LoggingContext` - Thread-local context management
- `LogBuilder` - Fluent API for complex logging scenarios
- `EnhancedActionLogger` - Extended interface with visual feedback
- `VisualFeedbackOptions` - Configuration for visual highlighting

## Examples Repository

Find more examples in the [brobot-examples](https://github.com/jspinak/brobot-examples) repository under `/logging-examples`.