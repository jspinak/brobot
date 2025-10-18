---
sidebar_position: 10
title: 'Enhanced Mock Testing System'
---

# Enhanced Mock Testing System

:::info For Framework Developers
This section covers **advanced mocking features** for developers extending the Brobot framework or building complex testing scenarios.

For standard automation testing, see the [main Testing documentation](/docs/04-testing/testing-intro).
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
For Spring-based tests, see the [Test Logging Architecture (Proposed)](/docs/proposals/test-logging-architecture) - a proposed design for factory-based configuration. Note: This is a design proposal, not currently implemented.
:::

```java
import io.github.jspinak.brobot.config.MockModeManager;
import io.github.jspinak.brobot.config.BrobotProperties;
import org.springframework.beans.factory.annotation.Autowired;

public class MyMockTest {
    // BrobotProperties must be injected as a dependency
    @Autowired
    private BrobotProperties brobotProperties;

    public void configureMockMode() {
        // Enable mock mode across all components
        MockModeManager.setMockMode(true);

        // Check if mock mode is active
        if (brobotProperties.getCore().isMock()) {
            // Execute mock-specific logic
        }

        // Debug mock mode state
        MockModeManager.logMockModeState();
    }
}
```

This ensures consistency across:
- System properties
- ExecutionEnvironment
- FrameworkSettings
- All other mock-aware components

## Mock Scenario Configuration

### Basic Scenario Setup

:::warning MockScenarioManager Not in Main Library
The examples in this section reference `mockScenarioManager`, which is **not included in the main Brobot library**. It exists only as an example implementation in `/examples/03-core-library/testing/enhanced-mocking/`.

To use these features, you'll need to:
1. Implement your own MockScenarioManager based on the example, or
2. Use MockScenarioConfig directly with `scenario.activate()` method
3. See `/examples/` directory for reference implementation
:::

All test classes should extend `BrobotTestBase` for automatic mock mode configuration:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.scenario.MockScenarioConfig;
import io.github.jspinak.brobot.action.ActionResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyScenarioTest extends BrobotTestBase {

    @Test
    public void testLoginUnderNetworkIssues() {
        MockScenarioConfig scenario = MockScenarioConfig.builder()
            .scenarioName("login_network_issues")
            .description("Simulate intermittent network connectivity during login")
            .stateAppearanceProbability("LOGIN_STATE", 0.8)  // 80% appear rate
            .stateAppearanceProbability("DASHBOARD", 0.9)    // 90% appear rate
            .build();

        // Note: MockScenarioManager.activateScenario() is from examples, not main library
        // Use scenario.activate() or implement your own manager
        // mockScenarioManager.activateScenario(scenario);

        // Your test logic here
        ActionResult result = actions.find(loginButton);

        assertTrue(result.isSuccess());
    }
}
```

### Advanced Failure Patterns

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.scenario.FailurePattern;
import io.github.jspinak.brobot.tools.testing.mock.scenario.MockScenarioConfig;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.options.PatternFindOptions;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class CascadingFailuresTest extends BrobotTestBase {

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
}
```

### Temporal Conditions

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.scenario.TemporalConditions;
import io.github.jspinak.brobot.tools.testing.mock.scenario.MockScenarioConfig;
import io.github.jspinak.brobot.action.ActionResult;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemporalConditionsTest extends BrobotTestBase {

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
}
```

## Behavioral Verification

### State Transition Verification

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.verification.StateTransitionVerification;
import io.github.jspinak.brobot.tools.testing.mock.verification.VerificationResult;
import io.github.jspinak.brobot.tools.testing.mock.verification.MockBehaviorVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateTransitionTest extends BrobotTestBase {

    @Autowired
    private MockBehaviorVerifier mockBehaviorVerifier;

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
}
```

