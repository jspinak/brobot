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

Provide a common base class for integration tests:

```java
public abstract class IntegrationTestBase {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    @BeforeEach
    public void setupTest() {
        // Ensure mock mode is enabled
        MockModeManager.setMockMode(true);
        System.setProperty("brobot.mock.enabled", "true");
        System.setProperty("java.awt.headless", "true");
    }
}
```

### 3. Profile Properties

Configure test-specific properties in `application-integration.properties`:

```properties
# Integration Test Configuration
spring.main.allow-bean-definition-overriding=true
spring.main.lazy-initialization=false

# Mock Mode Settings - SIMPLIFIED
# Single master switch for mock mode
brobot.mock.enabled=true
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
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@Profile("integration-minimal")
public class IntegrationTestMinimalConfig {
    
    static {
        // Enable mock mode before Spring context loads
        MockModeManager.setMockMode(true);
        System.setProperty("java.awt.headless", "true");
        System.setProperty("brobot.mock.enabled", "true");
    }
    
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
            .perform(any(ActionConfig.class), any(ObjectCollection[].class));
        
        return action;
    }
    
    // Add other required beans...
}
```

### Step 2: Update Test Classes

Use the profile-based configuration in your test classes:

```java
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
        // No bean conflicts!
    }
}
```

### Step 3: Handle Component Annotations

For test classes with `@Component` annotations (like state classes), import them explicitly:

```java
@SpringBootTest(classes = IntegrationTestMinimalConfig.class)
@Import({
    MyIntegrationTest.TestState.class,
    MyIntegrationTest.AnotherTestState.class
})
@ActiveProfiles("integration-minimal")
public class MyIntegrationTest extends IntegrationTestBase {
    
    @Component
    @State
    public static class TestState {
        // State definition
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

Ensure mock mode is set before Spring context loads:

```java
static {
    MockModeManager.setMockMode(true);
    System.setProperty("brobot.mock.enabled", "true");
}
```

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
// Configuration
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
        return new MockAction();
    }
    
    @Bean
    public StateService stateService() {
        return mock(StateService.class);
    }
}

// Test class
@SpringBootTest(classes = ExampleTestConfig.class)
@ActiveProfiles("integration-example")
@TestPropertySource(properties = {
    "brobot.mock.enabled=true",
    "logging.level.io.github.jspinak.brobot=DEBUG"
})
public class ExampleIntegrationTest {
    
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