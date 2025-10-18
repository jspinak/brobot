---
sidebar_position: 1
title: Testing Strategy
description: Comprehensive testing strategy and best practices for Brobot framework
---

# Brobot Testing Strategy

## Overview
This document outlines the comprehensive testing strategy for the Brobot framework, including unit tests, integration tests, and best practices.

## Test Categories

Brobot uses **tag-based test organization** rather than directory structure. Tests are organized by package/feature with test type indicated by JUnit 5 tags.

### 1. Unit Tests
- **Tag**: `@Tag("unit")`
- **Naming**: `*Test.java`
- **Coverage Target**: 70% overall, 60% per-class minimum
- **Mock Strategy**: Use Mockito for external dependencies
- **Execution**: `./gradlew unitTest`

### 2. Integration Tests
- **Tag**: `@Tag("integration")`
- **Naming**: `*IntegrationTest.java`
- **Coverage Target**: 60% per-class minimum
- **Mock Strategy**: Use Brobot's built-in mock mode via `BrobotTestBase`
- **Execution**: `./gradlew integrationTest`

### 3. Performance Tests
- **Tag**: `@Tag("performance")`
- **Naming**: `*BenchmarkTest.java`
- **Execution**: Separate profile, not in CI

## Test Base Classes

### BrobotTestBase
All Brobot tests MUST extend `BrobotTestBase`:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyTest extends BrobotTestBase {

    @Autowired
    private BrobotProperties brobotProperties;

    @Test
    public void testFeature() {
        // Mock mode is automatically enabled via MockModeManager
        assertTrue(brobotProperties.getCore().isMock());
        // Test runs in headless environment
        // Fast test profile timings: 0.005-0.04s per operation
    }
}
```

**Benefits:**
- Automatic mock mode activation via centralized [MockModeManager](./mock-mode-manager.md)
- Synchronizes mock settings across all components (ExecutionEnvironment, system properties, Spring configuration)
- Headless environment compatibility (works in CI/CD)
- Consistent test configuration
- Fast execution with test profile (0.005-0.04s per operation)

### Custom Test Base Classes

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.reports.ActionResult;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class ActionTestBase extends BrobotTestBase {

    @Mock
    protected Action action;

    @Mock
    protected ActionResult actionResult;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        // Common action test setup
    }
}
```

## Builder Pattern Testing

### Testing Builders with New Convention

```java
@Test
@DisplayName("Builder creates valid configuration with setter methods")
public void testBuilderWithSetters() {
    // Given - use setXxx naming convention
    MyOptions options = MyOptions.builder()
        .setField1("value1")
        .setField2(42)
        .setNestedOptions(NestedOptions.builder()
            .setSomething("nested")
            .build())
        .build();
    
    // Then - verify all fields
    assertAll(
        () -> assertEquals("value1", options.getField1()),
        () -> assertEquals(42, options.getField2()),
        () -> assertNotNull(options.getNestedOptions()),
        () -> assertEquals("nested", options.getNestedOptions().getSomething())
    );
}
```

### Testing Default Values

```java
@Test
@DisplayName("Builder uses correct defaults")
public void testBuilderDefaults() {
    // When - build with no setters
    MyOptions options = MyOptions.builder().build();
    
    // Then - verify defaults
    assertAll(
        () -> assertEquals(DEFAULT_VALUE_1, options.getField1()),
        () -> assertEquals(DEFAULT_VALUE_2, options.getField2()),
        () -> assertNotNull(options.getNestedOptions())
    );
}
```

### Testing Immutability

```java
@Test
@DisplayName("Options objects are immutable")
public void testImmutability() {
    // Given
    MyOptions original = MyOptions.builder()
        .setField1("original")
        .build();
    
    // When - create new instance with toBuilder
    MyOptions modified = original.toBuilder()
        .setField1("modified")
        .build();
    
    // Then - original unchanged
    assertEquals("original", original.getField1());
    assertEquals("modified", modified.getField1());
}
```

## Mock Mode Testing

### Testing with Brobot Mock Mode

```java
@Test
@DisplayName("Action works in mock mode")
public void testActionInMockMode() {
    // Given - mock mode enabled by BrobotTestBase via MockModeManager
    assertTrue(brobotProperties.getCore().isMock());
    // This also ensures brobotProperties.getCore().isMock() is synchronized
    
    // When - perform action
    ActionResult result = action.perform(config, objectCollection);
    
    // Then - verify mock behavior
    assertTrue(result.isSuccess());
    assertNotNull(result.getDuration());
    assertTrue(result.getDuration() < 0.1); // Fast in mock mode
}
```

### Testing Complex Mock Workflows

