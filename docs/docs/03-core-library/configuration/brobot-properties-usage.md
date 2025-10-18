# BrobotProperties Usage Guide

This guide explains how to configure and use BrobotProperties in your Brobot applications.

## Overview

BrobotProperties is the Spring-based configuration system for Brobot, providing type-safe, validated configuration management with excellent IDE support and testing capabilities.

> **Related Documentation**:
> - For a complete reference of all available properties, see the [Configuration Properties Reference](properties-reference.md)
> - To understand how Brobot auto-configures these properties, see the [Auto-Configuration Guide](auto-configuration.md)
> - For testing patterns, see the [Testing Introduction](../../04-testing/testing-intro.md)

## Quick Start: Complete Working Example

Here's a minimal, fully functional example showing BrobotProperties in action:

### Project Structure
```
my-brobot-app/
├── src/main/java/com/example/
│   └── MyBrobotApp.java
├── src/main/resources/
│   └── application.properties
└── src/test/java/com/example/
    └── MyBrobotAppTest.java
```

### MyBrobotApp.java
```java
package com.example;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example",
    "io.github.jspinak.brobot"
})
public class MyBrobotApp {

    public static void main(String[] args) {
        SpringApplication.run(MyBrobotApp.class, args);
    }

    @Bean
    public CommandLineRunner demo(BrobotProperties properties) {
        return args -> {
            System.out.println("=== Brobot Configuration ===");
            System.out.println("Mock mode: " + properties.getCore().isMock());
            System.out.println("Headless: " + properties.getCore().isHeadless());
            System.out.println("Mouse delay: " + properties.getMouse().getMoveDelay());
            System.out.println("Screenshot path: " + properties.getScreenshot().getPath());
        };
    }
}
```

### application.properties
```properties
# Core configuration
brobot.core.mock=false
brobot.core.headless=false

# Mouse behavior
brobot.mouse.move-delay=0.3

# Screenshots
brobot.screenshot.path=screenshots
brobot.screenshot.save-snapshots=true
```

### MyBrobotAppTest.java
```java
package com.example;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.mouse.move-delay=0.01"
})
class MyBrobotAppTest {

    @Autowired
    private BrobotProperties properties;

    @Test
    void propertiesAreLoaded() {
        assertTrue(properties.getCore().isMock(), "Mock mode should be enabled in tests");
        assertEquals(0.01, properties.getMouse().getMoveDelay(), "Mouse delay should be overridden");
    }
}
```

### Expected Output
```
=== Brobot Configuration ===
Mock mode: false
Headless: false
Mouse delay: 0.3
Screenshot path: screenshots
```

> **Next**: For detailed configuration options, continue reading the sections below.

---

## Configuration in application.properties

All Brobot configuration properties use the `brobot.` prefix and are organized into logical groups:

### Core Properties
```properties
# Mock mode for testing without screen interaction
# Note: Can be set as either brobot.mock or brobot.core.mock
# Accessed in code via: brobotProperties.getCore().isMock()
brobot.core.mock=false

# Headless mode (no display available)
# Accessed in code via: brobotProperties.getCore().isHeadless()
brobot.core.headless=false

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

These properties control simulated operation times when running in [mock mode](../../04-testing/mock-mode-guide.md):

```properties
# Simulated operation times (in seconds)
brobot.mock.time-find-first=0.01
brobot.mock.time-find-all=0.04
brobot.mock.time-click=0.01
```

> **See Also**: [Mock Mode Guide](../../04-testing/mock-mode-guide.md) for complete mock configuration options

### Screenshot Properties
```properties
# Screenshot configuration
brobot.screenshot.save-snapshots=false
brobot.screenshot.path=images
```

## Using BrobotProperties in Your Code

### Basic Usage with Dependency Injection

```java
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyAutomationService {

    @Autowired
    private BrobotProperties brobotProperties;

    public void performAction() {
        if (brobotProperties.getCore().isMock()) {
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
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationExample {

    @Autowired
    private BrobotProperties brobotProperties;

    public void showConfiguration() {
        // Core properties - accessed via getCore()
        boolean mockMode = brobotProperties.getCore().isMock();
        boolean headless = brobotProperties.getCore().isHeadless();

        // Mouse configuration
        BrobotProperties.Mouse mouse = brobotProperties.getMouse();
        double moveDelay = mouse.getMoveDelay();
        double pauseBeforeDown = mouse.getPauseBeforeDown();

        // Mock timing configuration
        BrobotProperties.Mock mock = brobotProperties.getMock();
        double findFirstTime = mock.getTimeFindFirst();
        double clickTime = mock.getTimeClick();

        // Screenshot configuration
        BrobotProperties.Screenshot screenshot = brobotProperties.getScreenshot();
        boolean saveSnapshots = screenshot.isSaveSnapshots();
        String screenshotPath = screenshot.getPath();
    }
}
```

## Configuration in Tests

> **Testing Resources**:
> - [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Comprehensive testing with mock mode
> - [Unit Testing Guide](../../04-testing/unit-testing.md) - Unit testing patterns with BrobotTestBase
> - [Integration Testing Guide](../../04-testing/integration-testing.md) - Integration testing patterns
> - [Profile-Based Testing](../../04-testing/profile-based-testing.md) - Advanced profile usage

### Using @TestPropertySource

```java
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.mock.time-find-first=0.001",
    "brobot.screenshot.save-snapshots=false"
})
class MyIntegrationTest {

    @Autowired
    private BrobotProperties brobotProperties;

    @Test
    void testInMockMode() {
        assertTrue(brobotProperties.getCore().isMock());
        assertEquals(0.001, brobotProperties.getMock().getTimeFindFirst());
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

> **Learn More**: For detailed information on unit testing with BrobotTestBase, see the [Unit Testing Guide](../../04-testing/unit-testing.md).

## Working with Non-Spring Classes

### Option 1: Convert to Spring Component

```java
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import io.github.jspinak.brobot.config.core.BrobotProperties;
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
        if (props.getCore().isMock()) {
            // Mock behavior
        }
    }
}
```

## Environment-Specific Configuration

Use Spring profiles to maintain different configurations for each environment. See [Profile-Based Testing](../../04-testing/profile-based-testing.md) for advanced testing scenarios.

### Development Environment
`application-dev.properties`:
```properties
brobot.mock=false
brobot.screenshot.save-snapshots=true
brobot.debug.image.enabled=true  # See: Image Find Debugging Guide
brobot.logging.verbosity=VERBOSE  # See: Auto-Configuration Guide
```

> **Learn More**: [Image Find Debugging](../tools/image-find-debugging.md) for debugging configuration

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
brobot.core.headless=true
brobot.gui-access.continue-on-error=true
brobot.gui-access.check-on-startup=false
```

> **CI/CD Tips**: See [Mock Mode Guide](../../04-testing/mock-mode-guide.md) for headless testing strategies

### Activating Profiles

**Command line**:
```bash
java -jar myapp.jar --spring.profiles.active=dev
```

**Environment variable**:
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar myapp.jar
```

**Gradle**:
```bash
./gradlew bootRun --args='--spring.profiles.active=ci'
```

**Programmatically** (for tests):
```java
@SpringBootTest
@ActiveProfiles("test")
class MyTest {
    // Uses application-test.properties
}
```

> **Best Practice**: Use the `test` profile for all automated tests - it's automatically optimized with fast mock timings. See [Auto-Configuration Guide](auto-configuration.md#5-mockconfiguration-with-test-profile-optimization) for details.

## Property Precedence and Validation

### Property Loading Order

Spring Boot loads properties in this order (highest priority first):

1. **@TestPropertySource** in tests (highest priority)
2. **Command line arguments**: `--brobot.core.mock=true`
3. **Java system properties**: `-Dbrobot.core.mock=true`
4. **OS environment variables**: `BROBOT_CORE_MOCK=true`
5. **application-{profile}.properties** for active profiles
6. **application.properties** in your project
7. **brobot-{profile}-defaults.properties** from Brobot library
8. **brobot-defaults.properties** from Brobot library (lowest priority)

**Example of precedence**:
```properties
# brobot-defaults.properties (Brobot library)
brobot.mouse.move-delay=0.5

# application.properties (your project)
brobot.mouse.move-delay=0.3  # Overrides default

# application-dev.properties (when dev profile is active)
brobot.mouse.move-delay=0.1  # Overrides application.properties

# @TestPropertySource in test
brobot.mouse.move-delay=0.01  # Overrides everything
```

**Result**: In this scenario, tests use `0.01`, dev environment uses `0.1`, production uses `0.3`.

> **Learn More**: See [Auto-Configuration Guide](auto-configuration.md#configuration-layers) for detailed explanation of property loading

### Property Validation

BrobotProperties uses Spring Boot's `@ConfigurationProperties` for type-safe property binding:

```java
@ConfigurationProperties(prefix = "brobot")
public class BrobotProperties {
    // Properties are bound at startup
    // Type mismatches will cause startup failures
}
```

**Validation behavior**:
- **Type checking**: Spring Boot validates property types automatically
- **Startup failure**: Invalid properties cause application to fail fast
- **Clear error messages**: Type mismatches show helpful error messages

> **Note**: The current implementation does not include JSR-303 validation annotations (`@Validated`, `@Min`, `@Max`, etc.). Properties are validated through Spring Boot's type system and will fail at startup if types don't match. Future versions may add explicit validation constraints.

## Common Configuration Patterns

### Conditional Bean Creation

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionalConfig {

    @Bean
    @ConditionalOnProperty(name = "brobot.core.mock", havingValue = "true")
    public MockActionExecutor mockActionExecutor() {
        return new MockActionExecutor();
    }

    @Bean
    @ConditionalOnProperty(name = "brobot.core.mock", havingValue = "false", matchIfMissing = true)
    public RealActionExecutor realActionExecutor() {
        return new RealActionExecutor();
    }
}
```

### Configuration Profiles

```java
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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

**Symptoms**: Properties have default values instead of configured values

**Diagnosis**:
```java
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PropertyDiagnostics {

    @Autowired
    private Environment environment;

    @Autowired
    private BrobotProperties properties;

    public void diagnose() {
        // Check if property is in environment
        String mockValue = environment.getProperty("brobot.core.mock");
        System.out.println("Property brobot.core.mock from environment: " + mockValue);

        // Check bound value
        System.out.println("Property from BrobotProperties: " + properties.getCore().isMock());

        // List all property sources
        System.out.println("\n=== Property Sources (in order) ===");
        if (environment instanceof org.springframework.core.env.ConfigurableEnvironment) {
            org.springframework.core.env.ConfigurableEnvironment env =
                (org.springframework.core.env.ConfigurableEnvironment) environment;
            env.getPropertySources().forEach(ps ->
                System.out.println("- " + ps.getName())
            );
        }
    }
}
```

**Solutions**:
1. Ensure `application.properties` is in `src/main/resources`
2. Check property names use correct prefix: `brobot.` (not `brobot-`)
3. Verify Spring Boot is properly configured with `@SpringBootApplication`
4. Check for typos in property names (case-sensitive)
5. Ensure `@ComponentScan` includes `io.github.jspinak.brobot`

> **See Also**: [Auto-Configuration Guide](auto-configuration.md) explains property loading order

### Cannot Access in Static Context

**Problem**: Cannot inject BrobotProperties into static methods or non-Spring classes

**Solutions**: See the [Working with Non-Spring Classes](#working-with-non-spring-classes) section above for three different approaches.

### Test Properties Not Applied

**Symptoms**: Test runs with production properties instead of test properties

**Diagnosis**:
```java
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class PropertyLoadingTest {

    @Autowired
    private Environment environment;

    @Autowired
    private BrobotProperties properties;

    @Test
    void verifyTestPropertiesLoaded() {
        // Check active profiles
        String[] profiles = environment.getActiveProfiles();
        System.out.println("Active profiles: " + String.join(", ", profiles));

        // Check property value
        String mockValue = environment.getProperty("brobot.core.mock");
        System.out.println("brobot.core.mock = " + mockValue);

        // Verify bound value
        System.out.println("Actual mock mode: " + properties.getCore().isMock());
    }
}
```

**Solutions**:
1. Use `@TestPropertySource` annotation (see [Configuration in Tests](#configuration-in-tests))
2. Use `@ActiveProfiles("test")` with `application-test.properties`
3. For unit tests, extend [BrobotTestBase](../../04-testing/unit-testing.md)
4. Ensure test properties file is in `src/test/resources`
5. Check that profile is actually activated (see diagnosis above)

### IDE Auto-completion Not Working

**Problem**: No auto-completion for `brobot.*` properties in application.properties

**Solution**:

1. Add Spring Boot Configuration Processor dependency:

**Gradle** (`build.gradle`):
```gradle
dependencies {
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
}
```

**Maven** (`pom.xml`):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

2. Enable annotation processing in your IDE:
   - **IntelliJ IDEA**: Settings → Build → Compiler → Annotation Processors → Enable annotation processing
   - **Eclipse**: Project Properties → Java Compiler → Annotation Processing → Enable project specific settings
   - **VS Code**: Install Spring Boot Extension Pack

3. Rebuild project to generate metadata

> **Note**: This dependency is not currently in the Brobot project but is recommended for improved developer experience.

### Property Value Type Mismatch

**Symptoms**: Application fails to start with property binding error

**Example Error**:
```
Failed to bind properties under 'brobot.mouse.move-delay' to double:
    Reason: failed to convert java.lang.String to double
```

**Solution**: Check property value types match expectations:
```properties
# ✅ CORRECT
brobot.mouse.move-delay=0.5

# ❌ WRONG - not a number
brobot.mouse.move-delay=slow
```

> **See Also**: [Properties Reference](properties-reference.md) for expected types of all properties

## Benefits

- **Type Safety**: Properties are validated at startup
- **IDE Support**: Auto-completion and documentation
- **Testing**: Easy to override for different test scenarios
- **No Static State**: Better for concurrent testing
- **Spring Integration**: Works with profiles, conditions, and validation
- **Environment Flexibility**: Different configs per environment

## Summary

BrobotProperties provides a modern, flexible configuration system that integrates seamlessly with Spring Boot. By using dependency injection and property files, you get type-safe, testable, and maintainable configuration management for your Brobot automation projects.

## Next Steps

- **Complete Properties Reference**: See [Configuration Properties Reference](properties-reference.md) for all available properties and their default values
- **Auto-Configuration Details**: Learn how Brobot configures itself automatically in the [Auto-Configuration Guide](auto-configuration.md)
- **Testing Setup**: Get started with testing using the [Testing Introduction](../../04-testing/testing-intro.md)
- **Mock Mode**: Understand mock mode testing with the [Mock Mode Guide](../../04-testing/mock-mode-guide.md)
- **Getting Started**: New to Brobot? Check out the [Quick Start Guide](../../01-getting-started/quickstart.md)