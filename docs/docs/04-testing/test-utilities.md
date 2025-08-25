---
sidebar_position: 8
title: Test Utilities
description: Common utilities and helpers for testing Brobot applications
---

# Test Utilities

The `BrobotTestUtils` class provides a comprehensive set of utilities for writing tests in Brobot applications. These utilities help reduce boilerplate code and ensure consistent test data creation across your test suite.

## Overview

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
@Test
void testScreenCapture() {
    if (BrobotTestUtils.isHeadless()) {
        // Use mock mode for headless environments
        FrameworkSettings.mock = true;
    }
    
    // Proceed with test
    // ...
}
```

## Timing Utilities

### Sleep Functions

```java
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
        FrameworkSettings.mock = true;
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
@ExtendWith(MockitoExtension.class)
class MyBrobotTest extends BrobotTestBase {
    
    @BeforeEach
    void setup() {
        // Use utilities for test setup
        if (BrobotTestUtils.isHeadless()) {
            FrameworkSettings.mock = true;
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

## See Also

- [Unit Testing Guide](unit-testing.md)
- [Integration Testing](integration-testing.md)
- [Mock Mode Guide](mock-mode-guide.md)
- [BrobotTestBase Class](enhanced-mocking.md)