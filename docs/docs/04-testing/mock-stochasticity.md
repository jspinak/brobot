---
sidebar_position: 9
title: 'Mock Stochasticity'
description: 'Understanding mock stochasticity and ActionHistory in Brobot testing'
---

# Mock Stochasticity in Brobot

> ⚠️ **IMPORTANT NOTE**: This document describes the `mockFindStochasticModifier` field that exists in the State model but is **not currently used by the mock framework**. Mock behavior is currently determined by [ActionHistory](./actionhistory-mock-snapshots.md) alone. This field is reserved for future enhancements.

## Overview

Brobot's [mock mode](./mock-mode-guide.md) uses [ActionHistory](./actionhistory-mock-snapshots.md) to simulate realistic behavior by replaying historical action outcomes. The `mockFindStochasticModifier` field exists as a probability modifier but is not currently integrated with the mock framework.

For information on how mock mode actually works, see:
- [Mock Mode Guide](./mock-mode-guide.md) - Comprehensive mock mode documentation
- [ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md) - How ActionHistory determines mock behavior
- [Mock Mode Manager](./mock-mode-manager.md) - Mock mode configuration

## The mockFindStochasticModifier Field

Each `State` in Brobot has a `mockFindStochasticModifier` field that represents an additional probability factor for whether the state's images will be found during mock runs.

### Purpose

The `mockFindStochasticModifier` field simulates environmental uncertainties that can affect GUI automation:

- **Network failures**: Connection issues that prevent pages from loading
- **Unexpected overlays**: Update windows, notifications, or popups that cover target elements
- **Rendering issues**: Elements with transparent backgrounds or poor contrast that make detection unreliable
- **Timing variations**: Elements that appear/disappear at unpredictable times
- **System interruptions**: OS-level dialogs, screen savers, or other interruptions

### How Mock Mode Actually Works

> ⚠️ **Current Implementation**: Mock behavior is determined by ActionHistory alone. The `mockFindStochasticModifier` field exists but is not currently consulted by the mock framework.

**Current Mock Behavior** (as implemented in `MockFind.java`):

1. **ActionHistory-Based Matching**: The mock framework uses `ActionHistory` stored in each `Pattern` to determine find results:
   - Retrieves historical match snapshots from past runs
   - Selects a random snapshot matching the current action type and active states
   - Returns the historical match results (matches found, locations, etc.)
   - If no history exists, returns a default successful match

2. **No Probability Calculations**: Currently, there is no random failure injection or probability-based decision making. Mock results are deterministic based on ActionHistory.

For details on how ActionHistory works, see:
- [ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md)
- [Action Recording](./action-recording.md)

### Planned Future Enhancement: mockFindStochasticModifier

The `mockFindStochasticModifier` field is reserved for future enhancement to add probabilistic failure injection:
- Would act as an additional probability modifier
- Could simulate random environmental failures
- Would work in conjunction with ActionHistory

### Setting the Field (For Future Use)

The field can be set via `State.Builder`, though it currently has no effect:

```java
import io.github.jspinak.brobot.model.state.State;

// Programmatic approach using State.Builder
State loginState = new State.Builder()
    .setName("LoginScreen")
    .setBaseMockFindStochasticModifier(95)  // Field exists but not currently used
    .build();

// Note: In Brobot 1.1.0, states are typically defined using @State annotations
// on classes, not programmatically. See the States Guide for modern patterns.
```

For modern state definition patterns, see [States Guide](../01-getting-started/states.md).

## Implementation Notes

### Framework Behavior

The Brobot framework's `StateMemory` class **does NOT modify** `mockFindStochasticModifier` during state transitions:

✅ **Actual StateMemory Behavior**:
```java
// From StateMemory.java:
// "Note: mockFindStochasticModifier is NOT modified here as it's only relevant
//  for mock mode and should be controlled by the mock framework, not by state activation."
```

The field is designed to be:
- Set during state definition
- Managed by mock configuration (when future implementation is complete)
- NOT modified during state transitions

### TransitionExecutor Behavior

The `TransitionExecutor` class resets the field to base values:

