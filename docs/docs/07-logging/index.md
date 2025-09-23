# Brobot Logging System

The Brobot framework provides a comprehensive, structured logging system designed specifically for GUI automation. The system uses hierarchical configuration, industry-standard log levels, and multiple output formats to provide clear visibility into automation execution.

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

## Quick Start

### Basic Configuration

```properties
# application.properties

# Set global log level (applies to all categories)
brobot.logging.global-level=INFO

# Override specific categories if needed
brobot.logging.categories.actions=DEBUG
brobot.logging.categories.matching=WARN

# Choose output format
brobot.logging.output.format=SIMPLE
```

### Using ActionConfig Logging

Add custom log messages directly to your action configurations:

```java
PatternFindOptions options = new PatternFindOptions.Builder()
    .withBeforeActionLog("Searching for login button...")
    .withSuccessLog("Login button found!")
    .withFailureLog("Login button not found - check page state")
    .build();

action.find(options, loginButton);
```

### Sample Output

With the above configuration, you'll see:
```
[ACTIONS] INFO  Searching for login button...
[ACTIONS] INFO  FIND loginButton → SUCCESS [25ms] loc:(100,200) sim:0.95
[ACTIONS] INFO  Login button found!
```

For standard actions without custom logging:
```
[ACTIONS] INFO  FIND submitButton → SUCCESS [25ms] loc:(100,200) sim:0.95
[ACTIONS] DEBUG   Details: 3 matches found
[TRANSITIONS] INFO  MainMenu → LoginPage SUCCESS [150ms]
```

## Documentation Structure

- [Configuration Guide](configuration.md) - Detailed configuration options
- [Usage Guide](usage.md) - How to use logging in your code
- [Output Formats](output-formats.md) - Available output formats and examples
- [Performance](performance.md) - Performance considerations and optimizations

## Key Features

1. **Hierarchical Configuration** - Global settings cascade to specific categories
2. **Structured Events** - Rich event objects with metadata and context
3. **Multiple Output Formats** - SIMPLE, STRUCTURED, and JSON formats
4. **Correlation Tracking** - Track related operations with correlation IDs
5. **Performance Optimized** - Minimal overhead with early filtering
6. **Clean Architecture** - No legacy code or backward compatibility baggage
7. **ActionConfig Logging** - Built-in logging methods for all action configurations