# Migration from FrameworkSettings to BrobotProperties

## Overview

The static `FrameworkSettings` class has been completely removed in favor of the modern, Spring-based `BrobotProperties` configuration approach. This provides better testability, cleaner code, and proper dependency injection.

## Key Changes

### 1. Configuration Method

**Before (FrameworkSettings):**
```java
// Static field access
FrameworkSettings.mock = true;
FrameworkSettings.moveMouseDelay = 0.5f;
```

**After (BrobotProperties):**
```properties
# application.properties
brobot.core.mock=true
brobot.mouse.move-delay=0.5
```

### 2. Accessing Configuration in Code

**Before:**
```java
public class MyClass {
    public void myMethod() {
        if (FrameworkSettings.mock) {
            // mock logic
        }
    }
}
```

**After:**
```java
@Component
public class MyClass {
    @Autowired
    private BrobotProperties brobotProperties;

    public void myMethod() {
        if (brobotProperties.getCore().isMock()) {
            // mock logic
        }
    }
}
```

### 3. Test Configuration

**Before:**
```java
@BeforeEach
void setUp() {
    FrameworkSettings.mock = true;
}
```

**After:**
```java
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true"
})
class MyTest {
    // Tests run in mock mode
}
```

Or use `application-test.properties`:
```properties
brobot.core.mock=true
```

## Property Mappings

| FrameworkSettings Field | BrobotProperties Path |
|-------------------------|----------------------|
| `mock` | `brobot.core.mock` |
| `headless` | `brobot.core.headless` |
| `packageName` | `brobot.core.package-name` |
| `moveMouseDelay` | `brobot.mouse.move-delay` |
| `pauseBeforeMouseDown` | `brobot.mouse.pause-before-down` |
| `pauseAfterMouseDown` | `brobot.mouse.pause-after-down` |
| `timeFindFirst` | `brobot.mock.time-find-first` |
| `timeFindAll` | `brobot.mock.time-find-all` |
| `timeClick` | `brobot.mock.time-click` |
| `saveSnapshots` | `brobot.screenshot.save-snapshots` |
| `screenshotPath` | `brobot.screenshot.path` |

## Benefits of the Migration

1. **Type Safety**: Configuration is validated at startup
2. **IDE Support**: Auto-completion for property names
3. **Environment-Specific**: Easy per-environment configuration
4. **Testability**: Simple to override in tests
5. **No Static State**: Better for concurrent testing
6. **Spring Integration**: Works seamlessly with Spring profiles

## Common Migration Tasks

### Converting Static Utility Classes

If you have static utility classes that used `FrameworkSettings`:

**Before:**
```java
public class MyUtil {
    public static boolean isInMockMode() {
        return FrameworkSettings.mock;
    }
}
```

**After (Option 1 - Make it a Spring Component):**
```java
@Component
public class MyUtil {
    @Autowired
    private BrobotProperties brobotProperties;

    public boolean isInMockMode() {
        return brobotProperties.getCore().isMock();
    }
}
```

**After (Option 2 - Pass as Parameter):**
```java
public class MyUtil {
    public static boolean isInMockMode(BrobotProperties properties) {
        return properties.getCore().isMock();
    }
}
```

### Updating Tests

All test base classes now handle configuration automatically:

```java
public class MyTest extends BrobotTestBase {
    // Mock mode is automatically enabled
    // No need to set FrameworkSettings.mock = true
}
```

## Troubleshooting

### Issue: Cannot access BrobotProperties in static context

**Solution**: Convert the class to a Spring component or pass BrobotProperties as a method parameter.

### Issue: Properties not being loaded

**Solution**: Ensure your `application.properties` is in the classpath and contains the correct property names with the `brobot.` prefix.

### Issue: Mock mode not working in tests

**Solution**: Extend `BrobotTestBase` or add `@TestPropertySource(properties = {"brobot.core.mock=true"})` to your test class.

## Summary

The migration from `FrameworkSettings` to `BrobotProperties` represents a shift from static configuration to dependency-injected, Spring-managed configuration. This change improves testability, removes global state, and provides better integration with the Spring ecosystem.