### Action Pattern Verification

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.verification.ActionPatternVerification;
import io.github.jspinak.brobot.tools.testing.mock.verification.VerificationResult;
import io.github.jspinak.brobot.tools.testing.mock.verification.MockBehaviorVerifier;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionPatternTest extends BrobotTestBase {

    @Autowired
    private MockBehaviorVerifier mockBehaviorVerifier;

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
}
```

## Structured Test Data Builder

### Creating Test Scenarios

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.data.TestDataBuilder;
import io.github.jspinak.brobot.tools.testing.data.TestScenario;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitiveStructs.region.Region;
import io.github.jspinak.brobot.action.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDataBuilderExample extends BrobotTestBase {

    @Autowired
    private TestDataBuilder testDataBuilder;

    @Test
    public void testWithStructuredData() {
        TestScenario loginScenario = testDataBuilder
        .scenario("comprehensive_login_test")
        .withDescription("Complete login flow with variations")
        .withVersion("1.1.0")
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
}
```

### Test Variations

:::caution API Note
The TestDataBuilder variations API is partially complete. StateImage transformation methods shown below are still in development. See TestDataBuilder.java TODOs for current status.
:::

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.data.TestDataBuilder;
import io.github.jspinak.brobot.tools.testing.data.TestScenario;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.action.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestVariationsExample extends BrobotTestBase {

    @Autowired
    private TestDataBuilder testDataBuilder;

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
    // Note: StateImage similarity checking API may vary - check actual implementation
    // assertTrue("Mobile button has lower similarity threshold",
    //            mobileLoginButton.getSimilarity() < 0.8);

    ActionResult result = actions.find(mobileLoginButton);
    assertTrue(result.isSuccess());
    }
}
```

### Custom Variations

:::warning Under Development
The transformation API shown below references StateImage methods (`.toBuilder()`, `.getSimilarity()`) that are still being implemented. This is an aspirational API design.  Check the actual TestDataBuilder and StateImage classes for current capabilities.
:::

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.data.TestDataBuilder;
import io.github.jspinak.brobot.tools.testing.data.TestScenario;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.action.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomVariationsExample extends BrobotTestBase {

    @Autowired
    private TestDataBuilder testDataBuilder;

    @Test
    public void testHighContrastVariation() {
        TestScenario scenario = testDataBuilder
        .scenario("high_contrast_test")
        .withStateImage("button", "normal_button.png")
        .withVariation("high_contrast")
            .withDescription("High contrast accessibility mode")
            .withTransformation("reduce_similarity", (name, obj) -> {
                // Note: This transformation API is under development
                if (obj instanceof StateImage) {
                    // StateImage.toBuilder() not yet implemented
                    // return ((StateImage) obj).toBuilder()
                    //     .similarity(Math.max(0.6, ((StateImage) obj).getSimilarity() - 0.15))
                    //     .build();
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

1. **Mock Mode Activation** - Sets `// Mock mode is now configured via application.properties:
// brobot.core.mock=true` to prevent SikuliX headless exceptions
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
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;

public class CustomTest extends BrobotTestBase {

