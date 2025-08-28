---
sidebar_position: 10
title: 'Enhanced Mock Testing System'
---

# Enhanced Mock Testing System

:::info For Framework Developers
This section covers **advanced mocking features** for developers extending the Brobot framework or building complex testing scenarios. 

For standard automation testing, see the [main Testing documentation](/docs/testing/testing-intro).
:::

Brobot's enhanced mock testing system provides sophisticated scenario-based testing capabilities with advanced failure patterns, verification, and structured test data management.

## Overview

The enhanced mock system extends the basic mocking capabilities with:

- **Centralized mock mode management** via `MockModeManager`
- **Scenario-based configurations** for complex test conditions
- **Advanced failure patterns** with temporal and cascading behaviors  
- **Behavioral verification** beyond simple operation counting
- **Structured test data builders** with variations and versioning
- **Performance optimization** for large-scale test execution

### Centralized Mock Mode Management

Brobot now provides the `MockModeManager` class as a single source of truth for mock mode configuration:

:::note Clean Test Configuration
For Spring-based tests, see the [Test Logging Architecture](/docs/core-library/testing/test-logging-architecture) which provides factory-based configuration with clean dependencies.
:::

```java
import io.github.jspinak.brobot.config.MockModeManager;
import io.github.jspinak.brobot.test.logging.TestLoggerFactory;

// Enable mock mode across all components
MockModeManager.setMockMode(true);

// Check if mock mode is active
if (MockModeManager.isMockMode()) {
    // Execute mock-specific logic
}

// Debug mock mode state
MockModeManager.logMockModeState();
```

This ensures consistency across:
- System properties
- ExecutionEnvironment
- FrameworkSettings
- All other mock-aware components

## Mock Scenario Configuration

### Basic Scenario Setup

All test classes should extend `BrobotTestBase` for automatic mock mode configuration:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;

public class MyScenarioTest extends BrobotTestBase {
    