```java
import io.github.jspinak.brobot.model.state.State;

// TransitionExecutor calls this method when activating states:
state.setProbabilityToBaseProbability();  // Resets to baseMockFindStochasticModifier
```

This ensures state probability returns to configured base values after transitions.

## Configuration

### Setting Base Values via State.Builder

When using the programmatic approach, set the base value via the builder:

```java
import io.github.jspinak.brobot.model.state.State;

// Using State.Builder (programmatic approach)
State myState = new State.Builder()
    .setName("MyState")
    .setBaseMockFindStochasticModifier(90)  // Currently not used, reserved for future
    .build();
```

> **Note**: The modern Brobot 1.1.0 approach uses `@State` annotations on classes rather than programmatic builders. The `@State` annotation does not currently have a parameter for `baseMockFindStochasticModifier`. See [States Guide](../01-getting-started/states.md) for current patterns.

### Common Values (For Future Use)

> ⚠️ **Note**: These values are illustrative for when the feature is implemented. Currently, the field has no effect on mock behavior.

- **100**: Perfect availability (default value)
- **95-99**: Highly stable states (main navigation, home screens)
- **85-94**: Normal states (typical application screens)
- **70-84**: Less stable states (dynamic content, Ajax-heavy pages)
- **50-69**: Unreliable states (states with frequent overlays or interruptions)

## How ActionHistory Currently Works in Mock Mode

> ✅ **Current Implementation**: Mock behavior is entirely driven by ActionHistory.

The mock framework uses [ActionHistory](./actionhistory-mock-snapshots.md) to create realistic test behavior:

1. **Historical Snapshot Selection**: When a find operation occurs in mock mode:
   - MockFind retrieves ActionHistory from the Pattern
   - Selects a random historical snapshot matching:
     - Current action type (FIND vs VANISH)
     - Active states
     - Find strategy (BEST, EACH, ALL)
   - Returns the historical match results

2. **Realistic Variation**: By replaying actual historical outcomes, mocks naturally include:
   - Successful finds with actual match locations
   - Failed finds when historically no matches were found
   - Varying match counts and positions
   - Realistic timing (via mock timing configuration)

3. **Default Behavior**: If no ActionHistory exists, MockFind returns a default successful match, allowing tests to run even without recorded history.

For detailed information:
- [ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md) - Creating and using ActionHistory
- [Action Recording](./action-recording.md) - Recording action history for mocks
- [ActionHistory Integration Testing](./actionhistory-integration-testing.md) - Testing with ActionHistory

## Best Practices for Mock Testing

### Current Best Practices (ActionHistory-Based)

1. **Record ActionHistory**: Build comprehensive ActionHistory data for realistic mocks
   - Run automation in real mode to capture actual behavior
   - Include both success and failure scenarios
   - See [Action Recording](./action-recording.md)

2. **Use Mock Mode for Fast Testing**: Enable mock mode for unit and integration tests
   - Tests run in ~10-40ms instead of seconds
   - No screen capture or GUI interaction required
   - See [Mock Mode Guide](./mock-mode-guide.md)

3. **Test with BrobotTestBase**: All tests should extend BrobotTestBase
   - Automatic mock mode configuration
   - Headless environment support
   - See [Test Utilities](./test-utilities.md)

4. **Use Profile-Based Testing**: Create different test profiles
   - Development: Full ActionHistory for comprehensive testing
   - CI/CD: Minimal ActionHistory for fast builds
   - See [Profile-Based Testing](./profile-based-testing.md)

### Future Practices (When mockFindStochasticModifier Is Implemented)

1. **Set realistic probability values**: Model actual state reliability
2. **Document state stability**: Explain why certain states have lower availability
3. **Don't modify at runtime**: Keep configuration static for reproducible tests

## Testing Strategies

### Testing with ActionHistory

Create realistic test scenarios by recording and replaying ActionHistory:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.model.action.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

public class ActionHistoryTest extends BrobotTestBase {

    @Autowired
    private Action action;

