---
sidebar_position: 8
title: Profile-Based Testing
description: Using Spring profiles to manage test configurations and avoid bean conflicts
---

# Profile-Based Testing Architecture

## Overview

Profile-based testing provides a robust and scalable solution for managing test configurations in Brobot. This approach eliminates Spring bean conflicts that can occur when multiple test configurations define the same beans with `@Primary` annotations.

## Problem Solved

When running integration tests with Spring Boot, you may encounter errors like:

```
NoUniqueBeanDefinitionException: No qualifying bean of type 'ScreenCaptureService' 
available: more than one 'primary' bean found among candidates
```

This happens when multiple configurations (test and production) define the same beans as `@Primary`, causing Spring to be unable to determine which bean to inject.

## Solution Architecture

### 1. Profile-Specific Configuration

Create isolated test configurations using Spring profiles:

```java
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Profile;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@Profile("integration-minimal")
public class IntegrationTestMinimalConfig {
    // Test-specific bean definitions
}
```

### 2. Test Base Class

Provide a common base class for integration tests that extends BrobotTestBase:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for integration tests with Spring profiles.
 * Extends BrobotTestBase which provides automatic mock mode setup.
 */
public abstract class IntegrationTestBase extends BrobotTestBase {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // BrobotTestBase handles MockModeManager setup
        log.debug("Integration test setup complete");
    }
}
```

> **Note**: BrobotTestBase automatically configures mock mode, headless mode, and fast mock timings. You don't need to call `MockModeManager.setMockMode(true)` manually.

### 3. Profile Properties

Configure test-specific properties in `application-integration.properties`:

```properties
# Integration Test Configuration
spring.main.allow-bean-definition-overriding=true
spring.main.lazy-initialization=false

# Mock Mode Settings
# Single master switch for mock mode
brobot.mock=true
# Probability of action success (0.0 to 1.0)
brobot.mock.action.success.probability=1.0

# Headless Mode
java.awt.headless=true

# Mock Timing Configuration (ultra-fast for tests)
brobot.mock.time-find-first=0.01
brobot.mock.time-click=0.01
brobot.mock.time-type=0.01

# Logging
logging.level.io.github.jspinak.brobot=DEBUG
```

## Implementation Guide

### Step 1: Create Minimal Test Configuration

Create a configuration class that provides only the essential beans needed for your tests:

```java
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.awt.image.BufferedImage;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.composites.methods.find.ActionConfig;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.ActionResult;
import io.github.jspinak.brobot.services.ScreenCaptureService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Minimal test configuration with Spring profile isolation.
 * Use @TestPropertySource to set brobot.mock=true instead of static blocks.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@Profile("integration-minimal")
public class IntegrationTestMinimalConfig {

    // Note: Mock mode is configured via @TestPropertySource in test classes,
    // not in static blocks. BrobotTestBase handles MockModeManager initialization.

    @Bean
    @Primary
    public ScreenCaptureService screenCaptureService() {
        ScreenCaptureService service = mock(ScreenCaptureService.class);
        BufferedImage mockImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        when(service.captureScreen()).thenReturn(mockImage);
        return service;
    }

    @Bean
    @Primary
    public Action action() {
        // Configure mock Action for tests
        Action action = mock(Action.class);

        ActionResult successResult = new ActionResult();
        successResult.setSuccess(true);

        // Add default match for find operations
        Match mockMatch = new Match.Builder()
            .setRegion(new Region(100, 100, 50, 50))
            .setSimScore(0.95)
            .build();
        successResult.add(mockMatch);

        // Configure mock responses
        doReturn(successResult).when(action)
            .perform(any(ActionOptions.class), any(ObjectCollection[].class));

        return action;
    }

    // Add other required beans...
}
```

### Step 2: Update Test Classes

Use the profile-based configuration in your test classes:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.manageStates.StateService;

@SpringBootTest(classes = IntegrationTestMinimalConfig.class)
@ActiveProfiles("integration-minimal")
@TestPropertySource(locations = "classpath:application-integration.properties")
public class MyIntegrationTest extends IntegrationTestBase {

    @Autowired
    private Action action;

    @Autowired
    private StateService stateService;

    @Test
    public void testWorkflow() {
        // Your test code here
        // Mock mode is automatically enabled by BrobotTestBase
        // No bean conflicts!
    }
}
```