    @Test
    public void testLoginUnderNetworkIssues() {
    MockScenarioConfig scenario = MockScenarioConfig.builder()
        .scenarioName("login_network_issues")
        .description("Simulate intermittent network connectivity during login")
        .stateAppearanceProbability("LOGIN_STATE", 0.8)  // 80% appear rate
        .stateAppearanceProbability("DASHBOARD", 0.9)    // 90% appear rate
        .build();
        
    // Apply scenario and run test
    mockScenarioManager.activateScenario(scenario);
    
    // Your test logic here
    ActionResult result = actions.find(loginButton);
    
    assertTrue(result.isSuccess());
}
```

### Advanced Failure Patterns

```java
@Test 
public void testRetryBehaviorWithCascadingFailures() {
    // Configure cascading failures that worsen over time
    FailurePattern cascadingFailure = FailurePattern.builder()
        .baseProbability(0.3)              // Start with 30% failure rate
        .cascading(true)                   // Enable cascading
        .cascadeMultiplier(1.5)            // Each failure increases probability by 50%
        .maxConsecutiveFailures(3)         // Force success after 3 failures
        .recoveryDelay(Duration.ofSeconds(2))  // 2-second recovery period
        .failureMessage("Network timeout")
        .build();
        
    MockScenarioConfig scenario = MockScenarioConfig.builder()
        .scenarioName("cascading_network_failures")
        .actionFailurePattern(ActionType.FIND, cascadingFailure)
        .maxDuration(Duration.ofMinutes(5))  // Scenario timeout
        .build();
        
    mockScenarioManager.activateScenario(scenario);
    
    // Test retry logic
    for (int attempt = 1; attempt <= 5; attempt++) {
        // Find with pause before next retry
        PatternFindOptions findWithPause = new PatternFindOptions.Builder()
            .setPauseAfterEnd(0.5)  // 500ms pause if not found
            .build();
        ActionResult result = actions.perform(findWithPause, targetElement);
        if (result.isSuccess()) break;
    }
}
```

### Temporal Conditions

```java
@Test
public void testPerformanceUnderLoad() {
    TemporalConditions slowNetwork = TemporalConditions.builder()
        .baseDelay(Duration.ofMillis(500))        // Base 500ms delay
        .maximumDelay(Duration.ofSeconds(3))      // Cap at 3 seconds
        .delayProgression(Duration.ofMillis(100)) // Increase by 100ms each time
        .randomVariation(0.2)                     // ±20% random variation
        .activeTimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0)) // Business hours
        .build();
        
    MockScenarioConfig scenario = MockScenarioConfig.builder()
        .scenarioName("performance_degradation")
        .temporalCondition("slow_network", slowNetwork)
        .build();
        
    mockScenarioManager.activateScenario(scenario);
    
    long startTime = System.currentTimeMillis();
    ActionResult result = actions.find(targetElement);
    long duration = System.currentTimeMillis() - startTime;
    
    assertTrue("Action should take longer under load", duration >= 500);
    assertTrue(result.isSuccess());
}
```

## Behavioral Verification

### State Transition Verification

```java
@Test
public void testLoginFlowTransitionSequence() {
    // Set up verification for expected state transitions
    StateTransitionVerification verification = mockBehaviorVerifier
        .expectTransitionSequence("login_flow")
        .fromState("INITIAL")
        .toState("LOGIN_PAGE")
        .maxDuration(Duration.ofSeconds(2))  // Transition should be fast
        .fromState("LOGIN_PAGE") 
        .toState("AUTHENTICATING")
        .minDuration(Duration.ofMillis(100)) // Should take some time to authenticate
        .maxDuration(Duration.ofSeconds(5))
        .fromState("AUTHENTICATING")
        .toState("DASHBOARD")
        .withinTime(Duration.ofSeconds(10))  // Overall sequence timeout
        .verify();
        
    // Execute the login flow
    performLoginSequence();
    
    // Verify the transitions occurred as expected
    assertEquals(VerificationResult.PASSED, verification.getResult());
    assertTrue("No transition errors", verification.getErrors().isEmpty());
}
```

### Action Pattern Verification

```java
@Test
public void testRetryPatternCompliance() {
    // Verify that find operations retry with proper backoff
    ActionPatternVerification retryVerification = mockBehaviorVerifier
        .expectActionPattern("find_retry_pattern")
        .action(ActionType.FIND)
        .maxAttempts(3)
        .withBackoff(Duration.ofMillis(500))
        .expectedSuccessRate(0.8)  // 80% should eventually succeed
        .within(Duration.ofSeconds(10))
        .verify();
        
    // Execute actions that may need retries
    ActionResult result1 = actions.find(intermittentElement);
    ActionResult result2 = actions.find(intermittentElement);
    ActionResult result3 = actions.find(intermittentElement);
    
    // Verify retry behavior
    assertEquals(VerificationResult.PASSED, retryVerification.getResult());
    assertTrue("Retry timing should be correct", retryVerification.getErrors().isEmpty());
}
```

## Structured Test Data Builder

### Creating Test Scenarios

```java
@Test
public void testWithStructuredData() {
    TestScenario loginScenario = testDataBuilder
        .scenario("comprehensive_login_test")
        .withDescription("Complete login flow with variations")
        .withVersion("1.2.0")
        .withBaselineData()
        .withStateImage("login_button", "login_btn.png")
        .withStateImage("username_field", "username_input.png") 
        .withStateString("welcome_text", "Welcome back!")
        .withRegion("login_form", new Region(300, 200, 400, 300))
        .withTag("authentication")
        .withTag("critical_path")
        .build();
        
    // Use the structured scenario in your test
    StateImage loginButton = loginScenario.getStateImages().get("login_button");
    Region formArea = loginScenario.getRegions().get("login_form");
    
    ActionResult result = actions.find(loginButton).searchRegions(formArea);
    assertTrue(result.isSuccess());
}
```

### Test Variations

```java
@Test
public void testMobileLayoutVariation() {
    TestScenario baseScenario = testDataBuilder.loginScenario().build();
    
    // Create mobile variation with adjusted similarity thresholds
    TestScenario mobileScenario = baseScenario.withVariation("small_screen");
    
    // Mobile scenario automatically has:
    // - Reduced similarity thresholds for scaled elements
    // - Adjusted regions for smaller screen
    // - Modified timing expectations
    
    StateImage mobileLoginButton = mobileScenario.getStateImages().get("login_button");
    assertTrue("Mobile button has lower similarity threshold", 
               mobileLoginButton.getSimilarity() < 0.8);
               
    ActionResult result = actions.find(mobileLoginButton);
    assertTrue(result.isSuccess());
}
```

### Custom Variations

```java
@Test
public void testHighContrastVariation() {
    TestScenario scenario = testDataBuilder
        .scenario("high_contrast_test")
        .withStateImage("button", "normal_button.png")
        .withVariation("high_contrast")
            .withDescription("High contrast accessibility mode")
            .withTransformation("reduce_similarity", (name, obj) -> {
                if (obj instanceof StateImage) {
                    return ((StateImage) obj).toBuilder()
                        .similarity(Math.max(0.6, ((StateImage) obj).getSimilarity() - 0.15))
                        .build();
                }
                return obj;
            })
        .endVariation()
        .build();
        
    TestScenario highContrastScenario = scenario.withVariation("high_contrast");
    
    // Test with high contrast variation
    ActionResult result = actions.find(
        highContrastScenario.getStateImages().get("button"));
    assertTrue(result.isSuccess());
}
```

## Base Test Configuration

### Using BrobotTestBase

All Brobot tests should extend `BrobotTestBase` to ensure proper mock mode configuration and consistent test behavior across different environments:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;

public class MyBrobotTest extends BrobotTestBase {
    
    @Test
    public void testMyFeature() {
        // Your test code here
        // Mock mode is automatically enabled
    }
}
```

