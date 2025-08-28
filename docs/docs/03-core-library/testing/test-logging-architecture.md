---
sidebar_position: 5
title: Test Logging Architecture
description: Clean architectural patterns for test logging and configuration in Brobot
---

# Test Logging Architecture

## Overview

Brobot's test logging architecture follows Single Responsibility Principle (SRP) and clean architecture patterns to avoid circular dependencies and Spring initialization issues. This guide explains how to use the TestLoggerFactory and related components for proper test configuration.

## Key Components

### TestLoggerFactory

The `TestLoggerFactory` is a factory class that creates and wires logging components in the correct order, ensuring no circular dependencies and proper initialization.

**Location**: `library-test/src/test/java/io/github/jspinak/brobot/test/logging/TestLoggerFactory.java`

**Single Responsibility**: Create and wire logger components for tests

```java
@Component
public class TestLoggerFactory {
    
    public LoggingSystem createTestLoggingSystem(
            ActionLogger actionLogger,
            LoggingVerbosityConfig verbosityConfig) {
        
        // Components are created in dependency order
        LoggingContext context = new LoggingContext();
        LogSink logSink = new NoOpLogSink();
        ConsoleFormatter formatter = new ConsoleFormatter(verbosityConfig);
        MessageRouter router = new MessageRouter(actionLogger, verbosityConfig, formatter);
        BrobotLogger logger = new BrobotLogger(context, router);
        ConsoleReporterInitializer reporterInit = new ConsoleReporterInitializer(logger);
        
        return new LoggingSystem(context, logSink, formatter, router, logger, reporterInit);
    }
}
```

### TestConfigurationManager

The `TestConfigurationManager` initializes the test environment before Spring context loads, preventing static initialization conflicts.

**Location**: `library-test/src/test/java/io/github/jspinak/brobot/test/config/TestConfigurationManager.java`

**Single Responsibility**: Initialize test environment before Spring context

```java
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestConfigurationManager implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Set system properties before any beans are created
        System.setProperty("brobot.preserve.headless.setting", "true");
        System.setProperty("java.awt.headless", "true");
        
        // Configure ExecutionEnvironment based on test type
        String testType = System.getProperty("brobot.test.type", "unit");
        boolean isIntegrationTest = "integration".equals(testType);
        
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .mockMode(!isIntegrationTest)
            .forceHeadless(true)
            .allowScreenCapture(false)
            .build();
        
        ExecutionEnvironment.setInstance(env);
    }
}
```

## Usage Examples

### 1. Basic Test Configuration

For tests that need logging but don't require Spring context:

```java
public class SimpleLoggingTest extends BrobotTestBase {
    
    private BrobotLogger logger;
    
    @BeforeEach
    void setUp() {
        super.setupTest();
        
        // Create logger using factory
        TestLoggerFactory factory = new TestLoggerFactory();
        ActionLogger actionLogger = new MockActionLogger();
        LoggingVerbosityConfig config = new LoggingVerbosityConfig();
        
        TestLoggerFactory.LoggingSystem system = 
            factory.createTestLoggingSystem(actionLogger, config);
        
        this.logger = system.getLogger();
    }
    
    @Test
    void testLogging() {
        logger.info("Test message");
        // Your test assertions
    }
}
```

### 2. Spring Integration Test Configuration

For Spring-based integration tests, use the provided configuration classes:

```java
@SpringBootTest(classes = BrobotTestApplication.class)
@ContextConfiguration(initializers = TestConfigurationManager.class)
@Import({TestActionConfig.class, MockBrobotLoggerConfig.class})
public class IntegrationTest extends BrobotIntegrationTestBase {
    
    @Autowired
    private BrobotLogger logger;
    
    @Autowired
    private ActionLogger actionLogger;
    
    @Test
    void testWithSpringContext() {
        // Logger is automatically configured via MockBrobotLoggerConfig
        logger.info("Integration test running");
        
        // Use action logger for automation events
        LogData logData = actionLogger.logAction("session-1", result, collection);
        assertNotNull(logData);
    }
}
```

### 3. Custom Test Configuration

Create your own test configuration using the factory pattern:

```java
@TestConfiguration
public class CustomTestLoggerConfig {
    
    @Bean
    public TestLoggerFactory.LoggingSystem customLoggingSystem(
            ActionLogger actionLogger,
            LoggingVerbosityConfig verbosityConfig) {
        
        // Customize configuration
        verbosityConfig.setLevel(LogLevel.DEBUG);
        verbosityConfig.setIncludeTimestamp(true);
        
        TestLoggerFactory factory = new TestLoggerFactory();
        return factory.createTestLoggingSystem(actionLogger, verbosityConfig);
    }
    
    @Bean
    @Primary
    public BrobotLogger customLogger(TestLoggerFactory.LoggingSystem system) {
        return system.getLogger();
    }
}
```