    @Override
    @BeforeEach
    public void setupTest() {
        super.setupTest(); // Important: call parent setup first

        // Add your custom setup
        // Configure custom mock timings (example - adjust as needed)
        System.setProperty("brobot.mock.time.find.first", "0.05");
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
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.grid.MockGridConfig;
import io.github.jspinak.brobot.datatypes.primitiveStructs.region.Region;
import io.github.jspinak.brobot.datatypes.primitiveStructs.location.Location;
import io.github.jspinak.brobot.tools.region.RegionUtils;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridOperationsTest extends BrobotTestBase {

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
}
```

#### MockGridConfig Features

- **Configurable Dimensions**: Set custom grid sizes with `setDefaultGrid(rows, cols)`
- **Thread-Safe**: Safe for use in parallel test execution
- **Fallback Calculations**: When SikuliX is unavailable, uses native Brobot calculations
- **Consistent Behavior**: Same API in mock and real modes

### MockFind Intelligent Fallback Behavior

The `MockFind` class provides intelligent fallback behavior to ensure robust testing even when match history is not configured:

#### Automatic Match Generation

When a pattern has no configured match history, MockFind automatically generates default successful matches:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.action.MockFind;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.Pattern;
import io.github.jspinak.brobot.datatypes.primitiveStructs.match.Match;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockFindFallbackTest extends BrobotTestBase {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testWithoutMatchHistory() {
        // Create pattern without any match history
        Pattern pattern = new Pattern.Builder()
            .setName("TestPattern")
            .build();

        // MockFind will still return a match in mock mode
        MockFind mockFind = context.getBean(MockFind.class);
        List<Match> matches = mockFind.getMatches(pattern);

        assertFalse(matches.isEmpty(), "MockFind provides default match");
        assertEquals("TestPattern", matches.get(0).getName());
    }
}
```

#### Fallback Behavior Rules

MockFind follows these rules for consistent testing behavior:

1. **Check for existing history**: First checks if the pattern has any match history configured
2. **Generate default match if empty**: If no history exists, generates a default successful match with:
   - Region: 100, 100, 50, 50 (reasonable default location and size)
   - Similarity score: 0.95 (high confidence match)
   - Name: Uses pattern name if available, otherwise "MockMatch"
3. **Respect configured history**: If history exists but has no matches for current state, returns empty list
4. **State-aware matching**: When history exists, filters by active states

#### Example: Testing Without Extensive Setup

```java
@Test
public void testQuickMockSetup() {
    // No need to configure match history for simple tests
    StateImage quickTestImage = new StateImage.Builder()
        .addPattern(new Pattern.Builder()
            .setName("QuickTestPattern")
            .build())
        .build();
    
    ObjectCollection collection = new ObjectCollection.Builder()
        .withImages(quickTestImage)
        .build();
    
    // MockFind will provide default matches
    ActionResult result = action.perform(new PatternFindOptions.Builder().build(), collection);
    
    assertTrue(result.isSuccess(), "Mock mode works without match history setup");
}
```

#### When to Use Match History vs Fallback

- **Use configured match history** when:
  - Testing specific match locations or patterns
  - Simulating test scenarios with varying success rates
  - Testing state-specific behaviors
  
- **Rely on fallback behavior** when:
  - Running quick smoke tests
  - Testing flow logic rather than match specifics
  - Prototyping new test scenarios
  - Testing in CI/CD without complex setup

#### Configuring Match History When Needed

For tests requiring specific match behavior, configure match history explicitly:

```java
@Test
public void testWithSpecificMatchHistory() {
    Pattern pattern = new Pattern.Builder()
        .setName("SpecificPattern")
        .build();
    
    // Add specific match history
    ActionRecord successfulFind = new ActionRecord.Builder()
        .setActionConfig(new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build())
        .setActionSuccess(true)
        .setState("TestState")
        .setMatchList(Arrays.asList(
            new Match.Builder()
                .setRegion(new Region(200, 150, 100, 50))  // Specific location
                .setSimScore(0.98)  // High confidence
                .build()))
        .build();
    
    pattern.getMatchHistory().addSnapshot(successfulFind);
    
    // Test will use configured history instead of fallback
    MockFind mockFind = context.getBean(MockFind.class);
    List<Match> matches = mockFind.getMatches(pattern);
    
    assertEquals(200, matches.get(0).getRegion().x());  // Uses configured location
}
```

This intelligent fallback behavior ensures that:
- Tests can run successfully without extensive mock setup
- Quick prototyping and testing is possible
- Tests remain maintainable with minimal configuration
- Configured history is always respected when present

### Mock Scene and Color Analysis

The `MockSceneBuilder` provides comprehensive builders for creating test data for color analysis and scene processing:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.builders.MockSceneBuilder;
import io.github.jspinak.brobot.imageUtils.analysis.Scene;
import io.github.jspinak.brobot.imageUtils.analysis.SceneAnalysis;
import io.github.jspinak.brobot.imageUtils.analysis.PixelProfiles;
import io.github.jspinak.brobot.colorUtils.clustering.ColorCluster;
import io.github.jspinak.brobot.colorUtils.clustering.ColorClassifier;
import org.opencv.core.Mat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockSceneBuilderTest extends BrobotTestBase {

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
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.builders.MockSceneBuilder;
import io.github.jspinak.brobot.imageUtils.analysis.SceneAnalysis;
import io.github.jspinak.brobot.imageUtils.analysis.Scene;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComplexSceneTest extends BrobotTestBase {

    @Test
    public void testComplexSceneAnalysis() {
        // Create custom scene first
        Scene customScene = MockSceneBuilder.createMockScene();

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
}
```

### Performance Characteristics

Mock mode operations are significantly faster than real operations:

:::info Performance Note
The timing values below represent **configured mock delays** (0.01-0.04s), not measured benchmarks. Real mode timings are estimates based on typical SikuliX/OpenCV operations. Actual performance varies by system configuration and operation complexity.
:::

| Operation | Mock Mode (Configured) | Real Mode (Estimated) | Expected Speedup |
|-----------|------------------------|----------------------|------------------|
| Grid operations | ~0.01s | 0.5s | ~50x |
| Color analysis | ~0.02s | 1-2s | ~50-100x |
| Pattern matching | ~0.01s | 0.5-2s | ~50-200x |
| State transitions | ~0.01s | 0.2-1s | ~20-100x |

### Jackson Serialization Support

When creating test objects that need serialization, ensure proper Jackson annotations:

```java
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

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

## Test Suite Organization

```java
import io.github.jspinak.brobot.tools.testing.data.TestDataBuilder;
import io.github.jspinak.brobot.tools.testing.mock.scenario.MockScenarioConfig;
import io.github.jspinak.brobot.tools.testing.mock.scenario.FailurePattern;
import io.github.jspinak.brobot.tools.testing.mock.scenario.TemporalConditions;
import io.github.jspinak.brobot.action.ActionType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class MockTestConfig {

    // Note: MockScenarioManager is from examples, not main library
    // Implement your own or use MockScenarioConfig directly

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

---

## Related Documentation

### Core Testing Guides
- **[Testing Introduction](/docs/04-testing/testing-intro)** - Overview of Brobot testing approaches
- **[Unit Testing Guide](/docs/04-testing/unit-testing.md)** - Writing unit tests with Brobot
- **[Integration Testing Guide](/docs/04-testing/integration-testing.md)** - Integration test patterns and practices
- **[Mock Mode Guide](/docs/04-testing/mock-mode-guide.md)** - Fundamentals of mock mode configuration
- **[Test Utilities](/docs/04-testing/test-utilities.md)** - BrobotTestBase, BrobotTestUtils, and testing helpers
- **[Mat Testing Utilities](/docs/04-testing/mat-testing-utilities.md)** - OpenCV Mat testing utilities for color analysis
- **[CI/CD Testing](./ci-cd-testing.md)** - CI/CD testing best practices

### ActionConfig and Actions
- **[Action Config Overview](/docs/03-core-library/action-config/01-overview.md)** - ActionConfig class hierarchy
- **[ActionResult Components](/docs/03-core-library/action-config/17-actionresult-components.md)** - ActionResult reference
- **[PatternFindOptions](/docs/03-core-library/action-config/02-patternfindoptions.md)** - Find configuration

### Testing Strategy
- **[Testing Strategy](/docs/04-testing/testing-strategy.md)** - Overall testing philosophy and approach
- **[Test Logging Architecture (Proposed)](/docs/proposals/test-logging-architecture.md)** - Proposed factory-based test configuration (not implemented)

---

This enhanced mock testing system provides comprehensive tools for creating realistic, maintainable, and thorough test scenarios that closely mirror production conditions while enabling rapid iteration and debugging.