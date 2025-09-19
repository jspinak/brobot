# Headless Configuration Guide

## Overview

Starting with Brobot 1.1.0, headless mode is **explicitly configured** via properties rather than auto-detected. This simplifies the codebase and avoids issues with Java's unreliable `GraphicsEnvironment.isHeadless()` detection, particularly on Windows systems.

## Configuration

### Setting Headless Mode

Headless mode must be explicitly configured via properties:

```properties
# application.properties
# Explicitly set headless mode (default: false)
brobot.headless=false
```

### Property Values

| Value | Description | Use Case |
|-------|-------------|----------|
| `false` (default) | GUI is available | Normal desktop environments, Windows/Mac/Linux with displays |
| `true` | No display available | CI/CD pipelines, headless servers, Docker containers, test environments |

## How It Works

### Simple Configuration-Based Approach

The `HeadlessDetector` component simply reads the configured property value:

```java
@Component
public class HeadlessDetector {

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
brobot.mock=true  # Also enable mock mode for testing
```

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
// In tests, extend BrobotTestBase
public class MyTest extends BrobotTestBase {
    // Mock mode is automatically enabled
    // Headless-safe testing environment
}
```

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
   // Good
   @Autowired
   private HeadlessDetector headlessDetector;

   if (headlessDetector.isHeadless()) { }

   // Avoid - unreliable
   if (GraphicsEnvironment.isHeadless()) { }
   ```

## Related Configuration

### Mock Mode
Mock mode (simulated actions) is separate from headless mode:
- **Mock mode**: `brobot.mock=true` - Simulates actions for testing
- **Headless mode**: `brobot.headless=true` - No display available

Both can be used together for CI/CD testing.

### Screen Capture
When `brobot.headless=true`, ensure appropriate capture provider:
```properties
brobot.capture.provider=MOCK  # Use mock provider for headless
```

## Summary

- Headless mode is **explicitly configured** via `brobot.headless` property
- Auto-detection has been **removed** for simplicity and reliability
- Default is `false` (assumes display is available)
- No more complex workarounds or Robot initialization tricks
- SikuliX handles all low-level details automatically