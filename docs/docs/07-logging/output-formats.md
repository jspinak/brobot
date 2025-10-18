---
sidebar_position: 3
title: Output Formats
description: Guide to SIMPLE, STRUCTURED, and JSON output formats with AnsiColor visual symbols
---

# Output Formats

The Brobot logging system supports three output formats, each optimized for different use cases.

## SIMPLE Format

Human-readable format for development and debugging.

### Configuration
```properties
brobot.logging.output.format=SIMPLE
```

### Example Output
```
[14:23:45.123] [ACTIONS] INFO - Searching for login button... [FIND->submitButton]
[14:23:45.148] [ACTIONS] INFO - Login button found! [FIND->submitButton] ✓ (95%) (0.025s) @(100,200)
[14:23:45.163] [ACTIONS] INFO - Clicked submit button [CLICK->submitButton] ✓ (0.015s) @(100,200)
[14:23:45.313] [TRANSITIONS] INFO - Navigated to Settings [state_change] ✓ (0.150s)
[14:23:50.456] [PERFORMANCE] INFO - Batch processing completed [batch_process] ✓ (5.000s)
[14:23:50.457] [MATCHING] WARN - Low similarity match detected (65%)
[14:23:50.500] [SYSTEM] ERROR - Connection failed: timeout after 30s ✗
```

**Format Breakdown:**
- `[HH:mm:ss.SSS]` - Timestamp (if `include-timestamp=true`)
- `[CATEGORY]` - Log category (ACTIONS, TRANSITIONS, etc.)
- `LEVEL` - Log level (INFO, WARN, ERROR, DEBUG)
- `Message` - Custom or automatic log message
- `[ACTION->TARGET]` - Action type and target object
- `✓/✗` - Success/failure symbol
- `(XX%)` - Similarity score (for pattern matching)
- `(X.XXXs)` - Duration in seconds
- `@(x,y)` - Location coordinates (if applicable)

### When to Use
- During development
- Manual debugging sessions
- Quick console monitoring
- Local testing

## STRUCTURED Format

Key-value format for log aggregation tools.

### Configuration
```properties
brobot.logging.output.format=STRUCTURED
brobot.logging.output.include-timestamp=true
brobot.logging.output.include-correlation-id=true
```

### Example Output
```
timestamp=2024-01-15T10:30:45.123Z category=ACTIONS level=INFO thread=main correlation=a1b2c3d4 message="Searching for login button..." action.type=FIND action.target=submitButton
timestamp=2024-01-15T10:30:45.148Z category=ACTIONS level=INFO thread=main correlation=a1b2c3d4 message="Login button found!" action.type=FIND action.target=submitButton action.success=true timing.duration=0.025s match.similarity=95% location.x=100 location.y=200
timestamp=2024-01-15T10:30:45.163Z category=ACTIONS level=INFO thread=main correlation=a1b2c3d4 message="Clicked submit button" action.type=CLICK action.target=submitButton action.success=true timing.duration=0.015s
timestamp=2024-01-15T10:30:45.313Z category=TRANSITIONS level=INFO thread=main correlation=a1b2c3d4 message="Navigated to Settings" state.from=MainMenu state.to=Settings action.success=true timing.duration=0.150s
```

**Format Breakdown:**
- Pure space-separated `key=value` pairs
- Nested properties use dot notation: `action.type`, `timing.duration`, `location.x`
- String values with spaces are quoted: `message="..."`
- Timestamps in ISO-8601 format
- Consistent field ordering: timestamp, category, level, thread, correlation, message, then contextual fields

### Key-Value Pairs
- Easy to parse with tools like grep, awk, sed
- Compatible with log aggregation systems (Splunk, ELK)
- Maintains readability while being machine-parseable
- Grep-friendly: `grep 'action.type=FIND' logs.txt`

### When to Use
- Test environments
- Log aggregation with tools like Splunk, ELK
- When you need both human and machine readability
- Debugging with correlation tracking

## JSON Format

Machine-readable format for automated processing and monitoring.