For complex multi-step workflows in mock mode, use state transitions and ActionHistory. See [Mock Mode Guide](./mock-mode-guide.md) and [ActionHistory Integration Testing](./actionhistory-integration-testing.md) for details.

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.StateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Complex workflow in mock mode")
public class WorkflowTest extends BrobotTestBase {

    @Autowired
    private StateTransitions stateTransitions;

    @Autowired
    private StateService stateService;

    @Test
    @DisplayName("Login workflow transitions to dashboard")
    public void testLoginWorkflow() {
        // Given - initial state
        stateService.activateState("LoginScreen");

        // When - execute transition
        boolean success = stateTransitions.execute("login");

        // Then - verify transition
        assertTrue(success);
        assertTrue(stateService.isActive("Dashboard"));
        assertFalse(stateService.isActive("LoginScreen"));
    }
}
```

## JSON Serialization Testing

### Testing Jackson Serialization

```java
@Test
@DisplayName("Options serialize to JSON correctly")
public void testJsonSerialization() throws Exception {
    // Given
    ObjectMapper mapper = new ObjectMapper();
    MyOptions original = MyOptions.builder()
        .setField1("test")
        .setField2(123)
        .build();
    
    // When
    String json = mapper.writeValueAsString(original);
    MyOptions deserialized = mapper.readValue(json, MyOptions.class);
    
    // Then
    assertEquals(original.getField1(), deserialized.getField1());
    assertEquals(original.getField2(), deserialized.getField2());
}
```

### Testing Polymorphic Deserialization

```java
@Test
@DisplayName("Polymorphic types deserialize correctly")
public void testPolymorphicDeserialization() throws Exception {
    // Given
    String json = "{\"@type\":\"ClickOptions\",\"numberOfClicks\":2}";
    ObjectMapper mapper = new ObjectMapper();
    
    // When
    ActionConfig config = mapper.readValue(json, ActionConfig.class);
    
    // Then
    assertInstanceOf(ClickOptions.class, config);
    assertEquals(2, ((ClickOptions) config).getNumberOfClicks());
}
```

## Test Data Builders

### Creating Test Data

```java
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;

public class TestDataBuilder {

    public static StateImage createTestImage(String name) {
        return new StateImage.Builder()
            .setName(name)
            .setOwnerStateName("TestState")
            .setSearchRegionForAllPatterns(createTestRegion())
            .build();
    }

    public static Region createTestRegion() {
        return Region.builder()
            .withRegion(100, 100, 200, 150)  // x, y, width, height
            .build();
    }

    public static ObjectCollection createTestCollection() {
        return new ObjectCollection.Builder()
            .withImages(createTestImage("test1"), createTestImage("test2"))
            .build();
    }
}
```

## Test Utilities

### Custom Assertions

```java
import io.github.jspinak.brobot.reports.ActionResult;
import io.github.jspinak.brobot.actions.actionConfigs.ActionConfig;
import static org.junit.jupiter.api.Assertions.*;

public class BrobotAssertions {

    public static void assertActionSucceeded(ActionResult result) {
        assertAll(
            () -> assertTrue(result.isSuccess(), "Action should succeed"),
            () -> assertFalse(result.getMatchList().isEmpty(), "Should have matches"),
            () -> assertNotNull(result.getDuration(), "Should have duration"),
            () -> assertTrue(result.getDuration() >= 0, "Duration should be positive")
        );
    }

    public static void assertOptionsValid(ActionConfig options) {
        assertAll(
            () -> assertNotNull(options, "Options should not be null"),
            () -> assertTrue(options.getPauseBeforeBegin() >= 0, "Pause before should be >= 0"),
            () -> assertTrue(options.getPauseAfterEnd() >= 0, "Pause after should be >= 0")
        );
    }
}
```

### Test Fixtures

```java
import io.github.jspinak.brobot.actions.actionConfigs.ClickOptions;
import io.github.jspinak.brobot.actions.actionConfigs.PatternFindOptions;

/**
 * Common test fixtures for action testing.
 * Use these constants for consistent test data across test classes.
 */
public class ActionTestFixtures {

    public static final ClickOptions SINGLE_CLICK = new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build();

    public static final ClickOptions DOUBLE_CLICK = new ClickOptions.Builder()
        .setNumberOfClicks(2)
        .build();

    public static final PatternFindOptions QUICK_FIND = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .setSimilarity(0.7)
        .setSearchDuration(1.0)
        .build();

