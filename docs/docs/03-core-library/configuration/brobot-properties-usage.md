# BrobotProperties Usage Guide

This guide explains how to configure and use BrobotProperties in your Brobot applications.

## Overview

BrobotProperties is the Spring-based configuration system for Brobot, providing type-safe, validated configuration management with excellent IDE support and testing capabilities.

## Configuration in application.properties

All Brobot configuration properties use the `brobot.` prefix and are organized into logical groups:

### Core Properties
```properties
# Mock mode for testing without screen interaction
brobot.mock=false

# Headless mode (no display available)
brobot.headless=false

# Package name for state discovery
brobot.core.package-name=com.example.myapp
```

### Mouse Properties
```properties
# Mouse movement delays
brobot.mouse.move-delay=0.5
brobot.mouse.pause-before-down=0.1
brobot.mouse.pause-after-down=0.1
```

### Mock Timing Properties
```properties
# Simulated operation times (in seconds)
brobot.mock.time-find-first=0.01
brobot.mock.time-find-all=0.04
brobot.mock.time-click=0.01
```

### Screenshot Properties
```properties
# Screenshot configuration
brobot.screenshot.save-snapshots=false
brobot.screenshot.path=images
```

## Using BrobotProperties in Your Code

### Basic Usage with Dependency Injection

```java
import io.github.jspinak.brobot.config.BrobotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyAutomationService {

    @Autowired
    private BrobotProperties brobotProperties;

    public void performAction() {
        if (brobotProperties.isMock()) {
            // Execute mock behavior
            System.out.println("Running in mock mode");
        } else {
            // Execute real automation
            System.out.println("Running real automation");
        }
    }

    public void configureMouseBehavior() {
        double moveDelay = brobotProperties.getMouse().getMoveDelay();
        System.out.println("Mouse move delay: " + moveDelay);
    }
}
```

### Accessing Nested Properties

BrobotProperties provides structured access to configuration groups:

```java
@Component
public class ConfigurationExample {

    @Autowired
    private BrobotProperties brobotProperties;

    public void showConfiguration() {
        // Core properties
        boolean mockMode = brobotProperties.isMock();
        boolean headless = brobotProperties.isHeadless();

        // Mouse configuration
        BrobotProperties.MouseProperties mouse = brobotProperties.getMouse();
        double moveDelay = mouse.getMoveDelay();
        double pauseBeforeDown = mouse.getPauseBeforeDown();

        // Mock timing configuration
        BrobotProperties.MockProperties mock = brobotProperties.getMockProperties();
        double findFirstTime = mock.getTimeFindFirst();
        double clickTime = mock.getTimeClick();

        // Screenshot configuration
        BrobotProperties.ScreenshotProperties screenshot = brobotProperties.getScreenshot();
        boolean saveSnapshots = screenshot.isSaveSnapshots();
        String screenshotPath = screenshot.getPath();
    }
}
```

## Configuration in Tests

### Using @TestPropertySource

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "brobot.mock=true",
    "brobot.mock.time-find-first=0.001",
    "brobot.screenshot.save-snapshots=false"
})
class MyIntegrationTest {

    @Autowired
    private BrobotProperties brobotProperties;

    @Test
    void testInMockMode() {
        assertTrue(brobotProperties.isMock());
        assertEquals(0.001, brobotProperties.getMockProperties().getTimeFindFirst());
    }
}
```

### Using application-test.properties

Create `src/test/resources/application-test.properties`:

```properties
# Test-specific configuration
brobot.mock=true
brobot.headless=true
brobot.screenshot.save-snapshots=false
brobot.mock.time-find-first=0.001
brobot.mock.time-click=0.001
```

Then activate the test profile:

```java
@SpringBootTest
@ActiveProfiles("test")
class MyTest {
    // Tests will use application-test.properties
}
```

### Using BrobotTestBase

For unit tests, extend `BrobotTestBase` which automatically configures mock mode:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;

public class MyUnitTest extends BrobotTestBase {

    @Test
    public void testSomething() {
        // Mock mode is automatically enabled
        // No need to configure BrobotProperties manually
    }
}
```

## Working with Non-Spring Classes

### Option 1: Convert to Spring Component