    @Test
    public void testWithHistoricalBehavior() {
        // Mock mode is automatically enabled by BrobotTestBase
        assertTrue(isMockMode());

        // The mock framework uses ActionHistory to determine find results
        // Behavior varies based on recorded history:
        // - If history shows 80% success, mock will replay mixed results
        // - If history shows 100% success, mock will always succeed
        // - If no history exists, mock returns default success

        // Your test logic here using the Action API
        // Results will be based on ActionHistory, not random probability
    }
}
```

### Testing with Custom ActionHistory

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.actions.actionHistory.ActionHistory;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.patterns.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class CustomHistoryTest extends BrobotTestBase {

    private StateImage testImage;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Create StateImage with Pattern
        testImage = new StateImage.Builder()
            .addPatterns("test-pattern")
            .setName("Test Image")
            .build();

        // ActionHistory can be built programmatically for testing
        // See ActionHistory Mock Snapshots guide for details
    }

    @Test
    public void testWithMockMode() {
        // Test behavior using ActionHistory-driven mocks
        // See ActionHistory Integration Testing guide for patterns
    }
}
```

For complete testing patterns, see:
- [Integration Testing](./integration-testing.md)
- [Unit Testing](./unit-testing.md)
- [ActionHistory Integration Testing](./actionhistory-integration-testing.md)

## Conclusion

### Current State

Brobot's mock mode currently uses **ActionHistory** to create realistic test behavior by replaying historical action outcomes. This provides:
- Fast test execution (~10-40ms per action)
- Realistic variation based on actual past behavior
- Headless testing support
- Deterministic but varied test results

The `mockFindStochasticModifier` field exists in the State model as a reserved field for future probabilistic enhancements.

### Using Mock Mode Effectively

For effective mock-based testing:

1. **Build ActionHistory**: Record real automation runs to capture actual behavior patterns
2. **Use BrobotTestBase**: Extend BrobotTestBase for automatic mock mode configuration
3. **Test comprehensively**: Include both success and failure scenarios in ActionHistory
4. **Profile-based testing**: Use different test profiles for development vs CI/CD

See the comprehensive testing guides:
- [Mock Mode Guide](./mock-mode-guide.md) - Complete mock mode documentation
- [Testing Introduction](./testing-intro.md) - Overview of testing approaches
- [ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md) - Building ActionHistory data

## Related Documentation

### Core Mock Mode Documentation
- **[Mock Mode Guide](./mock-mode-guide.md)** - Comprehensive guide to mock mode
- **[Mock Mode Manager](./mock-mode-manager.md)** - Centralized mock mode management
- **[Mock Mode Migration](./mock-mode-migration.md)** - Migrating to MockModeManager

### ActionHistory Documentation
- **[ActionHistory Mock Snapshots](./actionhistory-mock-snapshots.md)** - Creating and using ActionHistory
- **[Action Recording](./action-recording.md)** - Recording action history
- **[ActionHistory Integration Testing](./actionhistory-integration-testing.md)** - Testing with ActionHistory

### Testing Guides
- **[Testing Introduction](./testing-intro.md)** - Overview of Brobot testing approaches
- **[Unit Testing](./unit-testing.md)** - Unit testing patterns with BrobotTestBase
- **[Integration Testing](./integration-testing.md)** - Integration test patterns with Spring
- **[Profile-Based Testing](./profile-based-testing.md)** - Profile-specific test configurations
- **[Test Utilities](./test-utilities.md)** - BrobotTestBase and testing utilities
- **[Testing Strategy](./testing-strategy.md)** - Overall testing strategy

### State Management
- **[States Guide](../01-getting-started/states.md)** - Foundational guide to States and @State annotation
- **[States - Theoretical Foundations](../05-theoretical-foundations/states.md)** - Theoretical model of states

### Configuration
- **[BrobotProperties Usage](../03-core-library/configuration/brobot-properties-usage.md)** - Configuring via properties
- **[Properties Reference](../03-core-library/configuration/properties-reference.md)** - Complete property reference

### Advanced Topics
- **[Enhanced Mocking](./advanced/enhanced-mocking.md)** - Advanced mock scenarios
- **[Debugging Pattern Matching](./debugging-pattern-matching.md)** - Troubleshooting pattern matching