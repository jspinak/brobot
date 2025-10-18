---
sidebar_position: 5
title: Test Logging Architecture (PROPOSED)
description: Proposed architectural patterns for test logging and configuration in Brobot
status: PROPOSED - NOT IMPLEMENTED
proposal_date: 2024
last_reviewed: 2025-01-16
implementation_status: No timeline scheduled
---

# Test Logging Architecture (PROPOSED)

:::caution Status: Proposed Design - No Implementation Planned
This document describes a **proposed architecture that has NOT been implemented** in the current Brobot codebase. The classes and patterns described here represent a design proposal that was explored but not prioritized for implementation.

**Implementation Status**: This is an architectural exploration without a scheduled implementation timeline. The current testing infrastructure works well for Brobot's needs using the patterns documented in the official testing guides below.

**Current Status:**
- ❌ TestLoggerFactory - Does NOT exist
- ❌ MockBrobotLoggerConfig - Does NOT exist
- ❌ LoggingSystem inner class - Does NOT exist
- ✅ TestConfigurationManager - EXISTS (as documented)
- ✅ ExecutionEnvironment - EXISTS (as documented)
- ✅ BrobotIntegrationTestBase - EXISTS (as documented)

**For current testing approaches**, see:
- [Unit Testing Guide](../../04-testing/unit-testing.md) - Current unit test patterns
- [Integration Testing Guide](../../04-testing/integration-testing.md) - Current integration test setup
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md) - Current mock testing approach
- [Test Utilities](../../04-testing/test-utilities.md) - Available testing utilities

**Alternative Implementation:** For mock logging in tests, see `MockLoggerFactory` in `library/src/test/java/io/github/jspinak/brobot/test/mock/MockLoggerFactory.java`
:::

## Overview

This document proposes a test logging architecture that would follow Single Responsibility Principle (SRP) and clean architecture patterns to avoid circular dependencies and Spring initialization issues. The proposed design would introduce TestLoggerFactory and related components for proper test configuration.

## Key Components (Proposed)

### TestLoggerFactory (PROPOSED)

The proposed `TestLoggerFactory` would be a factory class that creates and wires logging components in the correct order, ensuring no circular dependencies and proper initialization.

**Proposed Location**: `library-test/src/test/java/io/github/jspinak/brobot/test/logging/TestLoggerFactory.java` (NOT IMPLEMENTED)

**Single Responsibility**: Create and wire logger components for tests

:::warning Non-Functional Code
This code example will NOT compile because the classes referenced do not exist in the current codebase. This is theoretical design documentation.
:::

```java
// PROPOSED - NOT IMPLEMENTED
package io.github.jspinak.brobot.test.logging;

import io.github.jspinak.brobot.logging.*;
import org.springframework.stereotype.Component;

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

### TestConfigurationManager (IMPLEMENTED)

The `TestConfigurationManager` initializes the test environment before Spring context loads, preventing static initialization conflicts. **This class EXISTS and works as documented.**

**Location**: `library-test/src/test/java/io/github/jspinak/brobot/test/config/TestConfigurationManager.java` ✅

**Single Responsibility**: Initialize test environment before Spring context

:::tip Functional Code
This example is simplified but based on real, working code. The actual TestConfigurationManager exists in the codebase.
:::

```java
package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

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

## Usage Examples (Proposed)

:::warning Non-Functional Examples
All examples in this section reference classes that do NOT exist and will NOT compile. These are theoretical design examples only.

**For working test examples**, see:
- [Unit Testing Guide](../../04-testing/unit-testing.md)
- [Integration Testing Guide](../../04-testing/integration-testing.md)
- [Mock Mode Guide](../../04-testing/mock-mode-guide.md)
:::

### 1. Basic Test Configuration (PROPOSED)

For tests that would need logging but don't require Spring context:

```java
// PROPOSED - WILL NOT COMPILE
package io.github.jspinak.brobot.test.examples;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.logging.TestLoggerFactory;
import io.github.jspinak.brobot.logging.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

### 2. Spring Integration Test Configuration (PROPOSED)

For Spring-based integration tests, the proposed approach would use configuration classes:

```java
// PROPOSED - WILL NOT COMPILE (MockBrobotLoggerConfig doesn't exist)
package io.github.jspinak.brobot.test.examples;

import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.test.config.TestConfigurationManager;
import io.github.jspinak.brobot.logging.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

### 3. Custom Test Configuration (PROPOSED)

Create your own test configuration using the proposed factory pattern:

```java
// PROPOSED - WILL NOT COMPILE
package io.github.jspinak.brobot.test.examples;

import io.github.jspinak.brobot.test.logging.TestLoggerFactory;
import io.github.jspinak.brobot.logging.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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

## Architectural Benefits (Proposed Design)

### 1. No Circular Dependencies

The proposed factory pattern would ensure components are created in the correct order:

```
LoggingContext → ConsoleFormatter → MessageRouter → BrobotLogger → ConsoleReporterInitializer
```

Each arrow represents a one-way dependency with no cycles.

### 2. Single Responsibility Principle

Each proposed component would have a clear, single responsibility:

- **TestLoggerFactory** (proposed): Create logger components
- **TestConfigurationManager** (implemented ✅): Initialize environment
- **MockBrobotLoggerConfig** (proposed): Expose beans to Spring
- **LoggingSystem** (proposed): Container for logger components

### 3. No @Lazy Annotations Required

The proposed factory pattern would ensure proper initialization order, eliminating the need for `@Lazy` annotations:

```java
// PROPOSED - Clean approach with factory
@Bean
public MessageRouter messageRouter(TestLoggerFactory.LoggingSystem system) {
    return system.getRouter(); // Would be properly initialized
}
```

### 4. Testable and Maintainable

The proposed factory pattern would make it easy to:
- Create loggers for different test scenarios
- Mock specific components
- Test logger configuration independently
- Maintain consistent initialization order

## Common Use Cases (Proposed)

:::warning Non-Functional Examples
All code examples in this section reference classes that do NOT exist. See the [Unit Testing Guide](../../04-testing/unit-testing.md) and [Mock Mode Guide](../../04-testing/mock-mode-guide.md) for working examples.
:::

### Unit Tests (PROPOSED)

Use mock loggers with minimal configuration:

```java
// PROPOSED - WILL NOT COMPILE
TestLoggerFactory factory = new TestLoggerFactory();
MockActionLogger mockLogger = new MockActionLogger();
LoggingVerbosityConfig config = new LoggingVerbosityConfig();
config.setLevel(LogLevel.ERROR); // Only log errors in unit tests

TestLoggerFactory.LoggingSystem system = 
    factory.createTestLoggingSystem(mockLogger, config);
```

### Integration Tests (PROPOSED)

Use full logging with proper Spring configuration:

```java
// PROPOSED - WILL NOT COMPILE (MockBrobotLoggerConfig doesn't exist)
@SpringBootTest
@Import(MockBrobotLoggerConfig.class)
class IntegrationTest {
    @Autowired
    private BrobotLogger logger;
    // Full logging system would be available
}
```

### Performance Tests (PROPOSED)

Use NoOp loggers for minimal overhead:

```java
// PROPOSED - WILL NOT COMPILE
TestLoggerFactory factory = new TestLoggerFactory();
NoOpActionLogger noOpLogger = new NoOpActionLogger();
LoggingVerbosityConfig config = new LoggingVerbosityConfig();
config.setEnabled(false); // Disable all logging

TestLoggerFactory.LoggingSystem system = 
    factory.createTestLoggingSystem(noOpLogger, config);
```

## Best Practices (Proposed Design)

:::info Current Best Practices
For current testing best practices, see:
- [Unit Testing Guide](../../04-testing/unit-testing.md)
- [Integration Testing Guide](../../04-testing/integration-testing.md)
- [Test Utilities](../../04-testing/test-utilities.md)
:::

1. **Use TestConfigurationManager for Early Initialization** ✅ (IMPLEMENTED)
   - Always use `@ContextConfiguration(initializers = TestConfigurationManager.class)` for Spring tests
   - This ensures environment is configured before any beans are created

2. **Prefer Factory Pattern Over Direct Construction** (PROPOSED - NOT IMPLEMENTED)
   - Proposed: Use `TestLoggerFactory` instead of manually constructing logger components
   - Current: Use `MockLoggerFactory` from `library/src/test/java/io/github/jspinak/brobot/test/mock/`

3. **Separate Concerns in Test Configuration**
   - Logger configuration: `MockBrobotLoggerConfig` (PROPOSED - NOT IMPLEMENTED)
   - Action configuration: `TestActionConfig` (EXISTS)
   - Environment setup: `TestConfigurationManager` (EXISTS ✅)

4. **Use Appropriate Logger for Test Type** (PROPOSED)
   - Unit tests: `MockActionLogger` (proposed)
   - Integration tests: Full logger with `NoOpLogSink` (proposed)
   - Performance tests: `NoOpActionLogger` (proposed)

## Troubleshooting (Proposed Architecture)

:::tip Current Troubleshooting
For current testing issues and solutions, see:
- [Testing Introduction](../../04-testing/testing-intro.md)
- [Profile-Based Testing](../../04-testing/profile-based-testing.md)
- [Test Execution Solution](../../../TEST-EXECUTION-SOLUTION.md)
:::

### Spring Context Hanging ✅ (Applies to current architecture)

If tests hang during Spring context initialization:

1. Ensure `TestConfigurationManager` is included in `@ContextConfiguration` ✅
2. Check that no `@PostConstruct` methods are blocking ✅
3. Verify all configuration classes follow proper patterns ✅

### Missing Logger Beans (PROPOSED)

If Spring can't find logger beans in the proposed architecture:

1. Import `MockBrobotLoggerConfig` in your test (PROPOSED - class doesn't exist)
2. Ensure `TestLoggerFactory` is in the classpath (PROPOSED - class doesn't exist)
3. Check that `ActionLogger` implementation is available

### Static Initialization Conflicts ✅ (Applies to current architecture)

If you see conflicts with `ExecutionEnvironment`:

1. Set `brobot.preserve.headless.setting=true` system property ✅
2. Use `TestConfigurationManager` for early initialization ✅
3. Don't modify `ExecutionEnvironment` in `@PostConstruct` methods ✅

## Related Documentation

- [Enhanced Mocking](./enhanced-mocking.md) - Mock mode configuration
- [Integration Testing](../../04-testing/integration-testing.md) - Integration test setup
- [CI/CD Testing](./ci-cd-testing.md) - Testing in CI environments
- [Test Utilities](../../04-testing/test-utilities.md) - Testing helper classes