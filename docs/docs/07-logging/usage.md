# Logging Usage Guide

## Transparent Logging - Zero Code Changes Required

Brobot's logging is completely transparent and configuration-driven. You don't need to use special logging services or modify your code. Just use the standard `Action` class, and logging happens automatically based on your `application.properties` configuration.

### Example: Standard Actions with Transparent Logging
```java
// Just write normal automation code - logging is automatic!
action.click(usernameField);
action.type("user123");
action.click(passwordField);
action.type(password);
action.click(loginButton);
```

### Automatic Log Output (when enabled in properties)
```
→ CLICK usernameField
✓ CLICK usernameField | loc:(245,180) | sim:0.91 | 32ms
→ TYPE "user123"
✓ TYPE "user123" | 125ms
→ CLICK passwordField
✓ CLICK passwordField | loc:(245,220) | sim:0.90 | 28ms
→ TYPE "********"
✓ TYPE "********" | 95ms
→ CLICK loginButton
✓ CLICK loginButton | loc:(520,380) | sim:0.92 | 45ms
```

### Controlling Logging via Configuration
```properties
# Enable action logging (in application.properties)
brobot.logging.categories.actions=INFO

# Or disable it completely
# brobot.logging.categories.actions=OFF

# Or get more details
# brobot.logging.categories.actions=DEBUG
```

## ActionConfig Custom Logging

Brobot provides built-in logging methods for all ActionConfig subclasses (PatternFindOptions, ClickOptions, TypeOptions, etc.) allowing you to add custom log messages at key points in the action lifecycle.

### Available Logging Methods

Each ActionConfig builder supports four logging methods:

- **`withBeforeActionLog(String message)`** - Logged before the action begins
- **`withAfterActionLog(String message)`** - Logged after the action completes
- **`withSuccessLog(String message)`** - Logged only when the action succeeds
- **`withFailureLog(String message)`** - Logged only when the action fails

### Basic Example
```java
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for submit button...")
    .withSuccessLog("Submit button found!")
    .withFailureLog("Submit button not found - check if page loaded correctly")
    .build();

// Use perform() with ActionConfig for custom messages
ActionResult result = action.perform(findOptions, submitButton);
```

### Generated Log Output
```
Searching for submit button...
→ FIND submitButton
✓ FIND submitButton | loc:(100,200) | sim:0.95 | 25ms
Submit button found!
```

### Complex Workflow Example
```java
// Step 1: Verify we're on the correct page
PatternFindOptions verifyPage = new PatternFindOptions.Builder()
    .withBeforeActionLog("Verifying arrival at login page...")
    .withSuccessLog("Login page confirmed")
    .withFailureLog("WARNING: Not on login page")
    .build();

// Step 2: Enter username
TypeOptions typeUsername = new TypeOptions.Builder()
    .withBeforeActionLog("Entering username...")
    .withAfterActionLog("Username entry complete")
    .build();

// Step 3: Enter password
TypeOptions typePassword = new TypeOptions.Builder()
    .withBeforeActionLog("Entering password...")
    .withAfterActionLog("Password entry complete")
    .build();

// Step 4: Click submit
ClickOptions submitClick = new ClickOptions.Builder()
    .withBeforeActionLog("Submitting login form...")
    .withSuccessLog("Login form submitted successfully")
    .withFailureLog("ERROR: Failed to submit login form")
    .build();

// Execute the workflow - use perform() for custom messages
action.perform(verifyPage, loginPageHeader);
action.perform(typeUsername, usernameField, username);
action.perform(typePassword, passwordField, password);
action.perform(submitClick, submitButton);
```