### Configuration
```properties
brobot.logging.output.format=JSON
brobot.logging.output.include-timestamp=true
brobot.logging.output.include-correlation-id=true
brobot.logging.output.include-state-context=true
```

### Example Output
```json
{
  "@timestamp": "2024-01-15T10:30:45.148Z",
  "@version": "1",
  "category": "ACTIONS",
  "level": "INFO",
  "message": "Login button found!",
  "thread": "main",
  "correlation": {
    "id": "a1b2c3d4",
    "session": "session_12345",
    "operation": "user-login"
  },
  "action": {
    "type": "FIND",
    "target": "submitButton",
    "success": true
  },
  "timing": {
    "duration": 25,
    "durationFormatted": "0.025s"
  },
  "match": {
    "similarity": 0.95,
    "similarityPercent": "95%"
  },
  "location": {
    "x": 100,
    "y": 200
  },
  "state": {
    "current": "MainMenu"
  }
}
```

**Format Breakdown:**
- `@timestamp`, `@version` - ELK Stack standard fields
- Nested objects for logical grouping: `correlation`, `action`, `timing`, `match`, `location`, `state`
- camelCase naming convention for fields
- Both numeric and formatted values (e.g., `duration: 25` and `durationFormatted: "0.025s"`)
- Hierarchical structure for easy querying in JSON-aware tools

### Complex Event Example
```json
{
  "@timestamp": "2024-01-15T10:30:50.456Z",
  "@version": "1",
  "category": "PERFORMANCE",
  "level": "INFO",
  "message": "Batch processing completed",
  "thread": "worker-pool-1",
  "correlation": {
    "id": "a1b2c3d4",
    "session": "session_12345",
    "operation": "batch_process"
  },
  "timing": {
    "duration": 5000,
    "durationFormatted": "5.000s",
    "breakdown": {
      "initialization": 100,
      "processing": 4500,
      "cleanup": 400
    }
  },
  "performance": {
    "itemsProcessed": 150,
    "throughput": 30.0,
    "memoryUsedBytes": 52428800,
    "peakMemoryMb": 75
  }
}
```

### When to Use
- Production environments
- Integration with monitoring tools (Datadog, New Relic, CloudWatch)
- Automated log analysis
- Metrics collection and alerting
- Long-term log storage

## Visual Symbols and Colors

Brobot uses Unicode symbols and ANSI colors for clear, visually-appealing console output through the `AnsiColor` utility class.

### Symbol Reference

The following symbols are available in `io.github.jspinak.brobot.debug.AnsiColor`:

| Symbol | Unicode | Constant | Common Usage |
|--------|---------|----------|--------------|
| ✓ | Check mark | `AnsiColor.CHECK` | Success indicators |
| ✗ | X mark | `AnsiColor.CROSS` | Failure indicators |
| → | Right arrow | `AnsiColor.ARROW_RIGHT` | Action progression, transitions |
| ← | Left arrow | `AnsiColor.ARROW_LEFT` | Backward navigation |
| ↑ | Up arrow | `AnsiColor.ARROW_UP` | Scroll up, priority increase |
| ↓ | Down arrow | `AnsiColor.ARROW_DOWN` | Scroll down, priority decrease |
| • | Bullet | `AnsiColor.BULLET` | List items |
| ★ | Star | `AnsiColor.STAR` | Important items |
| ⚠ | Warning | `AnsiColor.WARNING_SIGN` | Warnings, cautions |
| ℹ | Info | `AnsiColor.INFO_SIGN` | Informational messages |
| ⏳ | Hourglass | `AnsiColor.HOURGLASS` | Wait operations |
| 🕐 | Clock | `AnsiColor.CLOCK` | Timing information |

### Color Codes

Colors are automatically enabled via the Jansi library, which provides ANSI support on all platforms including Windows.

