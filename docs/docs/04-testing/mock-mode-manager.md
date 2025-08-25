---
sidebar_position: 3
title: 'Mock Mode Manager'
description: 'Centralized mock mode management for consistent testing'
---

# Mock Mode Manager

The `MockModeManager` class provides centralized control over mock mode configuration across the entire Brobot framework, ensuring consistency and eliminating confusion about which mock mode flag to use.

## Overview

Prior to the introduction of `MockModeManager`, mock mode configuration was scattered across multiple components:
- `FrameworkSettings.mock` (legacy SikuliX setting)
- `ExecutionEnvironment.mockMode` (runtime environment setting)
- Various system properties (`brobot.mock.mode`, `brobot.framework.mock`, etc.)

This led to confusion and potential inconsistencies. The `MockModeManager` solves this by providing a single source of truth.

## Key Features

### Single Source of Truth
All mock mode checks and settings go through `MockModeManager`, ensuring consistency across the entire framework.

### Automatic Synchronization
When you set mock mode through `MockModeManager`, it automatically updates:
- All relevant system properties
- ExecutionEnvironment configuration
- FrameworkSettings (for SikuliX compatibility)
- Any other framework-specific mock settings

### Debug Capabilities
Built-in logging to help debug mock mode state across all components.

## Basic Usage

### Enabling Mock Mode

```java
import io.github.jspinak.brobot.config.MockModeManager;

// Enable mock mode globally
MockModeManager.setMockMode(true);
```

### Checking Mock Mode Status

```java
// Check if mock mode is enabled
if (MockModeManager.isMockMode()) {
    // Execute mock-specific logic
    System.out.println("Running in mock mode");
} else {
    // Execute real mode logic
    System.out.println("Running in real mode");
}
```

### Debugging Mock Mode State

```java
// Log the current mock mode state across all components
MockModeManager.logMockModeState();
```

This will output something like:
```
Mock Mode State:
  System Properties:
    brobot.mock.mode = true
    brobot.framework.mock = true
    brobot.core.mock-mode = true
  ExecutionEnvironment:
    mockMode = true
    hasDisplay = false
    canCaptureScreen = false
  FrameworkSettings.mock = true
```

## Integration with Tests

### Using with BrobotTestBase

All test classes that extend `BrobotTestBase` automatically use `MockModeManager`:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;

public class MyTest extends BrobotTestBase {
    
    @Test
    public void testInMockMode() {
        // Mock mode is automatically enabled via MockModeManager
        assertTrue(isMockMode());
        
        // Your test logic here
    }
    
    @Test
    public void testInRealMode() {
        // Temporarily disable mock mode
        disableMockMode();
        
        try {
            assertFalse(isMockMode());
            // Test with real screen capture
        } finally {
            // Re-enable mock mode for other tests
            MockModeManager.setMockMode(true);
        }
    }
}
```

### Manual Test Configuration

For tests that don't extend `BrobotTestBase`:

```java
import io.github.jspinak.brobot.config.MockModeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StandaloneTest {
    
    @BeforeEach
    public void setup() {
        // Manually enable mock mode
        MockModeManager.setMockMode(true);
    }
    
    @Test
    public void testFeature() {
        assertTrue(MockModeManager.isMockMode());
        // Test logic
    }
}
```

## Application Startup

### Initializing Mock Mode from Properties

During application startup, you can initialize mock mode based on system properties:

```java
import io.github.jspinak.brobot.config.MockModeManager;

@SpringBootApplication
public class BrobotApplication {
    
    public static void main(String[] args) {
        // Initialize mock mode based on system properties
        MockModeManager.initializeMockMode();
        
        SpringApplication.run(BrobotApplication.class, args);
    }
}
```

### Spring Configuration

Configure mock mode through Spring properties:

```yaml
# application.yml
brobot:
  mock:
    mode: true
  framework:
    mock: true
  core:
    mock-mode: true
