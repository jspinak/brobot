# Headless Configuration Guide

## Overview

Starting with Brobot 1.1.x (September 2025), headless mode is **explicitly configured** via properties rather than auto-detected. This simplifies the codebase and avoids issues with Java's unreliable `GraphicsEnvironment.isHeadless()` detection, particularly on Windows systems.

For comprehensive configuration options, see the [Brobot Properties Usage Guide](./brobot-properties-usage.md).

## Configuration

### Setting Headless Mode

Headless mode must be explicitly configured via properties:

```properties
# application.properties
# Explicitly set headless mode (default: false)
brobot.headless=false
```

:::info Property Path Options
Brobot supports two property paths for headless configuration:
- **`brobot.headless`** - Flat property path (used by `HeadlessDetector` via `@Value` injection)
- **`brobot.core.headless`** - Nested property path (used in `BrobotProperties` configuration)

Both paths reference the same configuration value. Use `brobot.headless` for simplicity in `application.properties`.
:::

### Property Values

| Value | Description | Use Case |
|-------|-------------|----------|
| `false` (default) | GUI is available | Normal desktop environments, Windows/Mac/Linux with displays |
| `true` | No display available | CI/CD pipelines, headless servers, Docker containers, test environments |

## How It Works

### Simple Configuration-Based Approach

The `HeadlessDetector` component simply reads the configured property value:

```java
package io.github.jspinak.brobot.config.environment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HeadlessDetector {

    private final boolean headlessMode;

    public HeadlessDetector(@Value("${brobot.headless:false}") boolean brobotHeadless) {
        // Uses configured value ONLY - no auto-detection
        this.headlessMode = brobotHeadless;
    }

    public boolean isHeadless() {
        return headlessMode;  // Returns configured value
    }
}
```

### No More Complex Workarounds

Previous versions attempted various workarounds for headless detection issues. These have been **removed** in favor of the simpler approach:

- ❌ **Removed**: `ForceNonHeadlessInitializer`
- ❌ **Removed**: `RobotForcedInitializer`
- ❌ **Removed**: Direct Robot manipulation
- ✅ **Kept**: Simple property-based configuration

## Common Scenarios

### Desktop Development (Windows/Mac/Linux)

```properties
# Default configuration - no changes needed
# brobot.headless defaults to false
```

### CI/CD Pipeline (GitHub Actions, Jenkins)

```properties
# application-ci.properties
brobot.headless=true
brobot.mock=true  # Enable mock mode for testing without real UI interactions
```

:::tip Mock Mode vs Headless Mode
- **Mock mode** (`brobot.mock=true`) simulates actions for testing - see [Mock Mode Guide](../../04-testing/mock-mode-guide.md)
- **Headless mode** (`brobot.headless=true`) indicates no display is available
- Use both together in CI/CD for complete test environment setup
:::

### Docker Container

```properties
# application-docker.properties
brobot.headless=true
brobot.mock=true
```

### WSL2 Development

```properties
# WSL2 with WSLg (GUI support)
brobot.headless=false

# WSL2 without display
brobot.headless=true
```

## Why We Changed

### The Problem with Auto-Detection

Java's `GraphicsEnvironment.isHeadless()` has several issues:

1. **Cached Results**: Once called, the result is cached and cannot change
2. **Gradle Interference**: Gradle often sets `java.awt.headless=true` before the JVM starts
3. **Timing Issues**: Early initialization can lock in the wrong headless state
4. **Platform Inconsistency**: Different behavior on different operating systems

### The Solution: Explicit Configuration

By using explicit configuration:
- **Predictable**: You know exactly what mode you're in
- **Simple**: No complex detection logic or workarounds
- **Reliable**: Works consistently across all platforms
- **Testable**: Easy to test both modes

## Troubleshooting

### Issue: Getting HeadlessException

**Symptom**: `HeadlessException` when running automation

**Solution**:
1. Ensure `brobot.headless=false` in your properties
2. If using Gradle, add to `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Djava.awt.headless=false
   ```