**Standard Colors:**
```java
import io.github.jspinak.brobot.debug.AnsiColor;

// Available standard color constants
AnsiColor.RED       // \u001B[31m
AnsiColor.GREEN     // \u001B[32m
AnsiColor.YELLOW    // \u001B[33m
AnsiColor.BLUE      // \u001B[34m
AnsiColor.MAGENTA   // \u001B[35m
AnsiColor.CYAN      // \u001B[36m

// Example usage
System.out.println(AnsiColor.RED + "Red text" + AnsiColor.RESET);
System.out.println(AnsiColor.GREEN + "Green text" + AnsiColor.RESET);
```

**Semantic Colors (Recommended):**
```java
import io.github.jspinak.brobot.debug.AnsiColor;

// Semantic color constants (recommended for logging)
AnsiColor.SUCCESS   // Bright green - successful operations
AnsiColor.ERROR     // Bright red - errors and failures
AnsiColor.WARNING   // Bright yellow - warnings
AnsiColor.INFO      // Bright cyan - informational messages
AnsiColor.DEBUG     // Bright magenta - debug information
AnsiColor.HEADER    // Bold bright blue - section headers
AnsiColor.DIMMED    // Bright black/gray - secondary info

// Example usage with helper methods
System.out.println(AnsiColor.success("Operation completed successfully"));
System.out.println(AnsiColor.error("Critical error occurred"));
System.out.println(AnsiColor.warning("Warning: threshold exceeded"));
System.out.println(AnsiColor.info("Information: process started"));
```

### Platform Support

**Cross-Platform Compatibility:**
- **Windows**: Full Unicode and ANSI support via Jansi library
- **Mac/Linux**: Native terminal support
- **WSL**: Full support when using Windows Terminal
- **CI/CD**: Works in most modern CI environments (GitHub Actions, GitLab CI, Jenkins)

**Terminal Detection:**
AnsiColor automatically initializes Jansi on Windows:
```java
import org.fusesource.jansi.AnsiConsole;

/**
 * This is a code snippet from io.github.jspinak.brobot.debug.AnsiColor
 * showing how Jansi is automatically initialized on Windows platforms.
 *
 * This code is for reference only - it runs automatically when AnsiColor is loaded.
 * You don't need to include this in your code.
 */
static {
    AnsiConsole.systemInstall();
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
        System.out.println("[Brobot Debug] Windows detected - ANSI colors enabled via Jansi");
    }
}
```

> **💡 Note**: There is NO automatic fallback to ASCII (`[OK]`, `[FAIL]`, etc.). Brobot uses Unicode symbols consistently across all platforms. If symbols don't display correctly, upgrade your terminal rather than expecting ASCII fallbacks.

### Usage Examples

**Basic Colorization:**
```java
import io.github.jspinak.brobot.debug.AnsiColor;

// Simple colored text
System.out.println(AnsiColor.success("Operation completed!"));
System.out.println(AnsiColor.error("Failed to connect"));
System.out.println(AnsiColor.warning("Connection timeout approaching"));

// Custom color
String message = AnsiColor.colorize("Custom message", AnsiColor.CYAN);
```

**Symbols in Log Messages:**
```java
import io.github.jspinak.brobot.debug.AnsiColor;

public class SymbolsExample {
    public void demonstrateSymbols() {
        // Success/failure indicators
        System.out.println(AnsiColor.CHECK + " Test passed");
        System.out.println(AnsiColor.CROSS + " Test failed");

        // Action progression
        System.out.println(AnsiColor.ARROW_RIGHT + " Processing next item");

        // Warnings
        System.out.println(AnsiColor.WARNING_SIGN + " Low memory warning");
    }
}
```

**Formatted Boxes:**
```java
import io.github.jspinak.brobot.debug.AnsiColor;

public class BoxesExample {
    public void demonstrateBoxes() {
        String boxContent = AnsiColor.box(
            "Test Results",
            "Passed: 45\nFailed: 2\nSkipped: 3",
            AnsiColor.SUCCESS
        );
        System.out.println(boxContent);
    }
}
```

