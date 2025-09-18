# Options Logging Methods Guide

## Overview

Brobot provides powerful embedded logging capabilities directly in all Options builder classes. This allows you to add contextual logging without cluttering your code with separate logging statements.

## Available Logging Methods

All Options classes that extend `ActionConfig` inherit the following logging methods in their builders:

### Core Logging Methods

1. **`withBeforeActionLog(String message)`**
   - Logs a message before the action is executed
   - Useful for indicating what the system is attempting to do
   ```java
   PatternFindOptions options = new PatternFindOptions.Builder()
       .withBeforeActionLog("Searching for submit button...")
       .build();
   ```

2. **`withSuccessLog(String message)`**
   - Logs a message when the action completes successfully
   - Can include placeholders like `{location}` that are replaced with actual values
   ```java
   ClickOptions options = new ClickOptions.Builder()
       .withSuccessLog("Successfully clicked button at {location}")
       .build();
   ```

3. **`withFailureLog(String message)`**
   - Logs a message when the action fails
   - Essential for debugging and understanding automation failures
   ```java
   TypeOptions options = new TypeOptions.Builder()
       .withFailureLog("Failed to type text - check if field is accessible")
       .build();
   ```

4. **`withAfterActionLog(String message)`**
   - Logs a message after the action completes, regardless of success/failure
   - Useful for debugging action completion timing
   ```java
   ScrollOptions options = new ScrollOptions.Builder()
       .withAfterActionLog("Scroll action completed")
       .build();
   ```

### Advanced Logging Configuration

5. **`withLogging(Consumer<LoggingOptionsBuilder> configurator)`**
   - Provides full control over all logging settings
   - Allows customization of log levels and conditions
   ```java
   PatternFindOptions options = new PatternFindOptions.Builder()
       .withLogging(log -> log
           .beforeActionMessage("Starting search...")
           .successMessage("Found match!")
           .failureMessage("No match found")
           .logOnSuccess(true)
           .logOnFailure(true)
           .successLevel(LogEventType.ACTION)
           .failureLevel(LogEventType.ERROR)
       )
       .build();
   ```

6. **`withNoLogging()`**
   - Disables all automatic logging for this action
   - Useful for repetitive actions where logging would be excessive
   ```java
   MouseMoveOptions options = new MouseMoveOptions.Builder()
       .withNoLogging()  // Disable logging for mouse movement
       .build();
   ```

## Available in All Options Classes

The logging methods are available in all Options classes that extend `ActionConfig`:

- **Find Operations**: `PatternFindOptions`, `ColorFindOptions`
- **Click Operations**: `ClickOptions`, `DoubleClickOptions`, `RightClickOptions`
- **Type Operations**: `TypeOptions`, `KeyDownOptions`, `KeyUpOptions`
- **Mouse Operations**: `MouseMoveOptions`, `MouseDownOptions`, `MouseUpOptions`, `ScrollOptions`
- **Drag Operations**: `DragOptions`
- **Region Operations**: `DefineRegionOptions`
- **Visual Operations**: `HighlightOptions`

## Best Practices

### 1. Use Descriptive Messages
```java
// Good
.withBeforeActionLog("Opening Black Spirit's Adventure window...")
.withSuccessLog("Black Spirit's Adventure window opened successfully")
.withFailureLog("Failed to open Black Spirit's Adventure - die icon not found")

// Less descriptive
.withBeforeActionLog("Opening...")
.withSuccessLog("Opened")
.withFailureLog("Failed")
```

### 2. Include Context in Failure Messages
```java
.withFailureLog("Submit button not found - check if page loaded completely")
.withFailureLog("Failed to type in field - ensure field is visible and not disabled")
.withFailureLog("Storage Keeper dialog not opened - make sure you're near the NPC")
```

### 3. Use Placeholders for Dynamic Values
```java
.withSuccessLog("Found {count} matches at {location}")
.withSuccessLog("Clicked at position {x}, {y}")
.withSuccessLog("Typed '{text}' successfully")
```

### 4. Combine with Timing Options
```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for save button...")
    .withSuccessLog("Save button found and ready")
    .withFailureLog("Save button not found - document may not be ready")
    .setPauseBeforeBegin(0.5)  // Wait before searching
    .setPauseAfterEnd(1.0)      // Wait after finding
    .setWaitTime(5.0)           // Maximum time to search
    .build();
```

## Complete Example

```java
@TransitionSet(state = MainScreenState.class)
@RequiredArgsConstructor
@Slf4j
public class MainScreenTransitions {

    private final MainScreenState mainScreenState;
    private final Action action;

    @OutgoingTransition(to = BlackSpiritsAdventureState.class, priority = 1)
    public boolean toBlackSpiritsAdventure() {
        // Clean, embedded logging - no separate log statements needed
        ClickOptions clickOptions = new ClickOptions.Builder()
            .withBeforeActionLog("Opening Black Spirit's Adventure...")
            .withSuccessLog("Black Spirit's Adventure opened successfully")
            .withFailureLog("Failed to open Black Spirit's Adventure - die icon not found")
            .setPauseBeforeBegin(0.5)
            .setPauseAfterEnd(1.5)
            .build();

        return action.click(mainScreenState.getDie(), clickOptions).isSuccess();
    }

    @IncomingTransition
    public boolean verifyArrival() {
        // Verification with detailed logging
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .withBeforeActionLog("Verifying arrival at MainScreen...")
            .withSuccessLog("Successfully arrived at MainScreen")
            .withFailureLog("Failed to verify MainScreen - check if game is running")
            .setWaitTime(5.0)
            .setPauseAfterEnd(0.5)
            .build();

        return action.find(mainScreenState.getDie(), findOptions).isSuccess() ||
               action.find(mainScreenState.getProcessing(), findOptions).isSuccess();
    }
}
```

## Log Levels

The logging system supports different log levels for different scenarios:

- `LogEventType.ACTION` - Standard action logging (default for most messages)
- `LogEventType.ERROR` - Error conditions (default for failure messages)
- `LogEventType.DEBUG` - Detailed debugging information
- `LogEventType.INFO` - General information
- `LogEventType.WARN` - Warning conditions

You can customize log levels using the `withLogging()` method:

```java
.withLogging(log -> log
    .beforeActionLevel(LogEventType.DEBUG)
    .successLevel(LogEventType.INFO)
    .failureLevel(LogEventType.ERROR)
)
```

## Configuration via application.properties

Global logging behavior can be configured in `application.properties`:

```properties
# Enable verbose logging for all actions
brobot.logging.verbosity=VERBOSE

# Enable console action logging
brobot.console.actions.enabled=true
brobot.console.actions.level=VERBOSE

# Control specific log types
brobot.logging.before-action=true
brobot.logging.after-action=false
brobot.logging.success=true
brobot.logging.failure=true
```

## Summary

The embedded logging methods in Options builders provide:
- **Clean code** - No separate logging statements cluttering your logic
- **Contextual messages** - Logging tied directly to the action being performed
- **Automatic timing** - Logs appear at the right moment in the action lifecycle
- **Consistent format** - All actions log in a standardized way
- **Easy debugging** - Clear messages help identify exactly where automation fails