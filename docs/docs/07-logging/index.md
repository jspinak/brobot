---
sidebar_position: 1
title: Logging Overview
description: Introduction to Brobot's transparent, configuration-driven logging system
---

# Brobot Logging System

## Transparent, Configuration-Driven Logging

The Brobot framework provides built-in logging capabilities. For `find()` operations, logging happens automatically. For other actions (`click()`, `type()`, `move()`, etc.), you can add custom log messages via ActionConfig methods to provide context about what your automation is doing.

**Key Principle**: Logging is a cross-cutting concern that should be transparent to your automation code. You write normal automation logic, and Brobot handles the logging based on your configuration.

**Current Status**: Automatic logging without custom messages is fully implemented for Find operations. Other actions require custom log messages via `withBeforeActionLog()`, `withSuccessLog()`, and `withFailureLog()` methods.

## Core Concepts

### Log Levels
Brobot uses industry-standard SLF4J with Logback log levels:
- **OFF** - No logging
- **ERROR** - Error conditions requiring attention
- **WARN** - Warning conditions and potential issues
- **INFO** - Key business events and action results
- **DEBUG** - Detailed debugging information
- **TRACE** - Most detailed information including method entry/exit

### Log Categories
Different aspects of automation are organized into categories:
- **ACTIONS** - User actions (click, type, find)
- **TRANSITIONS** - State transitions and navigation
- **MATCHING** - Pattern matching details
- **PERFORMANCE** - Timing and performance metrics
- **STATE** - State management events
- **LIFECYCLE** - Application lifecycle events
- **VALIDATION** - Input validation and checks
- **SYSTEM** - System-level events
- **EXECUTION** - Execution control events (pause, resume, stop)

### Custom Log Messages

Add custom log messages directly to your action configurations:

```java
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.report.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginFlow {

    @Autowired
    private Action action;

    public boolean findLoginButton(StateImage loginButton) {
        // PatternFindOptions with custom logging
        PatternFindOptions options = new PatternFindOptions.Builder()
            .withBeforeActionLog("Searching for login button...")
            .withSuccessLog("Login button found!")
            .withFailureLog("Login button not found - check page state")
            .setMaxSearchTime(5.0)
            .setSimilarity(0.85)
            .build();

        // Perform find with custom logging
        ActionResult result = action.perform(options, loginButton);
        return result.isSuccess();
    }
}
```

**Note**: The `loginButton` parameter is a `StateImage` object typically obtained from a `@State` class. See the [Usage Guide](usage.md) for complete examples including State definitions.

### Sample Output

With transparent logging enabled, you'll see:

**Standard actions (automatic logging only):**
```
→ CLICK usernameField
✓ CLICK usernameField | loc:(245,180) | sim:0.91 | 32ms
→ TYPE "user123"
✓ TYPE "user123" | 125ms
→ CLICK loginButton
✓ CLICK loginButton | loc:(520,380) | sim:0.92 | 45ms
```

**With custom messages:**
```
Searching for login button...
→ FIND loginButton
✓ FIND loginButton | loc:(520,380) | sim:0.92 | 45ms
Login button found!
```

**Failed action:**
```
→ CLICK submitButton
✗ CLICK submitButton | NOT FOUND | 5003ms
```

## Documentation Structure

### Logging System Guides
- **[Configuration Guide](configuration.md)** - Detailed configuration options and property reference
- **[Usage Guide](usage.md)** - How to use logging in your code with examples
- **[Output Formats](output-formats.md)** - Available output formats (SIMPLE, STRUCTURED, JSON)
- **[Performance](performance.md)** - Performance considerations and optimizations

## Key Features

1. **Transparent Logging** - No code changes needed, just configuration
2. **Configuration-Driven** - Control everything via application.properties
3. **Hierarchical Configuration** - Global settings cascade to specific categories
4. **Custom Messages** - Add context with ActionConfig logging methods
5. **Session Management** - Track workflows with ActionSessionManager
6. **Visual Indicators** - Clear symbols (→ ✓ ✗) for action status
7. **Concise Format** - One-line summaries with essential information
8. **Multiple Output Formats** - SIMPLE, STRUCTURED, and JSON
9. **Performance Optimized** - Minimal overhead with early filtering
10. **No Special Services** - Just use the standard Action class

## Related Documentation

### Configuration & Setup
- **[BrobotProperties Usage Guide](../03-core-library/configuration/brobot-properties-usage.md)** - How to access logging properties in code
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete reference for all Brobot properties
- **[Auto-Configuration Guide](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Logging in headless/CI environments
- **[Initial States Configuration](../03-core-library/configuration/initial-states.md)** - Startup logging configuration

### Testing & Debugging
- **[Mock Mode Guide](../04-testing/mock-mode-guide.md)** - Testing with logging in mock mode
- **[Profile-Based Testing](../04-testing/profile-based-testing.md)** - Using profiles to configure logging levels
- **[Integration Testing](../04-testing/integration-testing.md)** - Testing with logging enabled
- **[Action Recording](../04-testing/action-recording.md)** - Recording actions with ActionHistory
- **[Image Find Debugging](../03-core-library/tools/image-find-debugging.md)** - Comprehensive debugging system

### Core Library & Actions
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - Using custom logging methods (`withBeforeActionLog`, `withSuccessLog`, `withFailureLog`)
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Examples of ActionConfig with logging
- **[Convenience Methods](../03-core-library/action-config/18-convenience-methods.md)** - Action class convenience methods with automatic logging
- **[Quick Start Guide](../01-getting-started/quick-start.md)** - Getting started with Brobot
- **[AI Project Creation Guide](../01-getting-started/ai-brobot-project-creation.md)** - Complete guide with logging best practices