### State Transition Example
```java
public class LoginTransitions {

    @Autowired
    private Action action;

    public void transitionToInventory() {
        PatternFindOptions findInventory = new PatternFindOptions.Builder()
            .withBeforeActionLog("Navigating to Inventory...")
            .withSuccessLog("Successfully arrived at Inventory")
            .withFailureLog("Failed to reach Inventory - may need to close dialogs")
            .withSimilarity(0.85)
            .build();

        ClickOptions openInventory = new ClickOptions.Builder()
            .withBeforeActionLog("Opening Inventory menu...")
            .withAfterActionLog("Inventory menu interaction complete")
            .build();

        // Click to open, then verify arrival
        action.perform(openInventory, inventoryButton);
        action.perform(findInventory, inventoryHeader);
    }
}
```

### Debugging with Detailed Logging
```java
// Use detailed logging for debugging complex interactions
DragOptions complexDrag = new DragOptions.Builder()
    .withBeforeActionLog("Starting drag operation from item slot to storage...")
    .withAfterActionLog("Drag operation completed - checking result")
    .withSuccessLog("Item successfully moved to storage")
    .withFailureLog("Drag failed - item may be locked or storage full")
    .setFromLocation(itemSlot)
    .setToLocation(storageSlot)
    .setPauseBeforeBegin(0.5)  // Give UI time to respond
    .build();

action.drag(complexDrag);
```

### Conditional Logging Based on Context
```java
public PatternFindOptions buildFindOptions(boolean verbose) {
    PatternFindOptions.Builder builder = new PatternFindOptions.Builder()
        .withSimilarity(0.9);

    if (verbose) {
        builder.withBeforeActionLog("Performing high-precision search...")
               .withSuccessLog("High-precision match found")
               .withFailureLog("No match at 90% similarity");
    }

    return builder.build();
}
```

### Integration with Transitions
```java
public class StateTransitions {

    public PatternFindOptions arrivalVerification(String stateName) {
        return new PatternFindOptions.Builder()
            .withBeforeActionLog("Verifying arrival at " + stateName + "...")
            .withSuccessLog("Successfully arrived at " + stateName)
            .withFailureLog("Failed to confirm arrival at " + stateName)
            .withSearchRegion(SearchRegion.TOP_HALF)
            .build();
    }

    public ClickOptions navigationClick(String targetName) {
        return new ClickOptions.Builder()
            .withBeforeActionLog("Clicking " + targetName + "...")
            .withSuccessLog(targetName + " clicked successfully")
            .withFailureLog("Failed to click " + targetName)
            .setClickType(ClickType.LEFT)
            .build();
    }
}
```

## Session Management for Workflow Correlation

The `ActionSessionManager` provides session-based logging for tracking related actions across workflows. Sessions use SLF4J MDC (Mapped Diagnostic Context) to add correlation IDs to all logs within a session.

### Basic Session Usage
```java
@Autowired
private ActionSessionManager sessionManager;

public void processWorkflow() {
    // Start a named session
    sessionManager.startSession("Process Items Workflow");

    try {
        // Track each action in the session
        sessionManager.nextAction();
        action.click(inventoryButton);

        sessionManager.nextAction();
        action.click(processButton);

        sessionManager.nextAction();
        action.click(startButton);

    } finally {
        // Always end the session
        sessionManager.endSession();
    }
}
```

### Generated Session Logs
```
=== Starting Task: Process Items Workflow | Session: abc12345 ===
[session:abc12345 seq:001] → CLICK inventoryButton
[session:abc12345 seq:001] ✓ CLICK inventoryButton | loc:(100,50) | sim:0.92 | 45ms
[session:abc12345 seq:002] → CLICK processButton
[session:abc12345 seq:002] ✓ CLICK processButton | loc:(200,100) | sim:0.91 | 38ms
[session:abc12345 seq:003] → CLICK startButton
[session:abc12345 seq:003] ✓ CLICK startButton | loc:(300,200) | sim:0.93 | 42ms
=== Completed Task: Process Items Workflow | Session: abc12345 | Total Actions: 3 ===
```

### Automatic Session Management with Lambda
```java
// Session automatically starts and ends, even if exception occurs
sessionManager.executeWithSession("Login Flow", () -> {
    action.click(usernameField);
    action.type(username);
    action.click(passwordField);
    action.type(password);
    action.click(loginButton);
});
```

