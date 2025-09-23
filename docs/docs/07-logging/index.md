# Brobot Logging System

## Transparent, Configuration-Driven Logging

The Brobot framework provides transparent, built-in logging that requires no code changes or special services. Simply use the standard `Action` class methods (`click()`, `find()`, `type()`, etc.), and logging happens automatically based on your `application.properties` configuration.

**Key Principle**: Logging is a cross-cutting concern that should be transparent to your automation code. You write normal automation logic, and Brobot handles the logging based on your configuration.

## Core Concepts

### Log Levels
Brobot uses industry-standard SLF4J/Log4j2 log levels:
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

Add custom log messages directly to your action configurations:

Add custom log messages directly to your action configurations:

```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for login button...")
    .withSuccessLog("Login button found!")
    .withFailureLog("Login button not found - check page state")
    .build();

// Use perform() method with ActionConfig for custom logging
action.perform(options, loginButton);
```

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

- [Configuration Guide](configuration.md) - Detailed configuration options
- [Usage Guide](usage.md) - How to use logging in your code
- [Output Formats](output-formats.md) - Available output formats and examples
- [Performance](performance.md) - Performance considerations and optimizations

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