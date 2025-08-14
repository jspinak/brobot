---
sidebar_position: 3
title: 'Profile-Based Architecture'
---

# Profile-Based Architecture Guide

## Overview

Brobot's profile-based architecture provides a clean separation between test and production environments using Spring profiles. This approach eliminates runtime conditionals, improves performance, and ensures consistent behavior across different execution contexts.

## Architecture Evolution

### Previous Architecture (Runtime Delegation)
```java
// Old approach - runtime checks everywhere
public ActionResult execute(Action action) {
    if (FrameworkSettings.mock) {
        return mockExecution.execute(action);
    } else {
        return liveExecution.execute(action);
    }
}
```

### New Architecture (Profile-Based)
```java
// New approach - dependency injection based on profile
@Component
@Profile("test")
public class MockActionExecutor implements ActionExecutor {
    // Mock implementation
}

@Component
@Profile("!test")
public class LiveActionExecutor implements ActionExecutor {
    // Live implementation
}
```

## Profile Configuration

### Default Profiles

Brobot provides two default configuration files:

#### `brobot-defaults.properties`
Production/live defaults applied to all Brobot applications:
```properties
brobot.framework.mock=false
brobot.action.similarity=0.85
brobot.highlight.enabled=true
brobot.screenshot.save-history=true
```

#### `brobot-test-defaults.properties`
Test-optimized defaults automatically loaded with test profile:
```properties
brobot.framework.mock=true
brobot.action.similarity=0.70
brobot.highlight.enabled=false
brobot.screenshot.save-history=false
brobot.mock.time-find-first=0.01  # Fast execution
```

### Application-Specific Profiles

Create profile-specific configurations in your application:

#### `application.properties`
```properties
# Default/production configuration
brobot.framework.mock=false
logging.level.root=WARN
```

#### `application-test.properties`
```properties
# Test profile configuration
spring.config.import=optional:classpath:brobot-test-defaults.properties
brobot.framework.mock=true
logging.level.root=INFO

# State probabilities for deterministic testing
myapp.mock.login-state-probability=100
myapp.mock.home-state-probability=100
```

#### `application-dev.properties`
```properties
# Development profile
brobot.framework.mock=false
brobot.highlight.enabled=true
brobot.logging.verbosity=VERBOSE
logging.level.root=DEBUG
```

## Profile Activation

### In Tests

#### Using @ActiveProfiles
```java
@SpringBootTest
@ActiveProfiles("test")  // Activates test profile
public class MyAutomationTest {
    // Test will use application-test.properties
    // Mock mode automatically enabled
}
```

#### Multiple Profiles
```java
@SpringBootTest
@ActiveProfiles({"test", "integration"})
public class IntegrationTest {
    // Combines test and integration profiles
}
```

### Via Command Line

```bash
# Run tests with test profile
./gradlew test -Dspring.profiles.active=test

# Run application with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Multiple profiles
java -jar myapp.jar --spring.profiles.active=test,debug
```

### Via Environment Variables

```bash
export SPRING_PROFILES_ACTIVE=test
./gradlew test
```

### Programmatically

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MyApplication.class);
        app.setAdditionalProfiles("dev");  // Add dev profile
        app.run(args);
    }
}
```

## Auto-Configuration

### BrobotProfileAutoConfiguration

The framework provides automatic configuration based on active profiles:

```java
@Configuration
@ConditionalOnClass(Action.class)
@PropertySource("classpath:brobot-defaults.properties")
public class BrobotProfileAutoConfiguration {
    
    @Configuration
    @Profile("test")
    @PropertySource("classpath:brobot-test-defaults.properties")
    public static class TestProfileConfiguration {
        
        @PostConstruct
        public void configureTestEnvironment() {
            // Ensures mock mode is enabled
            FrameworkSettings.mock = true;
            // Optimizes for test execution
            FrameworkSettings.moveMouseDelay = 0;
            FrameworkSettings.saveSnapshots = false;
        }
    }
}
```

### Profile Validation

The framework validates configuration consistency:

```java
@Component
public static class ProfileValidator {
    @PostConstruct
    public void validateProfileConfiguration() {
        if (isTestProfile && !FrameworkSettings.mock) {
            log.warn("Test profile active but mock mode disabled - fixing...");
            FrameworkSettings.mock = true;
        }
    }
}
```

## State Configuration with Profiles

### Profile-Aware State Configuration

States can configure themselves based on the active profile:

```java
@State(initial = true)
@Getter
@Slf4j
public class LoginState {
    
    @Autowired(required = false)
    private MockStateManagement mockStateManagement;
    
