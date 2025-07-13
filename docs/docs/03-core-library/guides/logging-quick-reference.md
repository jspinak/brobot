---
sidebar_position: 6
---

# Logging Quick Reference

A concise reference for the Brobot Unified Logging System.

## Basic Logging

```java
@Autowired
private BrobotLogger logger;

// Simple actions
logger.action("CLICK", button);
logger.action("TYPE", textField, result);

// State transitions
logger.transition(fromState, toState);
logger.transition(fromState, toState, success, duration);

// Observations
logger.observation("Validation passed");
logger.observation("Warning: Slow response", "WARNING");

// Errors
logger.error("Element not found", exception);
```

## Session Management

```java
// Session scope (recommended)
try (var session = logger.session("test-123")) {
    // All logs include session ID
}

// Operation scope (with timing)
try (var op = logger.operation("ComplexTask")) {
    // Duration logged on close
}

// Manual timer
try (var timer = logger.startTimer("Process")) {
    // Timer stops on close
}
```

## Builder Pattern

```java
logger.log()
    .action("CLICK")
    .target(element)
    .success(true)
    .duration(250)
    .screenshot("/tmp/screen.png")
    .metadata("retry", 2)
    .log();
```

## Context & Metadata

```java
// Add persistent metadata
logger.getContext().addMetadata("env", "staging");
logger.getContext().addMetadata("suite", "regression");

// Set current context
logger.getContext().setCurrentState(state);
logger.getContext().setCurrentAction("CLICK");
```

## Configuration

```java
// Console output level
logger.setConsoleLevel(OutputLevel.HIGH);  // HIGH, LOW, NONE

// Structured logging
logger.enableStructuredLogging(true);
```

## Common Patterns

### Test Execution
```java
@Test
public void testLogin() {
    try (var session = logger.session(getTestId())) {
        logger.observation("Starting login test");
        
        LoginPage loginPage = new LoginPage();
        logger.action("NAVIGATE", loginPage);
        
        ActionResult result = loginPage.login(user, pass);
        logger.action("LOGIN", loginPage, result);
        
        if (!result.isSuccess()) {
            logger.error("Login failed", null);
        }
    }
}
```

### Error Handling
```java
try {
    performAction();
} catch (Exception e) {
    logger.log()
        .error(e)
        .screenshot(captureScreenshot())
        .metadata("lastAction", getLastAction())
        .log();
}
```

### Performance Tracking
```java
try (var timer = logger.startTimer("DataLoad")) {
    List<Data> data = loadData();
    logger.observation("Loaded " + data.size() + " records");
}
```

### Async Operations
```java
var snapshot = logger.getContext().snapshot();
CompletableFuture.runAsync(() -> {
    logger.getContext().restore(snapshot);
    logger.action("ASYNC_TASK", target);
});
```

## Output Examples

### Console Output
```
[abc-123] CLICK → LoginButton (success) [250ms]
[abc-123] STATE: Login → Dashboard [1.5s]
[abc-123] OBSERVE: 5 records loaded
[abc-123] ERROR: Timeout waiting for element
```

### Structured (JSON)
```json
{
  "timestamp": 1704067200000,
  "sessionId": "abc-123",
  "type": "ACTION",
  "action": "CLICK",
  "target": "LoginButton",
  "success": true,
  "duration": 250
}
```

## Console Levels

| Level | Shows |
|-------|-------|
| NONE  | No output |
| LOW   | Actions, transitions, errors |
| HIGH  | All logs including observations |

## Event Types

| Type | Usage | Method |
|------|-------|---------|
| ACTION | User interactions | `logger.action()` |
| TRANSITION | State changes | `logger.transition()` |
| OBSERVATION | General info | `logger.observation()` |
| PERFORMANCE | Timing data | `logger.operation()` |
| ERROR | Failures | `logger.error()` |

## Metadata Keys

Common metadata keys used by the framework:

- `screenshot` - Path to screenshot file
- `matchCount` - Number of pattern matches found
- `retryCount` - Number of retry attempts
- `elementLocation` - Coordinates of element
- `validationErrors` - List of validation failures
- `formData` - Submitted form data (sanitized)
- `responseTime` - Server response time
- `browser` - Browser name and version
- `viewport` - Screen dimensions

## Color Codes

When console colors are enabled:

- 🟢 **Green** - Successful actions
- 🔵 **Blue** - State transitions  
- 🟡 **Yellow** - Failed actions (non-error)
- 🔴 **Red** - Errors and exceptions
- 🟦 **Cyan** - Performance metrics
- ⚪ **Default** - Observations and info

## Enhanced Action Logging

### Console Output Configuration
```java
// Enable enhanced console output
@Autowired
private EnhancedActionLogger logger;

// Set verbosity
logger.setConsoleVerbosity("VERBOSE");  // QUIET, NORMAL, VERBOSE

// Check GUI access
logger.checkAndLogGuiAccess();
```

### Visual Feedback
```java
// Log with visual highlighting
logger.logActionWithVisuals(
    "FIND", 
    target, 
    result,
    VisualFeedbackOptions.debug()
);

// Custom visual options
VisualFeedbackOptions options = VisualFeedbackOptions.builder()
    .highlightFinds(true)
    .findHighlightColor(Color.GREEN)
    .findHighlightDuration(3.0)
    .showMatchScore(true)
    .build();
```

### Console Output Examples
```
🔍 FIND: login-button → ✓ FOUND (234ms)
   └─ Location: (450,320) Score: 98.5%

✗ FIND submit-button (2003ms)
   └─ Search regions: 3 areas checked

❌ GUI Problem: No DISPLAY environment variable set
💡 Solution: Set DISPLAY=:0 or run with X11 forwarding

⚠️ Performance Warning: FIND took 2003ms (threshold: 1000ms)
```

### Configuration Properties
```yaml
brobot:
  console:
    actions:
      enabled: true
      level: NORMAL
      show-match-details: true
      use-icons: true
  highlight:
    enabled: true
    auto-highlight-finds: true
    find:
      color: "#00FF00"
      duration: 2.0
```

### Visual Feedback Presets
```java
// Debug mode - everything visible
VisualFeedbackOptions.debug()

// Production - no visual distractions  
VisualFeedbackOptions.none()

// Finds only
VisualFeedbackOptions.findsOnly()
```