#### What BrobotTestBase Provides

`BrobotTestBase` automatically configures the following for all tests:

1. **Mock Mode Activation** - Sets `FrameworkSettings.mock = true` to prevent SikuliX headless exceptions
2. **Fast Mock Timings** - Configures minimal delays for mock operations (0.01-0.04 seconds)
3. **Mouse Settings** - Removes mouse pause delays for faster test execution
4. **Screenshot Paths** - Sets up paths for mock screenshot operations
5. **Per-Test Reset** - Ensures mock mode remains enabled between tests

#### Key Benefits

- **CI/CD Compatibility** - Tests run without requiring a display or GUI
- **Consistent Behavior** - All tests use the same mock configuration
- **Fast Execution** - Minimal mock delays speed up test suites
- **Headless Support** - Prevents AWTException and HeadlessException errors

#### Customizing Test Setup

You can override `setupTest()` to add custom configuration:

```java
public class CustomTest extends BrobotTestBase {
    
    @Override
    @BeforeEach
    public void setupTest() {
        super.setupTest(); // Important: call parent setup first
        
        // Add your custom setup
        FrameworkSettings.mockTimeFindFirst = 0.05; // Custom timing
        // Other custom configuration...
    }
}
```

#### When to Use BrobotTestBase

- **Always** for unit tests that use Brobot APIs
- **Always** for integration tests in headless environments
- **Optional** for end-to-end tests that need real screen interaction (don't extend BrobotTestBase)

## Enhanced Mock Infrastructure

### Grid Operations in Mock Mode

Brobot now provides full grid operation support in mock mode through the `MockGridConfig` class:

```java
import io.github.jspinak.brobot.tools.testing.mock.grid.MockGridConfig;

@Test
public void testGridOperations() {
    // Configure grid dimensions for testing
    MockGridConfig.setDefaultGrid(3, 3); // 3x3 grid
    
    Region region = new Region(0, 0, 300, 300);
    Location location = new Location(150, 150); // Center
    
    // Grid operations work seamlessly in mock mode
    Optional<Integer> gridNumber = RegionUtils.getGridNumber(region, location);
    assertTrue(gridNumber.isPresent());
    assertEquals(4, gridNumber.get()); // Center cell in 3x3 grid
    
    // Get specific grid region
    Region gridRegion = region.getGridRegion(4);
    assertEquals(100, gridRegion.w());
    assertEquals(100, gridRegion.h());
}
```

#### MockGridConfig Features

- **Configurable Dimensions**: Set custom grid sizes with `setDefaultGrid(rows, cols)`
- **Thread-Safe**: Safe for use in parallel test execution
- **Fallback Calculations**: When SikuliX is unavailable, uses native Brobot calculations
- **Consistent Behavior**: Same API in mock and real modes

### Mock Scene and Color Analysis

The `MockSceneBuilder` provides comprehensive builders for creating test data for color analysis and scene processing:

```java
import io.github.jspinak.brobot.tools.testing.mock.builders.MockSceneBuilder;

@Test
public void testColorAnalysis() {
    // Create a mock scene with initialized image data
    Scene scene = MockSceneBuilder.createMockScene();
    assertNotNull(scene.getPattern());
    assertNotNull(scene.getPattern().getImage());
    
    // Create scene analysis with multiple profiles
    SceneAnalysis analysis = MockSceneBuilder.createMockSceneAnalysis(3);
    assertEquals(3, analysis.size());
    
    // Each profile has properly initialized color clusters
    PixelProfiles profile = analysis.getPixelAnalysisCollection(0);
    assertNotNull(profile.getStateImage().getColorCluster());
    
    // Color operations work without real images
    ColorClassifier classifier = new ColorClassifier();
    Mat indices = classifier.getImageIndices(analysis, ColorCluster.ColorSchemaName.BGR);
    assertNotNull(indices);
}
```

#### MockSceneBuilder Methods

| Method | Description |
|--------|-------------|
| `createMockScene()` | Creates Scene with valid Pattern and Image |
| `createMockPattern()` | Creates Pattern with BGR Mat image |
| `createMockSceneAnalysis(int)` | Creates SceneAnalysis with specified number of profiles |
| `createMockColorCluster()` | Creates ColorCluster with BGR and HSV schemas |
| `createMockColorSchema(ColorSchemaName)` | Creates schema with proper statistics |
| `sceneAnalysis()` | Returns builder for complex SceneAnalysis configurations |

#### Builder Pattern for Complex Scenarios

```java
@Test
public void testComplexSceneAnalysis() {
    // Use builder for complex configurations
    SceneAnalysis analysis = MockSceneBuilder.sceneAnalysis()
        .withScene(customScene)
        .withPixelProfile(0)
        .withPixelProfile(1)
        .withPixelProfile(2)
        .build();
    
    // Analysis is fully initialized and ready for testing
    assertEquals(3, analysis.getPixelAnalysisCollections().size());
}
```

### Performance Characteristics

Mock mode operations are significantly faster than real operations:

| Operation | Mock Mode | Real Mode | Speedup |
|-----------|-----------|-----------|---------|
| Grid operations | ~0.01s | 0.5s | 50x |
| Color analysis | ~0.02s | 1-2s | 50-100x |
| Pattern matching | ~0.01s | 0.5-2s | 50-200x |
| State transitions | ~0.01s | 0.2-1s | 20-100x |

### Jackson Serialization Support

When creating test objects that need serialization, ensure proper Jackson annotations:

```java
@Getter
@Builder(toBuilder = true, builderClassName = "Builder")
@JsonDeserialize(builder = MyTestData.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyTestData {
    
    private String field;
    
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        // Lombok generates implementation
    }
}
```

### Common Testing Pitfalls and Solutions

| Pitfall | Solution |
|---------|----------|
| Tests fail in headless environments | Always extend `BrobotTestBase` |
| SikuliX field mocking errors | Use real SikuliX objects or Brobot mocks |
| Grid operations return empty | Configure `MockGridConfig` dimensions |
| ColorClassifier NPEs | Use `MockSceneBuilder` for test data |
| Forget to call `super.setupTest()` | Always call parent setup when overriding |
| Static mocks without cleanup | Use try-with-resources for static mocks |

## Integration with Existing Tests

### Migrating from Basic Mocks

```java
// Old approach - basic mock status
@Test
public void oldStyleTest() {
    MockStatus.setTimesToRun(10);
    
    for (int i = 0; i < 10; i++) {
        ActionResult result = actions.find(element);
        assertTrue(result.isSuccess());
    }
}

// New approach - scenario-based with verification
@Test 
public void newStyleTest() {
    MockScenarioConfig scenario = MockScenarioConfig.builder()
        .scenarioName("repeated_find_test")
        .build();
        
    mockScenarioManager.activateScenario(scenario);
    
    ActionPatternVerification verification = mockBehaviorVerifier
        .expectActionPattern("find_pattern")
        .action(ActionType.FIND)
        .expectedSuccessRate(1.0)
        .verify();
        
    for (int i = 0; i < 10; i++) {
        ActionResult result = actions.find(element);
        assertTrue(result.isSuccess());
    }
    
    assertEquals(VerificationResult.PASSED, verification.getResult());
}
```

### Test Suite Organization

```java
@TestConfiguration
public class MockTestConfig {
    
    @Bean
    public MockScenarioManager scenarioManager() {
        return new MockScenarioManager();
    }
    
    @Bean 
    public TestDataBuilder testDataBuilder() {
        return new TestDataBuilder();
    }
    
    // Pre-configured scenarios for reuse
    @Bean
    public Map<String, MockScenarioConfig> commonScenarios() {
        Map<String, MockScenarioConfig> scenarios = new HashMap<>();
        
        scenarios.put("network_issues", MockScenarioConfig.builder()
            .scenarioName("network_issues")
            .actionFailurePattern(ActionType.FIND, 
                FailurePattern.builder()
                    .baseProbability(0.2)
                    .maxConsecutiveFailures(2)
                    .build())
            .build());
            
        scenarios.put("slow_system", MockScenarioConfig.builder()
            .scenarioName("slow_system") 
            .temporalCondition("delay", 
                TemporalConditions.builder()
                    .baseDelay(Duration.ofMillis(200))
                    .build())
            .build());
            
        return scenarios;
    }
}
```

## Best Practices

### Scenario Design

1. **Keep scenarios focused** - Each scenario should test one specific condition
2. **Use meaningful names** - Scenario names should clearly indicate what they test
3. **Set appropriate timeouts** - Prevent runaway tests with reasonable duration limits
4. **Document expected behaviors** - Include descriptions of what each scenario validates

### Failure Pattern Design

1. **Model realistic failures** - Base patterns on actual system behavior
2. **Use progressive failures** - Start with low probability and increase over time
3. **Include recovery periods** - Allow systems to recover after failure sequences
4. **Set failure limits** - Prevent infinite failure loops with max consecutive failures

### Verification Strategy

1. **Verify behavior, not just results** - Check timing, sequences, and patterns
2. **Use multiple verification types** - Combine state transitions with action patterns
3. **Include negative tests** - Verify that unexpected behaviors are caught
4. **Clean up after tests** - Reset verifiers and scenarios between tests

### Performance Considerations

1. **Use sampling for high-frequency actions** - Reduce verification overhead
2. **Batch related verifications** - Group similar checks together
3. **Clean up resources** - Properly dispose of mock contexts and verifiers
4. **Monitor test execution time** - Enhanced mocking should not significantly slow tests

## Configuration Reference

### MockScenarioConfig Properties

| Property | Type | Description |
|----------|------|-------------|
| `scenarioName` | String | Unique identifier for the scenario |
| `description` | String | Human-readable description |
| `stateAppearanceProbabilities` | `Map<String, Double>` | Per-state appearance rates (0.0-1.0) |
| `actionFailurePatterns` | `Map<Action, FailurePattern>` | Failure patterns by action type |
| `temporalConditions` | `Map<String, TemporalConditions>` | Time-based conditions |
| `maxDuration` | Duration | Maximum scenario runtime |
| `cascadingFailures` | boolean | Enable failure cascading |

### FailurePattern Properties

| Property | Type | Description |
|----------|------|-------------|
| `baseProbability` | double | Base failure rate (0.0-1.0) |
| `probabilityDecay` | double | Reduction per failure occurrence |
| `maxConsecutiveFailures` | int | Max failures before forced success |
| `cascading` | boolean | Whether failures increase probability |
| `recoveryDelay` | Duration | Recovery time after failures |
| `exceptionType` | `Class<Exception>` | Type of exception to throw |

### Verification Configuration

| Property | Type | Description |
|----------|------|-------------|
| `maxTotalTime` | Duration | Overall sequence timeout |
| `minDuration` | Duration | Minimum step duration |
| `maxDuration` | Duration | Maximum step duration |
| `optional` | boolean | Whether step is optional |
| `verificationWindow` | Duration | Time window for pattern verification |

This enhanced mock testing system provides comprehensive tools for creating realistic, maintainable, and thorough test scenarios that closely mirror production conditions while enabling rapid iteration and debugging.