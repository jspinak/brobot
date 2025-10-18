---
sidebar_position: 8
title: Test Utilities
description: Common utilities and helpers for testing Brobot applications
---

# Test Utilities

Brobot provides comprehensive test utilities to help you write robust and maintainable tests. The framework includes base classes, utility methods, and centralized mock mode management.

## BrobotTestBase - Base Test Class

All Brobot tests should extend `BrobotTestBase` to ensure proper test configuration:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MyBrobotTest extends BrobotTestBase {

    // Note: BrobotProperties is automatically available from BrobotTestBase parent
    
    @Test
    public void testMyFeature() {
        // Mock mode is automatically enabled
        // Test will work in headless environments
    }
    
    @Test
    public void testWithRealMode() {
        // Temporarily disable mock mode
        disableMockMode();
        try {
            // Test with real screen capture
        } finally {
            // Re-enable mock mode
            MockModeManager.setMockMode(true);
        }
    }
}
```

### What BrobotTestBase Provides:
- **Automatic mock mode activation** via `MockModeManager`
- **Headless environment compatibility**
- **Fast mock timings** (0.01-0.04 seconds for operations)
- **CI/CD pipeline support**
- **Prevention of AWTException and HeadlessException errors**

## Test Utility Classes

### BrobotTestUtils - Common Test Helpers

Provides factory methods for creating test data:

```java
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.reports.ActionResult;
import java.util.List;

// Create test objects
State testState = BrobotTestUtils.createTestState("MyState");
StateImage testImage = BrobotTestUtils.createTestStateImage("TestImage");
Match testMatch = BrobotTestUtils.createTestMatch(100, 100, 50, 50, 0.95);

// Create multiple test matches
List<Match> matches = BrobotTestUtils.createTestMatches(5);

// Create action results
ActionResult success = BrobotTestUtils.createSuccessfulResult(3);
ActionResult failure = BrobotTestUtils.createFailedResult();
```

### MatTestUtils - OpenCV Mat Testing Utilities

Comprehensive utilities for safe OpenCV Mat operations in tests:

```java
import io.github.jspinak.brobot.test.utils.MatTestUtils;
import org.bytedeco.opencv.opencv_core.Mat;

// Create safe, validated Mats
Mat colorMat = MatTestUtils.createColorMat(100, 100, 255, 0, 0); // Red
Mat grayMat = MatTestUtils.createGrayMat(100, 100, 128);

// Create test patterns
Mat checkerboard = MatTestUtils.createCheckerboardMat(200, 200, 25);
Mat gradient = MatTestUtils.createGradientMat(100, 100, true);
Mat circle = MatTestUtils.createShapeMat(100, 100, 1);

// Validate Mats before operations
MatTestUtils.validateMat(mat, "before processing");

// Safe cleanup
MatTestUtils.safeReleaseAll(mat1, mat2, mat3);
```

**Key Features:**
- Prevents JVM crashes from invalid Mat operations
- Provides validation and safe cleanup
- Includes pattern generation for testing
- Motion detection test helpers

See [Mat Testing Utilities](mat-testing-utilities.md) for complete documentation.

## MockModeManager - Centralized Mock Control

The `MockModeManager` provides a single source of truth for mock mode configuration:

```java
import io.github.jspinak.brobot.config.mock.MockModeManager;

// Enable mock mode globally
MockModeManager.setMockMode(true);

// Check current mock mode
boolean isMock = brobotProperties.getCore().isMock();

// Debug mock mode state across all components
MockModeManager.logMockModeState();
```

## BrobotTestUtils - Test Data Creation

The `BrobotTestUtils` class provides utilities for creating test data:

### Overview

Located at `io.github.jspinak.brobot.test.utils.BrobotTestUtils`, this utility class provides:

- Factory methods for creating test objects
- Random data generators for realistic test scenarios
- Assertion helpers for approximate comparisons
- Environment detection utilities
- Timing and synchronization helpers

## Factory Methods

### Creating Test States

```java
// Create a simple test state
State testState = BrobotTestUtils.createTestState("LoginPage");

// Create with custom properties
State.Builder builder = new State.Builder("HomePage");
builder.setBlocking(true);
State customState = builder.build();
```

### Creating Test StateImages

```java
// Create a test StateImage
StateImage testImage = BrobotTestUtils.createTestStateImage("submitButton");

