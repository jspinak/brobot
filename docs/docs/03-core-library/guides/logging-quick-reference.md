---
sidebar_position: 6
---

# Logging Quick Reference

A concise reference for the Brobot Unified Logging System.

> **Note**: For action-specific logging with configurable output formats (QUIET, NORMAL, VERBOSE), see the [Modular Logging Guide](./modular-logging-guide.md).
> 
> **New**: For automatic action logging without boilerplate code, see the [Automatic Action Logging Guide](./automatic-action-logging.md).

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

## Pattern Matching Diagnostics

### DiagnosticLogger
```java
@Autowired
private DiagnosticLogger diagnosticLogger;

// Automatic logging in ScenePatternMatcher
// Provides verbosity-aware output:
// - QUIET: Only ✓/✗ symbols
// - NORMAL: [SEARCH], [RESULT], [FOUND] prefixes
// - VERBOSE: Full details with caching info

// Manual diagnostic logging
diagnosticLogger.logPatternSearch(pattern, scene, 0.7);
diagnosticLogger.logPatternResult(pattern, matchCount, bestScore);
diagnosticLogger.logImageAnalysis(patternImg, sceneImg, "button");
diagnosticLogger.logSimilarityAnalysis("button", thresholds, foundAt, score);
```

### Diagnostic Output Examples
```
# NORMAL Mode:
[SEARCH] Pattern: 'login-button' (64x32) | Similarity: 0.70 | Scene: 1920x1080
[FOUND #1] Score: 0.852 at (450, 320)
[RESULT] 2 matches for 'login-button' | Best score: 0.852

# VERBOSE Mode adds:
  [Pattern.sikuli()] Using CACHED SikuliX Pattern for: login-button
  [FOUND #4] Score: 0.743 at (450, 720)
  [METADATA] matchCount=2, cacheHit=true, searchTime=234ms

# Failed Match Analysis:
[RESULT] NO MATCHES for 'submit-button'
  [IMAGE ANALYSIS]
    Pattern: 128x48 type=RGB bytes=24KB
    Pattern content: 2.3% black, 45.6% white, avg RGB=(127,189,210)
    Scene: 1920x1080 type=RGB bytes=8MB
    Scene content: 95.2% black, 0.1% white, avg RGB=(5,5,5)
    WARNING: Scene is mostly BLACK - possible capture failure!
  [SIMILARITY ANALYSIS]
    Threshold 0.7: No match
    Threshold 0.6: FOUND with score 0.624
```

### Configuration for Diagnostics
```yaml
brobot:
  logging:
    verbosity: VERBOSE  # QUIET, NORMAL, or VERBOSE
  console:
    actions:
      level: VERBOSE
logging:
  level:
    io.github.jspinak.brobot.logging.DiagnosticLogger: DEBUG
```

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