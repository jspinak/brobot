# Headless Configuration Guide

## Overview

Brobot's headless detection has been redesigned to use explicit configuration rather than auto-detection. This change was made due to reliability issues with automatic detection, particularly on Windows systems where `GraphicsEnvironment.isHeadless()` could incorrectly return `true` even with displays available.

## Configuration

### Setting Headless Mode

Headless mode must be explicitly configured via properties:

```properties
# application.properties
# Explicitly set headless mode (default: false)
brobot.headless=false

# Enable debug logging for headless detection
brobot.headless.debug=false
```

### Property Values

| Value | Description | Use Case |
|-------|-------------|----------|
| `false` (default) | GUI is available | Normal desktop environments, Windows/Mac/Linux with displays |
| `true` | No display available | CI/CD pipelines, headless servers, Docker containers |

## How It Works

### HeadlessDetector

The `HeadlessDetector` component reads the configured property value:

```java
@Component
public class HeadlessDetector {

    public HeadlessDetector(
        @Value("${brobot.headless:false}") boolean brobotHeadless,
        @Value("${brobot.headless.debug:false}") boolean debugEnabled) {
        // Uses configured value, NOT auto-detection
        this.headlessMode = brobotHeadless;
    }

    public boolean isHeadless() {
        return headlessMode;  // Returns configured value
    }
}
```

### ForceNonHeadlessInitializer

To prevent Java's `GraphicsEnvironment` from incorrectly detecting headless mode, Brobot includes `ForceNonHeadlessInitializer`:

```java
public class ForceNonHeadlessInitializer {
    static {
        // Runs very early in application startup
        // Sets java.awt.headless=false before AWT classes load
        // Attempts to override incorrect GraphicsEnvironment initialization
    }
}
```

This initializer:
1. Sets `java.awt.headless=false` system property early
2. Attempts to override `GraphicsEnvironment` if already initialized
3. Forces Windows toolkit on Windows systems
4. Logs diagnostic information about the headless state

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

## Troubleshooting

### Issue: HeadlessException on Windows

**Symptom**: Getting `HeadlessException` even though Windows has displays

**Solution**:
1. Ensure `brobot.headless=false` in properties
2. Add JVM argument: `-Djava.awt.headless=false`
3. Check Gradle properties for `systemProp.java.awt.headless=false`

### Issue: GraphicsEnvironment.isHeadless() Returns True

**Symptom**: Java's `GraphicsEnvironment` incorrectly detects headless

**Explanation**: This is why Brobot uses explicit configuration. The `ForceNonHeadlessInitializer` attempts to fix this, but if `GraphicsEnvironment` is already initialized by Gradle or another tool, it may be too late.

**Solution**:
- Use `HeadlessDetector.isHeadless()` instead of `GraphicsEnvironment.isHeadless()`
- The HeadlessDetector uses your configured value, not auto-detection

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

## Debugging Headless Issues

### Enable Debug Logging

```properties
brobot.headless.debug=true
```

This will log:
- Current `java.awt.headless` property value
- `GraphicsEnvironment.isHeadless()` result
- Configured `brobot.headless` value
- Whether `ForceNonHeadlessInitializer` had to override settings

### Run Diagnostic Tool

```bash
# From project root
java -cp library/build/classes/java/main io.github.jspinak.brobot.debug.HeadlessDebugger
```

This tool provides comprehensive information about:
- System properties
- JVM arguments
- Environment variables
- GraphicsEnvironment state
- Display availability

## Migration from Auto-Detection

If you're upgrading from a version that used auto-detection:

### Before (Auto-Detection)
```java
// Old approach - unreliable
if (GraphicsEnvironment.isHeadless()) {
    // Handle headless
}
```

### After (Explicit Configuration)
```java
@Autowired
private HeadlessDetector headlessDetector;

// New approach - reliable
if (headlessDetector.isHeadless()) {
    // Handle headless based on configuration
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

   # application-prod.properties
   brobot.headless=true  # If running on headless server
   ```

3. **Test both modes**: Ensure your automation handles both configurations

4. **Use HeadlessDetector, not GraphicsEnvironment**:
   ```java
   // Good
   if (headlessDetector.isHeadless()) { }

   // Avoid
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
brobot.capture.provider=FFMPEG  # Works in headless environments
```

## Summary

- Headless mode is now **explicitly configured** via `brobot.headless` property
- Auto-detection has been **removed** due to reliability issues
- Default is `false` (assumes display is available)
- Use `HeadlessDetector` service instead of `GraphicsEnvironment.isHeadless()`
- `ForceNonHeadlessInitializer` helps prevent incorrect Java detection