### Combining Sessions with Custom Messages
```java
sessionManager.startSession("Critical Operation");

PatternFindOptions options = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for critical element...")
    .withSuccessLog("Critical element found - proceeding")
    .withFailureLog("CRITICAL: Element not found - aborting")
    .build();

sessionManager.nextAction();
action.perform(options, criticalElement);

sessionManager.endSession();
```

### Best Practices for ActionConfig Logging

1. **Be Descriptive but Concise**
   ```java
   // Good - clear and informative
   .withBeforeActionLog("Validating form data before submission...")

   // Too vague
   .withBeforeActionLog("Processing...")

   // Too verbose
   .withBeforeActionLog("Now starting the process to validate all form fields including username, password, email...")
   ```

2. **Include Context in Failure Messages**
   ```java
   // Good - helps with debugging
   .withFailureLog("Login button not found - check if page fully loaded or if button moved")

   // Less helpful
   .withFailureLog("Action failed")
   ```

3. **Use Success Logs for Important Milestones**
   ```java
   // Good - marks important workflow points
   .withSuccessLog("Order successfully submitted - Order ID captured")

   // Unnecessary - duplicates automatic logging
   .withSuccessLog("Click successful")
   ```

4. **Combine with Other ActionConfig Options**
   ```java
   PatternFindOptions robust = new PatternFindOptions.Builder()
       .withBeforeActionLog("Searching with reduced similarity...")
       .withSimilarity(0.7)  // Lower threshold
       .withSearchRegion(SearchRegion.FULL_SCREEN)  // Wider search
       .setPauseBeforeBegin(1.0)  // Allow page to stabilize
       .withFailureLog("Element not found even with relaxed criteria")
       .build();
   ```

## Enhanced Action Logging Service

Brobot provides an `ActionLoggingService` that wraps the standard Action class with automatic logging, session management, and formatted output.

### Using ActionLoggingService

```java
@Autowired
private ActionLoggingService actionLogger;

// Simple usage - automatic logging of attempts and results
actionLogger.click(submitButton);  // Logs: → CLICK submitButton
                                   // Logs: ✓ CLICK submitButton | loc:(100,200) | sim:0.95 | 25ms
```

### Session Management

Track related actions with session correlation:

```java
// Start a session for related actions
actionLogger.startSession("User Registration");

// All subsequent actions are tracked within this session
actionLogger.find(registrationForm);
actionLogger.type(emailField, "user@example.com");
actionLogger.type(passwordField, "password");
actionLogger.click(submitButton);

// End the session with summary
actionLogger.endSession();
// Logs: === Completed Task: User Registration | Session: a1b2c3d4 | Total Actions: 4 ===
```

### Automatic Session Management

```java
// Execute with automatic session lifecycle management
actionLogger.executeWithSession("Login Flow", () -> {
    actionLogger.find(loginPage);
    actionLogger.type(usernameField, username);
    actionLogger.type(passwordField, password);
    actionLogger.click(loginButton);
}); // Session automatically ended even if exception occurs
```

### Formatted Action Output

The service provides consistent, concise formatting:
- **Success**: `✓ CLICK saveButton | loc:(520,380) | sim:0.92 | 45ms`
- **Failure**: `✗ FIND submitButton | NOT FOUND | 1500ms`
- **Attempt**: `→ TYPE[user@example.com] emailField`

### Integration with ActionConfig Logging

ActionLoggingService works seamlessly with ActionConfig custom logging:

```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for save button...")
    .withSuccessLog("Save button found!")
    .build();

// Both custom and automatic logging will occur
actionLogger.find(options, saveButton);
// Logs: Searching for save button...
// Logs: → FIND saveButton
// Logs: ✓ FIND saveButton | loc:(100,50) | sim:0.95 | 30ms
// Logs: Save button found!
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