# Brobot Testing Strategy

## Overview
This document outlines the comprehensive testing strategy for the Brobot framework, including unit tests, integration tests, and best practices.

## Test Categories

### 1. Unit Tests
- **Location**: `src/test/java/.../unit/`
- **Naming**: `*Test.java`
- **Coverage Target**: 80% minimum
- **Mock Strategy**: Use Mockito for external dependencies

### 2. Integration Tests
- **Location**: `src/test/java/.../integration/`
- **Naming**: `*IntegrationTest.java`
- **Coverage Target**: 60% minimum
- **Mock Strategy**: Use Brobot's built-in mock mode

### 3. Performance Tests
- **Location**: `src/test/java/.../performance/`
- **Naming**: `*BenchmarkTest.java`
- **Execution**: Separate profile, not in CI

## Test Base Classes

### BrobotTestBase
All Brobot tests MUST extend `BrobotTestBase`:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.MockModeManager;

public class MyTest extends BrobotTestBase {
    @Test
    public void testFeature() {
        // Mock mode is automatically enabled via MockModeManager
        assertTrue(MockModeManager.isMockMode());
        // Test runs in headless environment
    }
}
```

**Benefits:**
- Automatic mock mode activation via centralized `MockModeManager`
- Synchronizes mock settings across all components (ExecutionEnvironment, FrameworkSettings, system properties)
- Headless environment compatibility
- Consistent test configuration
- Fast execution (0.01-0.04s per operation)

### Custom Test Base Classes

```java
public abstract class ActionTestBase extends BrobotTestBase {
    @Mock protected Action action;
    @Mock protected ActionResult actionResult;
    
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
    assertTrue(MockModeManager.isMockMode());
    // This also ensures FrameworkSettings.mock is synchronized
    
    // When - perform action
    ActionResult result = action.perform(config, objectCollection);
    
    // Then - verify mock behavior
    assertTrue(result.isSuccess());
    assertNotNull(result.getDuration());
    assertTrue(result.getDuration() < 0.1); // Fast in mock mode
}
```

### Testing Mock Scenarios

```java
@Test
@DisplayName("Complex scenario in mock mode")
public void testComplexScenario() {
    // Given - configure mock scenario
    MockScenario scenario = MockScenario.builder()
        .setInitialState("LoginScreen")
        .addTransition("LoginScreen", "login", "Dashboard")
        .addImage("LoginScreen", "username_field.png")
        .addImage("LoginScreen", "password_field.png")
        .build();
    
    mockManager.loadScenario(scenario);
    
    // When - execute workflow
    WorkflowResult result = workflow.execute();
    
    // Then - verify transitions
    assertEquals("Dashboard", result.getFinalState());
    assertEquals(2, result.getActionsPerformed());
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
public class TestDataBuilder {
    
    public static StateImage createTestImage(String name) {
        return StateImage.builder()
            .setName(name)
            .setOwnerStateName("TestState")
            .setSearchRegion(createTestRegion())
            .build();
    }
    
    public static Region createTestRegion() {
        return Region.builder()
            .setX(100)
            .setY(100)
            .setW(200)
            .setH(150)
            .build();
    }
    
    public static ObjectCollection createTestCollection() {
        return ObjectCollection.builder()
            .addStateImage(createTestImage("test1"))
            .addStateImage(createTestImage("test2"))
            .build();
    }
}
```

## Test Utilities

### Custom Assertions

```java
public class BrobotAssertions {
    
    public static void assertActionSucceeded(ActionResult result) {
        assertAll(
            () -> assertTrue(result.isSuccess(), "Action should succeed"),
            () -> assertFalse(result.getMatches().isEmpty(), "Should have matches"),
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
@TestFixture
public class ActionTestFixtures {
    
    public static final ClickOptions SINGLE_CLICK = ClickOptions.builder()
        .setNumberOfClicks(1)
        .build();
    
    public static final ClickOptions DOUBLE_CLICK = ClickOptions.builder()
        .setNumberOfClicks(2)
        .build();
    
    public static final PatternFindOptions QUICK_FIND = PatternFindOptions.builder()
        .setStrategy(PatternFindOptions.Strategy.FIRST)
        .setSimilarity(0.7)
        .setSearchDuration(1.0)
        .build();
    
    public static final PatternFindOptions PRECISE_FIND = PatternFindOptions.builder()
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
   - **Check**: `FrameworkSettings.mock` should be `true`

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
   - Use `MockModeManager.isMockMode()` instead of checking individual flags
2. **Use setXxx() naming** in all builder calls
3. **Group related tests** with `@Nested` classes
4. **Use descriptive names** with `@DisplayName`
5. **Test both success and failure** scenarios
6. **Mock external dependencies** appropriately
7. **Keep tests independent** - no shared state
8. **Use test fixtures** for common data
9. **Document complex test scenarios** with comments
10. **Run tests locally** before pushing