**Combined Usage:**
```java
import io.github.jspinak.brobot.debug.AnsiColor;

public class CombinedUsageExample {
    public void demonstrateActionLogging() {
        // Action logging pattern - before action
        System.out.println(
            AnsiColor.ARROW_RIGHT + " " +
            AnsiColor.info("CLICK") + " " +
            "submitButton"
        );

        // Action logging pattern - after successful action
        System.out.println(
            AnsiColor.CHECK + " " +
            AnsiColor.success("CLICK") + " " +
            "submitButton | loc:(245,180) | 32ms"
        );
    }
}
```

### Box Drawing Characters

Available for creating visual structures:

| Character | Constant | Unicode |
|-----------|----------|---------|
| ╔ | `BOX_TOP_LEFT` | Double line |
| ╗ | `BOX_TOP_RIGHT` | Double line |
| ╚ | `BOX_BOTTOM_LEFT` | Double line |
| ╝ | `BOX_BOTTOM_RIGHT` | Double line |
| ═ | `BOX_HORIZONTAL` | Double line |
| ║ | `BOX_VERTICAL` | Double line |
| ─ | `BOX_HORIZONTAL_LIGHT` | Single line |

### Troubleshooting Display Issues

**Symbols Not Displaying:**
1. **Windows**: Use Windows Terminal instead of legacy Command Prompt
2. **WSL**: Set `TERM=xterm-256color` in your shell
3. **IDE**: Most modern IDEs support Unicode (IntelliJ IDEA, VS Code, Eclipse)
4. **SSH**: Ensure your SSH client supports UTF-8 (PuTTY requires configuration)

**Colors Not Showing:**
- Jansi should handle this automatically on all platforms
- If colors still don't work, check terminal's ANSI support
- Colors are automatically disabled if terminal doesn't support ANSI

**Missing Jansi Dependency:**
If you see `[Brobot Debug] Windows detected - ANSI colors enabled via Jansi` at startup, colors are working.
If this message doesn't appear on Windows, verify Jansi is in your dependencies:
```gradle
implementation 'org.fusesource.jansi:jansi:2.4.0'
```

## Format Comparison

| Feature | SIMPLE | STRUCTURED | JSON |
|---------|--------|------------|------|
| Human Readable | ✅ Excellent | ✅ Good | ⚠️ Moderate |
| Machine Parseable | ❌ Poor | ✅ Good | ✅ Excellent |
| Storage Efficiency | ✅ High | ⚠️ Medium | ❌ Low |
| Tool Compatibility | ❌ Limited | ✅ Good | ✅ Excellent |
| Debugging Ease | ✅ Excellent | ✅ Good | ⚠️ Moderate |
| Production Ready | ❌ No | ✅ Yes | ✅ Yes |

## Custom Formatting

You can implement custom formatters by implementing the `LogFormatter` interface.

### LogEntry Fields

The `LogEntry` class provides extensive data for custom formatting:

**Core Fields:**
- `timestamp`, `category`, `level`, `message`
- `threadName`, `correlationId`, `sessionId`, `operationName`

**Action Fields:**
- `actionType`, `actionTarget`, `success`
- `similarity` (for pattern matching actions)
- `location` (x, y coordinates)

**Performance Fields:**
- `duration`, `operationStartTime`, `operationDepth`
- `memoryUsage`, `operationCount`

**Error Fields:**
- `error` (Throwable), `errorMessage`

**State Fields:**
- `currentState`, `targetState`

**Metadata:**
- `metadata` (Map&lt;String, Object&gt;) for custom key-value data

### Custom Formatter Implementation

You can implement custom formatters by implementing the `LogFormatter` interface:

```java
package com.example.brobot.logging;

import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.logging.LogFormatter;
import io.github.jspinak.brobot.logging.LogEntry;

/**
 * Example custom formatter implementation.
 * Implements the LogFormatter interface to provide custom log formatting.
 */
@Component
public class CustomFormatter implements LogFormatter {

    @Override
    public String format(LogEntry entry) {
        // Custom formatting logic
        return String.format("[%s] %s: %s",
            entry.getCategory(),
            entry.getLevel(),
            entry.getMessage());
    }
}
```

Register your custom formatter in the configuration:

```java
package com.example.brobot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.BrobotLoggerImpl;
import io.github.jspinak.brobot.logging.LogFormatter;
import io.github.jspinak.brobot.logging.LoggingConfiguration;
import io.github.jspinak.brobot.logging.CorrelationContext;

/**
 * Example configuration class showing how to register a custom formatter.
 */
@Configuration
public class CustomFormatterConfiguration {

    @Autowired
    private CorrelationContext correlationContext;

    @Bean
    public BrobotLogger brobotLogger(LoggingConfiguration config) {
        LogFormatter formatter = new CustomFormatter();
        return new BrobotLoggerImpl(config, formatter, correlationContext);
    }
}
```

## Output Examples by Category

### Actions Category
```
# SIMPLE
[ACTIONS] INFO  CLICK loginButton → SUCCESS [15ms] loc:(500,300)

# STRUCTURED
[ACTIONS] CLICK loginButton | success=true duration=15 location=(500,300)

# JSON
{"category":"ACTIONS","level":"INFO","action_type":"CLICK","target":"loginButton","success":true,"duration_ms":15}
```

### Transitions Category
```
# SIMPLE
[TRANSITIONS] INFO  MainMenu → Settings SUCCESS [200ms]

# STRUCTURED
[TRANSITIONS] state_change | from=MainMenu to=Settings success=true duration=200

# JSON
{"category":"TRANSITIONS","from":"MainMenu","to":"Settings","success":true,"duration_ms":200}
```

### Performance Category
```
# SIMPLE
[PERFORMANCE] INFO  batch_process [5000ms] items:150 throughput:30/s

# STRUCTURED
[PERFORMANCE] batch_process | duration=5000 items=150 throughput=30

# JSON
{"category":"PERFORMANCE","operation":"batch_process","duration_ms":5000,"items":150,"throughput":30}
```

## Related Documentation

### Logging System Overview
- **[Logging Overview](index.md)** - Introduction to Brobot's transparent, configuration-driven logging system
- **[Logging Configuration Guide](configuration.md)** - Comprehensive configuration options for output formats and enrichment
- **[Logging Usage Guide](usage.md)** - How to use custom logging with ActionConfig methods
- **[Logging Performance Guide](performance.md)** - Performance impact and optimization strategies for different output formats

### Configuration & Properties
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete reference for all `brobot.logging.output.*` properties
- **[BrobotProperties Usage Guide](../03-core-library/configuration/brobot-properties-usage.md)** - How to access logging properties programmatically
- **[Auto-Configuration Guide](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration of logging system
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Output format considerations in headless environments

### ActionConfig & Custom Logging
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - ActionConfig logging methods (withBeforeActionLog, withSuccessLog, withFailureLog)
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical examples of ActionConfig with custom logging messages
- **[Convenience Methods](../03-core-library/action-config/18-convenience-methods.md)** - Automatic logging via action convenience methods

### Testing & Debugging
- **[Testing Introduction](../04-testing/testing-intro.md)** - Testing approaches and output format recommendations for test environments
- **[Mock Mode Guide](../04-testing/mock-mode-guide.md)** - Using output formats in mock mode testing
- **[Profile-Based Testing](../04-testing/profile-based-testing.md)** - Configure output formats per test profile
- **[Image Find Debugging](../03-core-library/tools/image-find-debugging.md)** - Comprehensive debugging system using AnsiColor visual symbols

### Getting Started
- **[Quick Start Guide](../01-getting-started/quick-start.md)** - Getting started with Brobot logging
- **[Installation Guide](../01-getting-started/installation.md)** - Installing Brobot with Jansi library for ANSI color support

### Integration & Advanced
- **[Integrations Overview](../06-integrations/overview.md)** - Using JSON output format with integration platforms and monitoring tools

### External References
- **[Jansi Library](https://github.com/fusesource/jansi)** - ANSI color library providing Windows support
- **[SLF4J Documentation](https://www.slf4j.org/manual.html)** - Logging facade used by Brobot
- **[Logback Documentation](https://logback.qos.ch/manual/)** - Logging implementation configuration
- **[Spring Boot Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)** - Spring Boot logging framework reference