    public static final PatternFindOptions PRECISE_FIND = new PatternFindOptions.Builder()
        .setStrategy(PatternFindOptions.Strategy.BEST)
        .setSimilarity(0.95)
        .setSearchDuration(5.0)
        .build();
}
```

## Continuous Integration

### CI Test Configuration

```yaml
# .github/workflows/test.yml
name: Test Suite

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        
    - name: Cache Gradle packages
      uses: actions/cache@v2
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
        
    - name: Run tests
      run: ./gradlew test --no-daemon
      env:
        BROBOT_MOCK_MODE: true
        
    - name: Generate test report
      if: always()
      run: ./gradlew jacocoTestReport
      
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v2
      with:
        file: ./build/reports/jacoco/test/jacocoTestReport.xml
```

## Troubleshooting

### Common Test Issues

1. **HeadlessException in CI**
   - **Solution**: Ensure all tests extend `BrobotTestBase`
   - **Check**: `brobotProperties.getCore().isMock()` should be `true`

2. **Slow Test Execution**
   - **Solution**: Use mock mode for unit tests
   - **Check**: Mock timings should be 0.01-0.04s

3. **Flaky Tests**
   - **Solution**: Use deterministic mock scenarios
   - **Check**: Avoid timing-dependent assertions

4. **Serialization Failures**
   - **Solution**: Add proper Jackson annotations
   - **Check**: `@JsonDeserialize`, `@JsonPOJOBuilder`

## Best Practices

1. **Always use BrobotTestBase** for consistent behavior
   - Automatically configures mock mode via `MockModeManager`
   - Use `brobotProperties.getCore().isMock()` instead of checking individual flags
2. **Use setXxx() naming** in ActionConfig builder calls
3. **Use withXxx() naming** in domain model builders (Region, ObjectCollection)
4. **Group related tests** with `@Nested` classes
5. **Use descriptive names** with `@DisplayName`
6. **Test both success and failure** scenarios
7. **Mock external dependencies** appropriately
8. **Keep tests independent** - no shared state
9. **Use test fixtures** for common data
10. **Document complex test scenarios** with comments
11. **Run tests locally** before pushing
12. **Use @Tag annotations** to categorize tests (unit, integration, performance)

## Related Documentation

### Essential Testing Guides
- **[Testing Introduction](./testing-intro.md)** - Start here for overview of Brobot testing approaches
- **[Unit Testing Guide](./unit-testing.md)** - Detailed unit testing patterns with BrobotTestBase
- **[Integration Testing Guide](./integration-testing.md)** - Spring Boot integration testing patterns
- **[Profile-Based Testing](./profile-based-testing.md)** - Using test profiles for configuration isolation

### Mock Mode Testing
- **[Mock Mode Guide](./mock-mode-guide.md)** - Comprehensive guide to Brobot's mock framework
- **[Mock Mode Manager](./mock-mode-manager.md)** - Centralized mock configuration system
- **[Mock Mode Migration](./mock-mode-migration.md)** - Migrating legacy tests to use mock mode
- **[Mock Stochasticity](./mock-stochasticity.md)** - Understanding probabilistic mock behavior
- **[ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md)** - Creating and using mock data
- **[Action Recording](./action-recording.md)** - Recording actions for test creation
- **[ActionHistory Integration Testing](./actionhistory-integration-testing.md)** - Testing with action history

### Testing Utilities
- **[Test Utilities](./test-utilities.md)** - Available testing helper classes and methods
- **[Mat Testing Utilities](./mat-testing-utilities.md)** - OpenCV Mat testing utilities for image operations

### ActionConfig Documentation
- **[ActionConfig Overview](../03-core-library/action-config/01-overview.md)** - Introduction to ActionConfig system
- **[ActionConfig Examples](../03-core-library/action-config/03-examples.md)** - Practical examples for testing
- **[ActionConfig Reference](../03-core-library/action-config/05-reference.md)** - Complete API reference
- **[Action Chaining](../03-core-library/action-config/07-action-chaining.md)** - Chaining actions in tests
- **[Troubleshooting Chains](../03-core-library/action-config/troubleshooting-chains.md)** - Debugging action chains

### Configuration & Setup
- **[BrobotProperties Usage](../03-core-library/configuration/brobot-properties-usage.md)** - Using BrobotProperties in tests
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete properties documentation
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Headless test environments
- **[Auto Configuration](../03-core-library/configuration/auto-configuration.md)** - Spring Boot auto-configuration

### Debugging & Troubleshooting
- **[Debugging Pattern Matching](./debugging-pattern-matching.md)** - Troubleshooting image matching in tests
- **[Fail-Safe Image Loading](./fail-safe-image-loading.md)** - Robust image loading for tests

### Migration Guides
- **[ActionOptions to ActionConfig Migration](../03-core-library/migration/actionoptions-to-actionconfig.md)** - Updating legacy test code
- **[ActionConfig Migration Guide](../03-core-library/action-config/12-migration-guide.md)** - Complete migration patterns