    @Value("${myapp.mock.login-probability:100}")
    private int mockProbability;
    
    @PostConstruct
    public void configure() {
        if (FrameworkSettings.mock && mockStateManagement != null) {
            mockStateManagement.setStateProbabilities(mockProbability, "Login");
            log.info("Login state mock probability: {}%", mockProbability);
        }
    }
}
```

### Conditional Beans

Create different implementations for different profiles:

```java
@Component
@Profile("test")
public class MockScreenCapture implements ScreenCapture {
    public BufferedImage capture() {
        // Return mock image
        return mockImage;
    }
}

@Component
@Profile("!test")
public class LiveScreenCapture implements ScreenCapture {
    public BufferedImage capture() {
        // Real screen capture
        return robot.createScreenCapture(bounds);
    }
}
```

## Testing Strategies

### Unit Tests (Mock Mode)
```java
@SpringBootTest
@ActiveProfiles("test")
public class StateTransitionTest {
    @Test
    public void testLoginTransition() {
        // Mock mode enabled automatically
        // 100% state probabilities ensure deterministic behavior
    }
}
```

### Integration Tests (Mixed Mode)
```java
@SpringBootTest
@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "brobot.framework.mock=false",  // Override for real UI testing
    "brobot.action.similarity=0.95"  // Stricter matching
})
public class RealUIIntegrationTest {
    // Tests against actual application
}
```

### Performance Tests
```java
@SpringBootTest
@ActiveProfiles({"test", "performance"})
public class PerformanceTest {
    // Combines test profile with performance monitoring
}
```

## Best Practices

### 1. Profile Naming Convention
- `test` - Unit tests with mock mode
- `integration` - Integration tests
- `dev` - Local development
- `staging` - Staging environment
- `prod` - Production (default, no profile)

### 2. Property Organization
```
src/main/resources/
├── application.properties          # Production defaults
├── application-test.properties     # Test overrides
├── application-dev.properties      # Development overrides
└── application-{env}.properties    # Environment-specific
```

### 3. Profile Inheritance
```properties
# application-integration.properties
spring.profiles.include=test  # Inherit from test profile
brobot.framework.mock=false   # Override specific properties
```

### 4. Documentation
Always document profile purpose:
```properties
# application-test.properties
# Purpose: Unit test configuration
# Features: Mock mode, fast execution, no UI
# Activation: @ActiveProfiles("test") or -Dspring.profiles.active=test
```

### 5. Fail-Safe Defaults
```java
@Value("${myapp.feature.enabled:false}")  // Default to safe value
private boolean featureEnabled;
```

## Migration Guide

### Step 1: Create Profile Configurations
1. Copy existing `application.properties` to `application-prod.properties`
2. Create `application-test.properties` with test overrides
3. Update `application.properties` with common settings

### Step 2: Update Tests
```java
// Before
@TestPropertySource(properties = {
    "brobot.framework.mock=true",
    "logging.level=DEBUG"
})

// After
@ActiveProfiles("test")
```

### Step 3: Remove Runtime Checks (Optional)
```java
// Before
if (FrameworkSettings.mock) {
    return mockResult();
} else {
    return liveResult();
}

// After - Use dependency injection
@Autowired
private ActionExecutor executor;  // Injected based on profile
return executor.execute();
```

### Step 4: Verify Configuration
```java
@Test
public void verifyTestProfile() {
    assertTrue(environment.acceptsProfiles("test"));
    assertTrue(FrameworkSettings.mock);
    assertNotNull(mockStateManagement);
}
```

## Troubleshooting

### Profile Not Activating
```java
// Add logging to verify
@PostConstruct
public void logActiveProfiles() {
    log.info("Active profiles: {}", 
        Arrays.toString(environment.getActiveProfiles()));
}
```

### Mock Mode Not Enabled
Check profile configuration order:
1. Default properties loaded first
2. Profile-specific properties override
3. @PostConstruct methods run last

### Bean Not Found
```java
@Component
@Profile("test")  // Only available in test profile
public class TestOnlyBean {
    // Make sure test profile is active
}
```

### Property Not Resolved
```properties
# Provide defaults for all profiles
myapp.setting=${myapp.setting:defaultValue}
```

## Summary

The profile-based architecture provides:
- **Clean separation** between test and production code
- **Automatic configuration** based on environment
- **Better performance** (no runtime checks)
- **Consistent behavior** across environments
- **Easy maintenance** through centralized configuration

Use profiles to eliminate manual configuration, ensure consistent testing, and maintain clean separation between different execution environments.