```

Or via command line:
```bash
java -jar myapp.jar --brobot.mock.mode=true
```

## Implementation Details

### Synchronized Properties

`MockModeManager.setMockMode(true)` sets the following:

1. **System Properties:**
   - `brobot.mock.mode`
   - `brobot.framework.mock`
   - `brobot.core.mock-mode`

2. **ExecutionEnvironment:**
   - `mockMode = true`
   - `forceHeadless = true` (in mock mode)
   - `allowScreenCapture = false` (in mock mode)

3. **FrameworkSettings:**
   - `FrameworkSettings.mock = true` (via reflection)

### Priority Order

When checking mock mode status, `MockModeManager.isMockMode()` checks in this order:
1. ExecutionEnvironment (if available)
2. System properties (as fallback)

## Best Practices

### 1. Always Use MockModeManager

Instead of checking individual mock flags:
```java
// ❌ Don't do this
if (FrameworkSettings.mock || ExecutionEnvironment.getInstance().isMockMode()) {
    // ...
}

// ✅ Do this
if (MockModeManager.isMockMode()) {
    // ...
}
```

### 2. Set Mock Mode Once

Set mock mode at the beginning of your test or application:
```java
@BeforeAll
public static void setupClass() {
    MockModeManager.setMockMode(true);
}
```

### 3. Use Logging for Debugging

When troubleshooting mock mode issues:
```java
// Before your test
MockModeManager.logMockModeState();

// Run your test
// ...

// After if needed
MockModeManager.logMockModeState();
```

### 4. Handle Mode Transitions Carefully

If switching between modes in a test:
```java
@Test
public void testModeTransition() {
    // Start in mock mode
    MockModeManager.setMockMode(true);
    // ... mock tests ...
    
    // Switch to real mode
    MockModeManager.setMockMode(false);
    try {
        // ... real mode tests ...
    } finally {
        // Always restore mock mode for other tests
        MockModeManager.setMockMode(true);
    }
}
```

## Migration Guide

### From Direct Property Setting

Before:
```java
System.setProperty("brobot.framework.mock", "true");
FrameworkSettings.mock = true;
ExecutionEnvironment env = ExecutionEnvironment.builder()
    .mockMode(true)
    .build();
ExecutionEnvironment.setInstance(env);
```

After:
```java
MockModeManager.setMockMode(true);
```

### From Multiple Mock Checks

Before:
```java
boolean isMock = FrameworkSettings.mock || 
                 "true".equals(System.getProperty("brobot.mock.mode")) ||
                 ExecutionEnvironment.getInstance().isMockMode();
```

After:
```java
boolean isMock = MockModeManager.isMockMode();
```

## Troubleshooting

### Mock Mode Not Taking Effect

1. Check that `MockModeManager` is being used:
   ```java
   MockModeManager.logMockModeState();
   ```

2. Ensure you're calling `setMockMode()` early enough in your test/application lifecycle

3. Verify no other code is directly setting mock flags after `MockModeManager`

### Inconsistent Mock Behavior

If you see inconsistent behavior between components:
1. Use `MockModeManager.setMockMode()` instead of setting individual flags
2. Check for any legacy code still using direct property access
3. Enable debug logging to trace mock mode changes

## API Reference

### MockModeManager Methods

| Method | Description |
|--------|-------------|
| `setMockMode(boolean enable)` | Enable or disable mock mode globally |
| `isMockMode()` | Check if mock mode is currently enabled |
| `logMockModeState()` | Log current mock mode state for debugging |
| `initializeMockMode()` | Initialize mock mode based on system properties |

## Related Documentation

- [Mock Mode Guide](./mock-mode-guide.md) - Comprehensive guide to using mock mode
- [Test Utilities](./test-utilities.md) - Testing utilities and BrobotTestBase
- [Enhanced Mocking](../03-core-library/testing/enhanced-mocking.md) - Advanced mock scenarios