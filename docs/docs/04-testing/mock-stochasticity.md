# Mock Stochasticity in Brobot

## Overview

Brobot's mock mode includes a sophisticated stochasticity system that simulates the unpredictable nature of real-world GUI environments. This feature makes mock tests more realistic and helps catch edge cases that might occur in production.

## The mockFindStochasticModifier Field

Each `State` in Brobot has a `mockFindStochasticModifier` field that represents an additional probability factor for whether the state's images will be found during mock runs.

### Purpose

The `mockFindStochasticModifier` field simulates environmental uncertainties that can affect GUI automation:

- **Network failures**: Connection issues that prevent pages from loading
- **Unexpected overlays**: Update windows, notifications, or popups that cover target elements
- **Rendering issues**: Elements with transparent backgrounds or poor contrast that make detection unreliable
- **Timing variations**: Elements that appear/disappear at unpredictable times
- **System interruptions**: OS-level dialogs, screen savers, or other interruptions

### How It Works

When a `StateImage` is searched for in a mock run, the probability of finding it depends on two factors:

1. **ActionHistory Analysis**: The mock framework examines the `ActionHistory` stored in the `StateImage`, which records historical behavior of actions on that image. It compares:
   - Past action outcomes (success/failure)
   - Environmental characteristics when the action was performed
   - Current environmental characteristics

   The framework essentially asks: "What happened in the past when this action was performed on this image in similar environments?"

2. **mockFindStochasticModifier Factor**: This is an additional probability modifier that can deny the existence of the image, regardless of historical data. It acts as a random failure injection mechanism.

### Usage Example

```java
// In State.Builder
State loginState = State.builder()
    .withName("LoginScreen")
    .setBaseMockFindStochasticModifier(95)  // 95% chance of being available
    .build();

// During mock execution
// If mockFindStochasticModifier = 95, there's a 5% chance
// the state's images won't be found, simulating environmental issues
```

## Important Implementation Notes

### When NOT to Modify mockFindStochasticModifier

The `mockFindStochasticModifier` field should **NOT** be modified during normal state transitions:

❌ **Don't do this:**
```java
// In StateMemory.addActiveState()
state.setMockFindStochasticModifier(100);  // WRONG - Don't force availability
```

❌ **Don't do this either:**
```java
// In StateMemory.removeInactiveState()
state.setMockFindStochasticModifier(0);  // WRONG - This breaks future mock runs!
```

### Why These Are Bugs

1. **Setting to 100 on activation**: The state becoming active doesn't mean it will always be findable. Environmental issues can still occur.

2. **Setting to 0 on deactivation**: This is particularly harmful because it means the state's images will **never** be found again in mock runs, breaking the mock's ability to navigate back to that state.

### Correct Approach

The `mockFindStochasticModifier` should be:
- Set during **state definition** based on the expected reliability of that state
- Potentially adjusted by **mock configuration** for different test scenarios
- **Never modified** by state activation/deactivation logic

## Configuration

### Setting Base Values

Set the base mock find stochastic modifier when defining states:

```java
@State
public class MyStates {

    @State.Configure
    public void configure(State.Builder builder) {
        builder.setBaseMockFindStochasticModifier(90);  // 90% availability
    }
}
```

### Common Values

- **100**: Perfect availability (unrealistic, not recommended)
- **95-99**: Highly stable states (main navigation, home screens)
- **85-94**: Normal states (typical application screens)
- **70-84**: Less stable states (dynamic content, Ajax-heavy pages)
- **50-69**: Unreliable states (states with frequent overlays or interruptions)

## Integration with ActionHistory

The stochasticity system works in conjunction with `ActionHistory`:

1. **ActionHistory** provides historical context: "This click action succeeded 8 out of 10 times on this image"
2. **mockFindStochasticModifier** adds environmental randomness: "But there's also a 10% chance the entire state is unavailable"

Together, they create a realistic mock that can:
- Reproduce historical patterns
- Inject random failures
- Test automation robustness
- Validate error handling

## Best Practices

1. **Set realistic values**: Don't use 100% availability unless testing a specific scenario
2. **Document your choices**: Explain why certain states have lower availability
3. **Use different profiles**: Create test profiles with different stochasticity levels
4. **Monitor patterns**: If a state consistently fails in mocks, it might indicate a real reliability issue
5. **Don't modify at runtime**: Let the mock framework control the stochasticity

## Testing Strategies

### Reliability Testing

```java
// Test with reduced availability to stress-test error handling
@Test
public void testWithUnreliableStates() {
    // Configure all states to have 70% availability
    mockConfig.setGlobalMockFindStochasticModifier(70);

    // Run automation and verify it handles failures gracefully
    automationResult = automation.run();

    assertTrue(automationResult.hasRecoveryAttempts());
    assertTrue(automationResult.isEventuallySuccessful());
}
```

### Edge Case Discovery

```java
// Use varying stochasticity to discover edge cases
@ParameterizedTest
@ValueSource(ints = {50, 70, 90, 95})
public void testWithVaryingReliability(int availability) {
    state.setMockFindStochasticModifier(availability);
    // Test behavior under different reliability conditions
}
```

## Conclusion

The `mockFindStochasticModifier` field is a powerful tool for creating realistic mock tests that expose potential issues before they occur in production. By simulating environmental uncertainties, it helps ensure your automation is robust and can handle the unpredictable nature of real-world GUI environments.

Remember: This field is for **mock mode simulation only** and should not be confused with state activation logic. It represents the inherent unreliability of GUI environments, not the logical state of the application.