```java
// Before: Static utility class
public class ImageUtils {
    public static boolean shouldSaveScreenshot() {
        return FrameworkSettings.saveSnapshots; // OLD WAY
    }
}

// After: Spring component
@Component
public class ImageUtils {
    @Autowired
    private BrobotProperties brobotProperties;

    public boolean shouldSaveScreenshot() {
        return brobotProperties.getScreenshot().isSaveSnapshots();
    }
}
```

### Option 2: Pass as Parameter

```java
public class ImageUtils {
    public static boolean shouldSaveScreenshot(BrobotProperties properties) {
        return properties.getScreenshot().isSaveSnapshots();
    }
}

// Usage
@Component
public class MyService {
    @Autowired
    private BrobotProperties brobotProperties;

    public void process() {
        if (ImageUtils.shouldSaveScreenshot(brobotProperties)) {
            // Save screenshot
        }
    }
}
```

### Option 3: Use ApplicationContext

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class BrobotPropertiesProvider implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static BrobotProperties getProperties() {
        return context.getBean(BrobotProperties.class);
    }
}

// Usage in non-Spring class
public class NonSpringClass {
    public void someMethod() {
        BrobotProperties props = BrobotPropertiesProvider.getProperties();
        if (props.isMock()) {
            // Mock behavior
        }
    }
}
```

## Environment-Specific Configuration

### Development Environment
`application-dev.properties`:
```properties
brobot.mock=false
brobot.screenshot.save-snapshots=true
brobot.debug.image.enabled=true
brobot.logging.verbosity=VERBOSE
```

### Production Environment
`application-prod.properties`:
```properties
brobot.mock=false
brobot.screenshot.save-snapshots=false
brobot.debug.image.enabled=false
brobot.logging.verbosity=NORMAL
```

### CI/CD Environment
`application-ci.properties`:
```properties
brobot.mock=true
brobot.headless=true
brobot.gui-access.continue-on-error=true
brobot.gui-access.check-on-startup=false
```

Activate profiles using:
```bash
# Command line
java -jar myapp.jar --spring.profiles.active=dev

# Environment variable
export SPRING_PROFILES_ACTIVE=prod

# Gradle
./gradlew bootRun --args='--spring.profiles.active=ci'
```

## Property Validation

BrobotProperties includes validation to ensure configuration is correct:

```java
@ConfigurationProperties(prefix = "brobot")
@Validated
public class BrobotProperties {

    @Min(0)
    @Max(1)
    private double mockActionSuccessProbability = 1.0;

    @NotNull
    private String screenshotPath = "images";

    // Validation happens at startup
}
```

## Common Configuration Patterns

### Conditional Bean Creation

```java
@Configuration
public class ConditionalConfig {

    @Bean
    @ConditionalOnProperty(name = "brobot.mock", havingValue = "true")
    public MockActionExecutor mockActionExecutor() {
        return new MockActionExecutor();
    }

    @Bean
    @ConditionalOnProperty(name = "brobot.mock", havingValue = "false", matchIfMissing = true)
    public RealActionExecutor realActionExecutor() {
        return new RealActionExecutor();
    }
}
```

### Configuration Profiles

```java
@Component
@Profile("!mock")
public class RealScreenCapture implements ScreenCapture {
    // Implementation for real screen capture
}

@Component
@Profile("mock")
public class MockScreenCapture implements ScreenCapture {
    // Mock implementation
}
```

## Troubleshooting

### Properties Not Loading

1. Ensure `application.properties` is in `src/main/resources`
2. Check property names use correct prefix: `brobot.`
3. Verify Spring Boot is properly configured

### Cannot Access in Static Context

Convert to Spring component or pass BrobotProperties as parameter (see examples above)

### Test Properties Not Applied

1. Use `@TestPropertySource` or `@ActiveProfiles("test")`
2. Ensure test properties file is in `src/test/resources`
3. For unit tests, extend `BrobotTestBase`

### IDE Auto-completion Not Working

1. Add Spring Boot Configuration Processor dependency:
```gradle
annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
```

2. Enable annotation processing in your IDE

## Benefits

- **Type Safety**: Properties are validated at startup
- **IDE Support**: Auto-completion and documentation
- **Testing**: Easy to override for different test scenarios
- **No Static State**: Better for concurrent testing
- **Spring Integration**: Works with profiles, conditions, and validation
- **Environment Flexibility**: Different configs per environment

## Summary

BrobotProperties provides a modern, flexible configuration system that integrates seamlessly with Spring Boot. By using dependency injection and property files, you get type-safe, testable, and maintainable configuration management for your Brobot automation projects.