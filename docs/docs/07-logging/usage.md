---
sidebar_position: 2
title: Usage Guide
description: Complete guide to using Brobot logging with ActionConfig, sessions, and programmatic logging
---

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
# Use standard Spring Boot logging properties
logging.level.io.github.jspinak.brobot.action=INFO

# Or disable it completely
# logging.level.io.github.jspinak.brobot.action=OFF

# Or get more details
# logging.level.io.github.jspinak.brobot.action=DEBUG

# Control other Brobot packages
# logging.level.io.github.jspinak.brobot.statemanagement=INFO
# logging.level.io.github.jspinak.brobot.matching=WARN
```

## ActionConfig Custom Logging

Brobot provides built-in logging methods for all ActionConfig subclasses (PatternFindOptions, ClickOptions, TypeOptions, etc.) allowing you to add custom log messages at key points in the action lifecycle.

### Complete Example Template

All code examples in this guide assume a Spring component with standard Brobot imports:

```java
package com.example.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.composites.methods.find.PatternFindOptions;
import io.github.jspinak.brobot.actions.composites.methods.click.ClickOptions;
import io.github.jspinak.brobot.actions.composites.methods.type.TypeOptions;
import io.github.jspinak.brobot.actions.composites.methods.drag.DragOptions;
import io.github.jspinak.brobot.reports.ActionResult;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.logging.correlation.ActionSessionManager;

@Component
public class MyAutomation {

    @Autowired
    private Action action;

    @Autowired
    private ActionSessionManager sessionManager;

    // Your automation methods here
    // StateImage objects typically come from @State classes
}
```

> **Note**: StateImage objects (like `submitButton`, `usernameField`) are typically defined in `@State` classes. See the [States Guide](../01-getting-started/states.md) for details on creating state objects.

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

## Session Management for Correlated Actions

Use `ActionSessionManager` to track related actions within a logical workflow. Sessions automatically add correlation IDs to all logs, making it easy to trace complete workflows in production.

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
import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import java.time.Duration;

// Log action events
ActionEvent event = ActionEvent.builder()
    .actionType("CLICK")
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
import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import java.time.Duration;

ActionEvent event = ActionEvent.builder()
    .actionType("FIND")
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
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;

// Check if logging is enabled before expensive operations
if (logger.isLoggingEnabled(LogCategory.MATCHING, LogLevel.DEBUG)) {
    // Expensive operation only when DEBUG is enabled
    String details = generateDetailedReport();
    logger.debug(LogCategory.MATCHING, details);
}
```

> **Note**: `isLoggingEnabled()` currently delegates to SLF4J/Logback for level checking based on `logging.level.*` properties.

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
import io.github.jspinak.brobot.logging.events.ActionEvent;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import java.time.Duration;

// Good - structured event with metadata
ActionEvent event = ActionEvent.success("CLICK", "button", Duration.ofMillis(50));
logger.logAction(event);

// Less ideal - plain text
logger.info(LogCategory.ACTIONS, "Clicked button in 50ms");
```

## Related Documentation

### Logging System Components
- **[Logging Overview](./index.md)** - Comprehensive introduction to Brobot's transparent, configuration-driven logging system with key concepts
- **[Logging Configuration Guide](./configuration.md)** - Configuration properties, presets, and programmatic setup for all logging features
- **[Output Formats](./output-formats.md)** - Detailed guide to SIMPLE, STRUCTURED, and JSON output formats with visual symbols and examples
- **[Logging Performance Guide](./performance.md)** - Performance impact analysis, optimization strategies, and production configurations

### ActionConfig and Logging
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - Understanding ActionConfig hierarchy and builder patterns for implementing logging methods
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical examples demonstrating custom logging with withBeforeActionLog(), withSuccessLog(), and withFailureLog()
- **[ActionConfig API Reference](../03-core-library/action-config/05-reference.md)** - Complete reference for all ActionConfig methods including custom logging methods
- **[Convenience Methods](../03-core-library/action-config/18-convenience-methods.md)** - Simpler API patterns with automatic logging that don't require custom messages
- **[Action Chaining](../03-core-library/action-config/07-action-chaining.md)** - Chaining patterns and logging across multiple actions with then()
- **[ActionResult Components](../03-core-library/action-config/17-actionresult-components.md)** - Understanding ActionResult structure to extract logged action data

### State Management and Logging
- **[States Guide](../01-getting-started/states.md)** - @State annotation and StateImage definition with logging integration in state workflows
- **[Transitions Guide](../01-getting-started/transitions.md)** - @Transition annotations with logging examples in @IncomingTransition and @OutgoingTransition methods

### Configuration and Properties
- **[BrobotProperties Usage Guide](../03-core-library/configuration/brobot-properties-usage.md)** - How to access and configure logging properties programmatically in your code
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete reference for all brobot.logging.*, logging.level.*, and related properties
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Logging configuration for headless and CI/CD environments

### Testing with Logging
- **[Mock Mode Guide](../04-testing/mock-mode-guide.md)** - Testing with logging enabled in mock mode with optimized timing (0.01-0.04 seconds)
- **[Testing Introduction](../04-testing/testing-intro.md)** - Testing best practices with logging enabled and expected performance
- **[Profile-Based Testing](../04-testing/profile-based-testing.md)** - Configuring different logging levels and formats per test profile

### Getting Started
- **[Quick Start Guide](../01-getting-started/quick-start.md)** - Getting started with Brobot and logging in basic automation
- **[Installation Guide](../01-getting-started/installation.md)** - Installing Brobot with required logging dependencies
- **[AI Brobot Project Creation](../01-getting-started/ai-brobot-project-creation.md)** - Comprehensive project setup guide with logging best practices

### External References
- **[Spring Boot Logging Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)** - Official Spring Boot logging configuration reference
- **[SLF4J Documentation](https://www.slf4j.org/manual.html)** - SLF4J logging facade used by Brobot
- **[Logback Documentation](https://logback.qos.ch/manual/)** - Logback configuration and appender reference