## Architectural Benefits

### 1. No Circular Dependencies

The factory pattern ensures components are created in the correct order:

```
LoggingContext → ConsoleFormatter → MessageRouter → BrobotLogger → ConsoleReporterInitializer
```

Each arrow represents a one-way dependency with no cycles.

### 2. Single Responsibility Principle

Each component has a clear, single responsibility:

- **TestLoggerFactory**: Create logger components
- **TestConfigurationManager**: Initialize environment
- **MockBrobotLoggerConfig**: Expose beans to Spring
- **LoggingSystem**: Container for logger components

### 3. No @Lazy Annotations Required

The factory pattern ensures proper initialization order, eliminating the need for `@Lazy` annotations:

```java
// Clean approach with factory
@Bean
public MessageRouter messageRouter(TestLoggerFactory.LoggingSystem system) {
    return system.getRouter(); // Already properly initialized
}
```

### 4. Testable and Maintainable

The factory pattern makes it easy to:
- Create loggers for different test scenarios
- Mock specific components
- Test logger configuration independently
- Maintain consistent initialization order

## Common Use Cases

### Unit Tests

Use mock loggers with minimal configuration:

```java
TestLoggerFactory factory = new TestLoggerFactory();
MockActionLogger mockLogger = new MockActionLogger();
LoggingVerbosityConfig config = new LoggingVerbosityConfig();
config.setLevel(LogLevel.ERROR); // Only log errors in unit tests

TestLoggerFactory.LoggingSystem system = 
    factory.createTestLoggingSystem(mockLogger, config);
```

### Integration Tests

Use full logging with proper Spring configuration:

```java
@SpringBootTest
@Import(MockBrobotLoggerConfig.class)
class IntegrationTest {
    @Autowired
    private BrobotLogger logger;
    // Full logging system available
}
```

### Performance Tests

Use NoOp loggers for minimal overhead:

```java
TestLoggerFactory factory = new TestLoggerFactory();
NoOpActionLogger noOpLogger = new NoOpActionLogger();
LoggingVerbosityConfig config = new LoggingVerbosityConfig();
config.setEnabled(false); // Disable all logging

TestLoggerFactory.LoggingSystem system = 
    factory.createTestLoggingSystem(noOpLogger, config);
```

## Best Practices

1. **Use TestConfigurationManager for Early Initialization**
   - Always use `@ContextConfiguration(initializers = TestConfigurationManager.class)` for Spring tests
   - This ensures environment is configured before any beans are created

2. **Prefer Factory Pattern Over Direct Construction**
   - Use `TestLoggerFactory` instead of manually constructing logger components
   - This ensures proper initialization order

3. **Separate Concerns in Test Configuration**
   - Logger configuration: `MockBrobotLoggerConfig`
   - Action configuration: `TestActionConfig`
   - Environment setup: `TestConfigurationManager`

4. **Use Appropriate Logger for Test Type**
   - Unit tests: `MockActionLogger`
   - Integration tests: Full logger with `NoOpLogSink`
   - Performance tests: `NoOpActionLogger`

## Troubleshooting

### Spring Context Hanging

If tests hang during Spring context initialization:

1. Ensure `TestConfigurationManager` is included in `@ContextConfiguration`
2. Check that no `@PostConstruct` methods are blocking
3. Verify all configuration classes follow the factory pattern

### Missing Logger Beans

If Spring can't find logger beans:

1. Import `MockBrobotLoggerConfig` in your test
2. Ensure `TestLoggerFactory` is in the classpath
3. Check that `ActionLogger` implementation is available

### Static Initialization Conflicts

If you see conflicts with `ExecutionEnvironment`:

1. Set `brobot.preserve.headless.setting=true` system property
2. Use `TestConfigurationManager` for early initialization
3. Don't modify `ExecutionEnvironment` in `@PostConstruct` methods

## Related Documentation

- [Enhanced Mocking](./enhanced-mocking.md) - Mock mode configuration
- [Integration Testing](../../04-testing/integration-testing.md) - Integration test setup
- [CI/CD Testing](./ci-cd-testing.md) - Testing in CI environments
- [Test Utilities](../../04-testing/test-utilities.md) - Testing helper classes