### Step 3: Handle Component Annotations

For test classes with `@Component` annotations (like state classes), import them explicitly:

```java
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

@SpringBootTest(classes = IntegrationTestMinimalConfig.class)
@Import({
    MyIntegrationTest.TestState.class,
    MyIntegrationTest.AnotherTestState.class
})
@ActiveProfiles("integration-minimal")
public class MyIntegrationTest extends IntegrationTestBase {

    @Component
    @Getter
    public static class TestState {
        // Note: @State annotation is for state machine transitions.
        // For simple test states, @Component is sufficient.
        private final StateImage testElement;

        public TestState() {
            testElement = new StateImage.Builder()
                .setName("TestElement")
                .build();
        }
    }
}
```

## Benefits

### 1. **Isolation**
- Test configurations are completely isolated from production configurations
- No interference between different test suites

### 2. **Scalability**
- Easy to add new profiles for different test scenarios:
  - `integration-minimal` - Minimal beans for fast tests
  - `integration-full` - Complete application context
  - `integration-db` - Tests with database
  - `integration-ui` - Tests with UI components

### 3. **Performance**
- Load only required beans, reducing test startup time
- Ultra-fast mock timings for quick test execution

### 4. **Maintainability**
- Clear separation of concerns
- Easy to debug configuration issues
- Explicit declaration of test dependencies

## Multiple Profile Strategy

You can create different profiles for different testing needs:

### Minimal Profile (Fastest)
```java
@Profile("integration-minimal")
public class IntegrationTestMinimalConfig {
    // Only essential beans
}
```

### Full Profile (Complete Context)
```java
@Profile("integration-full")
@Import({BrobotConfig.class, StateManagementConfig.class})
public class IntegrationTestFullConfig {
    // Full application context with overrides
}
```

### Database Profile
```java
@Profile("integration-db")
@EnableJpaRepositories
public class IntegrationTestDatabaseConfig {
    // Database-specific test configuration
}
```

## Troubleshooting

### Bean Definition Conflicts

If you still encounter bean conflicts:

1. **Check for component scanning overlap**:
   ```java
   @ComponentScan(
       basePackages = "io.github.jspinak.brobot",
       excludeFilters = {
           @Filter(type = FilterType.REGEX, pattern = ".*Test.*"),
           @Filter(type = FilterType.REGEX, pattern = ".*Mock.*Config.*")
       }
   )
   ```

2. **Use @ConditionalOnMissingBean**:
   ```java
   @Bean
   @ConditionalOnMissingBean(ScreenCaptureService.class)
   public ScreenCaptureService screenCaptureService() {
       // Bean definition
   }
   ```

3. **Enable bean overriding** (use with caution):
   ```properties
   spring.main.allow-bean-definition-overriding=true
   ```

### Profile Not Activated

Ensure the profile is activated in your test:

```java
@ActiveProfiles("integration-minimal")  // Don't forget this!
```

Or via environment variable:
```bash
SPRING_PROFILES_ACTIVE=integration-minimal ./gradlew test
```

### Mock Mode Not Enabled

Ensure mock mode is configured via properties:

```java
// In test class, use @TestPropertySource:
@TestPropertySource(properties = {
    "brobot.mock=true",
    "java.awt.headless=true"
})
public class MyTest extends BrobotTestBase {
    // BrobotTestBase automatically calls MockModeManager.setMockMode(true)
}
```

> **Note**: Avoid static blocks for mock mode initialization. Use `@TestPropertySource` or extend `BrobotTestBase` which handles initialization properly.

## Best Practices

1. **Keep profiles focused**: Each profile should have a single, clear purpose
2. **Document profile purpose**: Add JavaDoc explaining what each profile provides
3. **Use descriptive names**: `integration-minimal` is clearer than `test1`
4. **Minimize bean count**: Only include beans actually needed for tests
5. **Reuse common configurations**: Create base configurations that profiles can extend
6. **Test profile combinations**: Ensure profiles work together when needed

## Migration Guide

To migrate existing tests to profile-based configuration:

1. **Identify conflicting beans** in your current test setup
2. **Create a minimal configuration** with only required beans
3. **Add @Profile annotation** to the configuration
4. **Update test classes** to use the new configuration
5. **Create profile-specific properties** file
6. **Run tests** to verify no conflicts

