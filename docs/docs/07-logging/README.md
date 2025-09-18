# Brobot Logging Documentation

Welcome to the Brobot logging documentation. This folder contains comprehensive guides for understanding and using Brobot's logging capabilities.

## Quick Start Guides

### 1. [Options Logging Guide](options-logging-guide.md) ⭐ NEW
Learn how to use embedded logging methods in Options builders for clean, contextual logging without cluttering your code.

### 2. [Logging Quick Reference](logging-quick-reference.md)
A concise reference for all logging methods, configuration properties, and common patterns.

## Core Documentation

### System Design
- **[Logging Architecture](logging-architecture.md)** - Deep dive into the logging system's design patterns, components, and extension points
- **[Modular Logging System](modular-logging-system.md)** - Architecture of the modular logging components and formatters
- **[Unified Logging System](unified-logging-system.md)** - Overview of the unified logging facade that consolidates all logging

### Implementation Guides
- **[Automatic Action Logging](automatic-action-logging.md)** - How actions are automatically logged throughout their lifecycle
- **[Action Logging Console Visual](action-logging-console-visual.md)** - Console output formatting and visual feedback configuration
- **[Modular Logging Guide](modular-logging-guide.md)** - Step-by-step guide to using the modular logging system

### Migration
- **[Logging Migration Guide](logging-migration-guide.md)** - Comprehensive guide for migrating from legacy logging to the unified system

## Topics by Use Case

### For Developers Writing Automation Code

**Start Here:** [Options Logging Guide](options-logging-guide.md)

The most common way to add logging is through Options builders:
```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for submit button...")
    .withSuccessLog("Submit button found at {location}")
    .withFailureLog("Submit button not found - check if page loaded")
    .build();
```

### For Framework Configuration

**Key Files:**
- [Unified Logging System](unified-logging-system.md) - Configuration via application.properties
- [Logging Quick Reference](logging-quick-reference.md) - All configuration properties

**Common Settings:**
```properties
# Enable verbose logging
brobot.logging.verbosity=VERBOSE

# Enable console action output
brobot.console.actions.enabled=true
brobot.console.actions.level=VERBOSE
```

### For Custom Extensions

**Architecture Guides:**
- [Logging Architecture](logging-architecture.md) - Extension points and custom handlers
- [Modular Logging System](modular-logging-system.md) - Creating custom formatters

### For Debugging and Diagnostics

**Diagnostic Features:**
- Pattern matching diagnostics
- Failed match analysis
- Performance tracking
- Visual feedback during development

See [Action Logging Console Visual](action-logging-console-visual.md) for console output configuration.

## Key Concepts

### 1. Embedded Logging in Options
All Options builders that extend `ActionConfig` provide logging methods:
- `withBeforeActionLog()` - Log before action execution
- `withSuccessLog()` - Log on successful completion
- `withFailureLog()` - Log on failure
- `withAfterActionLog()` - Log after action (regardless of result)
- `withLogging()` - Full control over logging configuration
- `withNoLogging()` - Disable logging for this action

### 2. Logging Levels
- **QUIET** - Minimal output, errors only
- **NORMAL** - Standard logging with key events
- **VERBOSE** - Detailed logging with diagnostics

### 3. Log Event Types
- `ACTION` - Action execution events
- `ERROR` - Error conditions
- `DEBUG` - Detailed debugging information
- `INFO` - General information
- `WARN` - Warning conditions

### 4. Unified Facade
The `BrobotLogger` provides a unified interface that:
- Consolidates multiple logging systems (SLF4J, Console, Action logging)
- Maintains thread-local context
- Supports structured metadata
- Enables session-scoped logging

## Common Patterns

### Pattern 1: Transition Logging
```java
@OutgoingTransition(to = NextState.class)
public boolean toNextState() {
    ClickOptions options = new ClickOptions.Builder()
        .withBeforeActionLog("Navigating to NextState...")
        .withSuccessLog("Successfully navigated to NextState")
        .withFailureLog("Failed to navigate - button not found")
        .build();

    return action.click(button, options).isSuccess();
}
```

### Pattern 2: Verification Logging
```java
@IncomingTransition
public boolean verifyArrival() {
    PatternFindOptions options = new PatternFindOptions.Builder()
        .withBeforeActionLog("Verifying arrival at {stateName}...")
        .withSuccessLog("Confirmed arrival at {stateName}")
        .withFailureLog("Not at expected state - markers not found")
        .setWaitTime(5.0)
        .build();

    return action.find(stateMarker, options).isSuccess();
}
```

### Pattern 3: Process Execution Logging
```java
public void executeProcess() {
    TypeOptions typeOptions = new TypeOptions.Builder()
        .withBeforeActionLog("Entering search term...")
        .withSuccessLog("Search term entered successfully")
        .withFailureLog("Failed to enter search term")
        .build();

    action.type("search term", typeOptions);
}
```

## Best Practices

1. **Use Descriptive Messages** - Include context about what's being attempted and why
2. **Add Failure Context** - Help users understand why something failed and what to check
3. **Leverage Placeholders** - Use `{location}`, `{count}`, `{text}` for dynamic values
4. **Configure Globally** - Set default verbosity in application.properties
5. **Override Locally** - Use Options builders for specific action logging needs

## Related Documentation

- [Brobot Testing Guide](../04-testing/testing-intro.md) - Testing with mock mode and logging
- [AI Brobot Project Creation](../01-getting-started/ai-brobot-project-creation.md) - Complete Brobot patterns including logging
- [Special Keys Guide](../03-core-library/keyboard/special-keys-guide.md) - How to use special keyboard keys in Brobot

## Getting Help

If you need assistance with logging:
1. Check the [Logging Quick Reference](logging-quick-reference.md)
2. Review the [Options Logging Guide](options-logging-guide.md) for embedded logging patterns
3. Consult the [Logging Migration Guide](logging-migration-guide.md) if updating legacy code
4. See the [Unified Logging System](unified-logging-system.md) for configuration details