// The created StateImage has default similarity settings
```

### Creating Test Matches

```java
// Create a match at specific location
Match match = BrobotTestUtils.createTestMatch(
    100,  // x
    200,  // y
    50,   // width
    50,   // height
    0.95  // score
);

// Create a random match for testing
Match randomMatch = BrobotTestUtils.createRandomMatch();

// Create multiple matches
List<Match> matches = BrobotTestUtils.createTestMatches(5);
```

## ActionResult Creation

### Successful Results

```java
// Create a successful result with 3 matches
ActionResult success = BrobotTestUtils.createSuccessfulResult(3);

// Verify the result
assertTrue(success.isSuccess());
assertEquals(3, success.getMatches().size());
```

### Failed Results

```java
// Create a failed result
ActionResult failed = BrobotTestUtils.createFailedResult();

// Verify the result
assertFalse(failed.isSuccess());
assertTrue(failed.getMatches().isEmpty());
```

## Location and Region Utilities

### Working with Locations

```java
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;

// Create a specific location
Location loc = BrobotTestUtils.createTestLocation(100, 200);

// Create a random location within screen bounds
Location randomLoc = BrobotTestUtils.createRandomLocation();

// Compare locations with tolerance
Location loc1 = new Location(100, 100);
Location loc2 = new Location(102, 98);
boolean isClose = BrobotTestUtils.areLocationsApproximatelyEqual(
    loc1, loc2, 5  // tolerance in pixels
);
```

### Working with Regions

```java
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;

// Create a specific region
Region region = BrobotTestUtils.createTestRegion(
    10,   // x
    20,   // y
    100,  // width
    50    // height
);

// Create a random region
Region randomRegion = BrobotTestUtils.createRandomRegion();

// Compare regions with tolerance
boolean isClose = BrobotTestUtils.areRegionsApproximatelyEqual(
    region1, region2, 3  // tolerance in pixels
);
```

## Environment Detection

### CI/CD Detection

```java
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@Test
void testRequiringDisplay() {
    if (BrobotTestUtils.isRunningInCI()) {
        // Skip or use mock mode in CI environment
        assumeFalse(true, "Skipping in CI environment");
    }

    // Test requiring actual display
    // ...
}
```

### Headless Environment Detection

```java
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import org.junit.jupiter.api.Test;

@Test
void testScreenCapture() {
    if (BrobotTestUtils.isHeadless()) {
        // Use mock mode for headless environments
        // Mock mode is now configured via application.properties:
        // brobot.mock=true
    }

    // Proceed with test
    // ...
}
```

## Timing Utilities

### Sleep Functions

```java
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Short sleep (100ms)
BrobotTestUtils.shortSleep();

// Custom sleep duration
BrobotTestUtils.sleep(500);  // 500ms

// Use in test scenarios
@Test
void testWithTiming() {
    action.click(button);
    BrobotTestUtils.shortSleep();  // Wait for UI to update
    assertTrue(isDialogVisible());
}
```

## Test Name Generation

```java
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;

// Generate unique test names
String testName = BrobotTestUtils.generateTestName("TestRun");
// Result: "TestRun_1735000123456_789"

// Useful for creating unique test resources
String imageName = BrobotTestUtils.generateTestName("testImage");
StateImage image = new StateImage.Builder()
    .setName(imageName)
    .build();