### Issue: Click Actions Not Working

**Symptom**: Images are found but clicks fail

**Solution**:
- This is likely a headless detection issue
- Ensure `brobot.headless=false` is set
- The click action now uses SikuliX directly, which handles Robot initialization lazily

### Issue: Different Behavior in Tests

**Symptom**: Tests behave differently than production

**Solution**: Ensure consistent configuration:
```java
package com.example.automation;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.Action;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// In tests, extend BrobotTestBase for headless-safe environment
public class MyTest extends BrobotTestBase {

    @Autowired
    private Action action;

    @Test
    public void testMyAutomation() {
        // Mock mode is automatically enabled
        // Headless-safe testing environment
        // Test your automation here
    }
}
```

For comprehensive testing guidance, see:
- [Unit Testing Guide](../../04-testing/unit-testing.md)
- [Integration Testing Guide](../../04-testing/integration-testing.md)
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md)

## Best Practices

1. **Always set explicitly for production**: Don't rely on defaults
   ```properties
   brobot.headless=false  # Explicitly set for your environment
   ```

2. **Use profiles for different environments**:
   ```properties
   # application-dev.properties
   brobot.headless=false

   # application-ci.properties
   brobot.headless=true
   ```

3. **Use HeadlessDetector, not GraphicsEnvironment**:
   ```java
   package com.example.automation;

   import io.github.jspinak.brobot.config.environment.HeadlessDetector;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Component;
   import java.awt.GraphicsEnvironment;

   @Component
   public class MyAutomation {

       @Autowired
       private HeadlessDetector headlessDetector;

       public void performAction() {
           // Good - uses Brobot's reliable configuration
           if (headlessDetector.isHeadless()) {
               // Handle headless case
           }

           // Avoid - unreliable due to caching and platform issues
           if (GraphicsEnvironment.isHeadless()) {
               // This may give incorrect results
           }
       }
   }
   ```

## Related Configuration

### Mock Mode
Mock mode (simulated actions) is separate from headless mode:
- **Mock mode**: `brobot.mock=true` - Simulates actions for testing - see [Mock Mode Guide](../../04-testing/mock-mode-guide.md)
- **Headless mode**: `brobot.headless=true` - No display available

Both can be used together for CI/CD testing. When mock mode is enabled, Brobot automatically uses `MockScreenCaptureService` for screen capture operations.

### Screen Capture
When running in headless environments, screen capture is typically handled by enabling mock mode:
```properties
brobot.headless=true
brobot.mock=true  # Automatically uses MockScreenCaptureService
```

For advanced capture configuration, see the [Modular Capture System documentation](../capture/modular-capture-system.md). Valid capture provider values include: `JAVACV_FFMPEG`, `ROBOT`, `FFMPEG`, `SIKULIX`, `AUTO`.

## Summary

- Headless mode is **explicitly configured** via `brobot.headless` property
- Auto-detection has been **removed** for simplicity and reliability
- Default is `false` (assumes display is available)
- No more complex workarounds or Robot initialization tricks
- SikuliX handles all low-level details automatically

## See Also

### Configuration Guides
- [Brobot Properties Usage Guide](./brobot-properties-usage.md) - Complete property reference
- [Auto-Configuration Guide](./auto-configuration.md) - Spring Boot auto-configuration details
- [Modular Capture System](../capture/modular-capture-system.md) - Screen capture configuration

### Testing Documentation
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Testing with simulated actions
- [Unit Testing Guide](../../04-testing/unit-testing.md) - Writing unit tests for Brobot
- [Integration Testing Guide](../../04-testing/integration-testing.md) - Integration test patterns
- [Profile-Based Testing](../../04-testing/profile-based-testing.md) - Environment-specific test configuration

### Related Topics
- [Pure Actions Quick Start](../../01-getting-started/pure-actions-quickstart.md) - Basic action usage
- [States in Brobot](../../01-getting-started/states.md) - State management concepts