## Example: Complete Test Setup

Here's a complete example of a test using profile-based configuration:

```java
// === Configuration Class ===
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.manageStates.StateService;

import static org.mockito.Mockito.mock;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@Profile("integration-example")
public class ExampleTestConfig {

    @Bean
    @Primary
    public Action action() {
        return mock(Action.class);
    }

    @Bean
    public StateService stateService() {
        return mock(StateService.class);
    }
}

// === Test Class ===
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.methods.basicactions.find.PatternFindOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.reports.ActionResult;
import io.github.jspinak.brobot.test.BrobotTestBase;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ExampleTestConfig.class)
@ActiveProfiles("integration-example")
@TestPropertySource(properties = {
    "brobot.mock=true",
    "logging.level.io.github.jspinak.brobot=DEBUG"
})
public class ExampleIntegrationTest extends BrobotTestBase {

    @Autowired
    private Action action;

    @Test
    public void testExample() {
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();

        StateImage image = new StateImage.Builder()
            .setName("TestImage")
            .build();

        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(image)
            .build();

        ActionResult result = action.perform(options, objects);
        assertTrue(result.isSuccess());
    }
}
```

## Conclusion

Profile-based testing provides a robust, scalable solution for managing test configurations in Brobot. By isolating test configurations with Spring profiles, you can:

- Eliminate bean conflicts
- Improve test performance
- Maintain cleaner test code
- Scale your test suite effectively

This approach is particularly valuable for large projects with complex Spring configurations and multiple test scenarios.

## Related Documentation

### Testing Guides
- **[Testing Introduction](./testing-intro.md)** - Overview of Brobot testing approaches
- **[Unit Testing](./unit-testing.md)** - Unit testing patterns with BrobotTestBase
- **[Integration Testing](./integration-testing.md)** - Integration test patterns with Spring
- **[Test Utilities](./test-utilities.md)** - BrobotTestBase and testing utilities reference
- **[Mock Mode Guide](./mock-mode-guide.md)** - Comprehensive guide to mock mode
- **[Mock Mode Manager](./mock-mode-manager.md)** - Centralized mock mode management
- **[Testing Strategy](./testing-strategy.md)** - Overall testing strategy and patterns

### Mock Mode Documentation
- **[Mock Stochasticity](./mock-stochasticity.md)** - Probabilistic mock behavior
- **[Mock Mode Migration](./mock-mode-migration.md)** - Migrating to MockModeManager
- **[ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md)** - Creating mock data
- **[Action Recording](./action-recording.md)** - Recording actions for mocks
- **[ActionHistory Integration Testing](./actionhistory-integration-testing.md)** - Testing with ActionHistory

### Configuration Documentation
- **[BrobotProperties Usage](../03-core-library/configuration/brobot-properties-usage.md)** - Complete configuration guide
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - All available properties
- **[Auto-Configuration](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Headless mode setup

### ActionConfig Documentation
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - Introduction to ActionConfig
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical examples
- **[ActionConfig Reference](../03-core-library/action-config/05-reference.md)** - Complete API reference
- **[Action Chaining](../03-core-library/action-config/07-action-chaining.md)** - Chaining actions together
- **[Convenience Methods](../03-core-library/action-config/18-convenience-methods.md)** - Simplified action methods

### State Management
- **[States Overview](../01-getting-started/states.md)** - Introduction to states
- **[Transitions](../01-getting-started/transitions.md)** - State transitions
- **[Annotations Guide](../03-core-library/user-guides/annotations.md)** - @State and @Transition usage

### Advanced Topics
- **[Enhanced Mocking](./advanced/enhanced-mocking.md)** - Advanced mock scenarios
- **[CI/CD Testing](./advanced/ci-cd-testing.md)** - Mock mode in CI/CD pipelines
- **[Debugging Pattern Matching](./debugging-pattern-matching.md)** - Troubleshooting pattern matching

### Getting Started
- **[Quick Start](../01-getting-started/quick-start.md)** - Getting started with Brobot
- **[AI Brobot Project Creation](../01-getting-started/ai-brobot-project-creation.md)** - Complete API reference and patterns