```

## Best Practices

### 1. Use Factory Methods for Consistency

```java
@Test
void testStateTransition() {
    // Good: Use factory methods
    State fromState = BrobotTestUtils.createTestState("LoginPage");
    State toState = BrobotTestUtils.createTestState("HomePage");
    
    // Test transition logic
    // ...
}
```

### 2. Leverage Random Data for Robustness

```java
@Test
void testMatchProcessing() {
    // Test with various random matches
    for (int i = 0; i < 10; i++) {
        Match match = BrobotTestUtils.createRandomMatch();
        // Process match
        assertTrue(processMatch(match));
    }
}
```

### 3. Use Environment Detection for Conditional Testing

```java
@BeforeEach
void setup() {
    if (BrobotTestUtils.isHeadless() || BrobotTestUtils.isRunningInCI()) {
        // Enable mock mode for headless/CI environments
        // Mock mode is now configured via application.properties:
        // brobot.mock=true
    }
}
```

### 4. Approximate Comparisons for UI Testing

```java
@Test
void testMouseMovement() {
    Location expected = new Location(100, 100);
    action.move(expected);
    
    Location actual = Mouse.at();
    
    // Allow for small variations in mouse position
    assertTrue(BrobotTestUtils.areLocationsApproximatelyEqual(
        expected, actual, 2
    ));
}
```

## Integration with Test Frameworks

### JUnit 5 Integration

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.reports.ActionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MyBrobotTest extends BrobotTestBase {

    @BeforeEach
    void setup() {
        // Use utilities for test setup
        if (BrobotTestUtils.isHeadless()) {
            // Mock mode is now configured via application.properties:
            // brobot.mock=true
        }
    }

    @Test
    void testWithUtilities() {
        State state = BrobotTestUtils.createTestState("TestState");
        ActionResult result = BrobotTestUtils.createSuccessfulResult(1);

        // Your test logic
        assertNotNull(state);
        assertTrue(result.isSuccess());
    }
}
```

### Parameterized Tests

```java
import io.github.jspinak.brobot.test.utils.BrobotTestUtils;
import io.github.jspinak.brobot.reports.ActionResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ParameterizedTest
@ValueSource(ints = {1, 3, 5, 10})
void testMultipleMatches(int matchCount) {
    ActionResult result = BrobotTestUtils.createSuccessfulResult(matchCount);

    assertEquals(matchCount, result.getMatches().size());
    assertTrue(result.isSuccess());
}
```

## Common Testing Patterns

### Mock Data Creation

```java
private ActionResult mockFindAction() {
    return BrobotTestUtils.createSuccessfulResult(1);
}

private List<Match> mockMultipleMatches() {
    return BrobotTestUtils.createTestMatches(5);
}
```

### Test Data Builders

```java
class TestDataBuilder {
    
    public State buildLoginState() {
        State state = BrobotTestUtils.createTestState("Login");
        // Add custom configuration
        return state;
    }
    
    public List<StateImage> buildTestImages() {
        return Arrays.asList(
            BrobotTestUtils.createTestStateImage("button1"),
            BrobotTestUtils.createTestStateImage("button2"),
            BrobotTestUtils.createTestStateImage("button3")
        );
    }
}
```

## Notes

- All random generators use reasonable bounds (e.g., 1920x1080 screen)
- Factory methods create objects with sensible defaults
- Utilities are designed to work in both mock and real modes
- Thread-safe for use in parallel test execution

## Related Documentation

### Testing Guides
- **[Testing Introduction](./testing-intro.md)** - Overview of Brobot testing approaches
- **[Unit Testing Guide](./unit-testing.md)** - Unit testing patterns with BrobotTestBase
- **[Integration Testing](./integration-testing.md)** - Integration test patterns with Spring
- **[Profile-Based Testing](./profile-based-testing.md)** - Profile-specific test configurations
- **[Testing Strategy](./testing-strategy.md)** - Overall testing strategy and patterns

### Mock Mode Documentation
- **[Mock Mode Guide](./mock-mode-guide.md)** - Comprehensive guide to mock mode
- **[Mock Mode Manager](./mock-mode-manager.md)** - Centralized mock mode management
- **[Mock Mode Migration](./mock-mode-migration.md)** - Migrating to MockModeManager
- **[Mock Stochasticity](./mock-stochasticity.md)** - Probabilistic mock behavior
- **[ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md)** - Creating mock data
- **[Action Recording](./action-recording.md)** - Recording actions for mocks

### Mat Testing Documentation
- **[Mat Testing Utilities](./mat-testing-utilities.md)** - Complete MatTestUtils documentation
- **[Debugging Pattern Matching](./debugging-pattern-matching.md)** - Troubleshooting pattern matching

### Configuration Documentation
- **[BrobotProperties Usage](../03-core-library/configuration/brobot-properties-usage.md)** - Complete configuration guide
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - All available properties
- **[Headless Configuration](../03-core-library/configuration/headless-configuration.md)** - Headless mode setup

### Advanced Testing
- **[Enhanced Mock Testing System](./advanced/enhanced-mocking.md)** - Advanced mock scenarios
- **[CI/CD Testing](./advanced/ci-cd-testing.md)** - Mock mode in CI/CD pipelines
<!-- Proposal documentation not included in /docs/ directory -->