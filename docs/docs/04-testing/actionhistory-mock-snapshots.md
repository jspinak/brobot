---
sidebar_position: 3
title: 'ActionHistory and Mock Snapshots'
---

# ActionHistory and Mock Snapshots

## Overview

ActionHistory is a critical component for mock mode testing in Brobot. It provides historical data about pattern matches and actions, enabling realistic simulation of GUI interactions without actual screen access. While **highly recommended** for patterns to be reliably "found" in mock mode, Brobot provides a fallback mechanism that generates default matches (at location 100,100 with 50x50 size and 95% similarity) when ActionHistory is not configured.

## Key Concepts

### What is ActionHistory?

ActionHistory stores a collection of `ActionRecord` objects that represent past interactions with GUI elements. Each record contains:
- Match information (location, similarity score)
- Action configuration (find options, strategies)
- Success/failure status
- Execution duration

### Why ActionHistory is Highly Recommended in Mock Mode

In mock mode, Brobot doesn't perform real pattern matching. Instead, it uses ActionHistory to:
1. Determine if a pattern should be "found" based on realistic success rates
2. Provide realistic match locations and scores from historical data
3. Simulate timing and performance characteristics accurately
4. Enable deterministic or probabilistic testing with configurable success rates

**Important**: While Brobot provides a fallback mechanism that generates default matches without ActionHistory, these fallback matches have fixed properties (location 100,100, size 50x50, 95% similarity) which may not reflect your actual UI behavior. For realistic testing, always configure ActionHistory with appropriate success rates and match locations.

## New Builder Integration (v1.0.0+)

Starting with Brobot v1.0.0, ActionHistory can be configured directly in the StateImage builder, eliminating the need for separate initialization methods.

### Basic Usage

```java
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;

// Create a StateImage with ActionHistory in one step
StateImage button = new StateImage.Builder()
    .addPatterns("button/ok-button.png")
    .setName("OkButton")
    .withActionHistory(MockActionHistoryFactory.reliableButton(
        new Region(100, 200, 80, 30)))
    .build();
```

### StateImage.Builder Methods

The StateImage.Builder class provides three methods for setting ActionHistory:

#### 1. Direct ActionHistory

```java
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.history.ActionHistory;

ActionHistory history = createCustomHistory();
StateImage image = new StateImage.Builder()
    .addPatterns("pattern.png")
    .withActionHistory(history)
    .build();
```

#### 2. Supplier Function (Lazy Initialization)

```java
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.history.ActionHistory;

StateImage image = new StateImage.Builder()
    .addPatterns("pattern.png")
    .withActionHistory(() -> createComplexHistory())
    .build();
```

#### 3. Single ActionRecord

```java
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.history.ActionRecord;

ActionRecord record = createSingleRecord();
StateImage image = new StateImage.Builder()
    .addPatterns("pattern.png")
    .withActionHistory(record)
    .build();
```

## MockActionHistoryBuilder

The `MockActionHistoryBuilder` provides a fluent API for creating custom ActionHistory configurations.

### Basic Configuration

```java
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.history.ActionHistory;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryBuilder;

// Assume region is defined elsewhere
Region region = new Region(100, 200, 80, 30);

ActionHistory history = MockActionHistoryBuilder.builder()
    .successRate(0.95)           // 95% success rate
    .matchRegion(region)          // Where matches occur
    .minSimilarity(0.90)          // Minimum similarity score
    .maxSimilarity(0.99)          // Maximum similarity score
    .minDuration(30)              // Minimum execution time (ms)
    .maxDuration(100)             // Maximum execution time (ms)
    .recordCount(20)              // Number of records to generate
    .build()
    .build();
```

### Preset Configurations

MockActionHistoryBuilder provides preset methods for common scenarios:

```java
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.history.ActionHistory;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryBuilder;

Region region = new Region(100, 200, 80, 30);

// Always found (100% success)
ActionHistory reliable = MockActionHistoryBuilder.Presets.alwaysFound(region);

// Highly reliable (95% success)
ActionHistory good = MockActionHistoryBuilder.Presets.reliable(region);

// Flaky element (70% success)
ActionHistory unstable = MockActionHistoryBuilder.Presets.flaky(region);

// Never found (0% success)
ActionHistory missing = MockActionHistoryBuilder.Presets.neverFound();
```

## MockActionHistoryFactory

The `MockActionHistoryFactory` provides factory methods for common UI patterns and screen positions.

### UI Pattern Methods

```java
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.history.ActionHistory;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;

Region buttonRegion = new Region(100, 200, 80, 30);
Region fieldRegion = new Region(150, 300, 200, 25);
Region loaderRegion = new Region(400, 500, 50, 50);
Region menuRegion = new Region(10, 10, 150, 30);
Region dialogRegion = new Region(300, 200, 400, 300);

// Reliable button (98% success, quick response)
ActionHistory button = MockActionHistoryFactory.reliableButton(buttonRegion);

// Dynamic text field (85% success, variable content)
ActionHistory textField = MockActionHistoryFactory.dynamicTextField(fieldRegion);

// Loading indicator (60% success, appears/disappears)
ActionHistory loader = MockActionHistoryFactory.loadingIndicator(loaderRegion);

// Menu item (90% success when visible)
ActionHistory menuItem = MockActionHistoryFactory.menuItem(menuRegion);

// Modal dialog (100% success when present)
ActionHistory dialog = MockActionHistoryFactory.modalDialog(dialogRegion);
```

### Screen Position Helpers

```java
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.history.ActionHistory;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;

// Element at specific screen position
ActionHistory centerElement = MockActionHistoryFactory.forScreenPosition(
    Positions.Name.MIDDLEMIDDLE, 100, 50);  // width=100, height=50

// Element in lower-left (common for status/chat)
ActionHistory lowerLeft = MockActionHistoryFactory.lowerLeftElement(200, 80);
```

### Caching for Performance

```java
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.history.ActionHistory;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;

Region buttonRegion = new Region(100, 200, 80, 30);

// Cache frequently used histories
ActionHistory cached = MockActionHistoryFactory.cached("main-button",
    () -> MockActionHistoryFactory.reliableButton(buttonRegion));

// Clear cache between test suites
MockActionHistoryFactory.clearCache();
```

## Complete Example: Claude Automator

Here's a real-world example showing how to use the new ActionHistory features in a state class. This example demonstrates:
- Using `@State` annotation for Spring component scanning
- Screen percentage-based regions for resolution independence
- Multiple pattern variants for robust matching
- Integrated ActionHistory configuration for mock mode support

```java
package com.claude.automator.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;
import lombok.Getter;

/**
 * State representing the Claude prompt interface.
 * Demonstrates best practices for ActionHistory integration.
 */
@Component
@State(initial = true)
@Getter
public class PromptState {

    private final StateImage claudePrompt;

    public PromptState() {
        // Define search region for lower-left quarter of screen
        // Using percentage-based region for resolution independence
        Region lowerLeftQuarter = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)  // x=0%, y=50%, w=50%, h=50%
            .build();

        // Create StateImage with integrated ActionHistory for mock mode
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1.png",
                        "prompt/claude-prompt-2.png",
                        "prompt/claude-prompt-3.png")
            .setName("ClaudePrompt")
            .setSearchRegionForAllPatterns(lowerLeftQuarter)
            .setFixedForAllPatterns(true)
            // ActionHistory highly recommended for realistic mock mode behavior
            // Without this, mock mode uses fallback with fixed location (100,100)
            .withActionHistory(MockActionHistoryFactory.lowerLeftElement(293, 83))
            .build();
    }
}
```

## Migration from Manual ActionHistory

### Old Approach (Pre-v1.0.0)

```java
public class MyState {
    private final StateImage button;
    
    public MyState() {
        button = new StateImage.Builder()
            .addPatterns("button.png")
            .build();
        
        // Separate method required
        createMockActionHistory();
    }
    
    private void createMockActionHistory() {
        ActionHistory history = new ActionHistory();
        // Manual creation of records...
        for (Pattern pattern : button.getPatterns()) {
            pattern.setMatchHistory(history);
        }
    }
}
```

### New Approach (v1.0.0+)

```java
public class MyState {
    private final StateImage button;
    
    public MyState() {
        // Everything in one builder chain
        button = new StateImage.Builder()
            .addPatterns("button.png")
            .withActionHistory(MockActionHistoryFactory.reliableButton(
                new Region(100, 200, 80, 30)))
            .build();
    }
}
```

## Best Practices

### 1. Always Set ActionHistory for Mock Mode

```java
// Check if mock mode is enabled
if (brobotProperties.getCore().isMock()) {
    stateImage = new StateImage.Builder()
        .addPatterns("pattern.png")
        .withActionHistory(MockActionHistoryFactory.reliable(region))
        .build();
}
```

### 2. Use Appropriate Factory Methods

Match the factory method to your UI element type:
- Buttons → `reliableButton()`
- Text fields → `dynamicTextField()`
- Loading indicators → `loadingIndicator()`
- Menus → `menuItem()`
- Dialogs → `modalDialog()`

### 3. Configure Realistic Success Rates

```java
// Critical elements should have high success rates
StateImage saveButton = new StateImage.Builder()
    .addPatterns("save.png")
    .withActionHistory(MockActionHistoryBuilder.builder()
        .successRate(0.98)  // Very reliable
        .build().build())
    .build();

// Optional elements can have lower rates
StateImage tooltip = new StateImage.Builder()
    .addPatterns("tooltip.png")
    .withActionHistory(MockActionHistoryBuilder.builder()
        .successRate(0.70)  // Sometimes visible
        .build().build())
    .build();
```

### 4. Use Screen-Adaptive Regions

```java
// Use percentage-based regions for resolution independence
Region adaptiveRegion = Region.builder()
    .withScreenPercentage(0.4, 0.4, 0.2, 0.2)  // Center 20% of screen
    .build();

StateImage centerElement = new StateImage.Builder()
    .addPatterns("center.png")
    .withActionHistory(MockActionHistoryFactory.reliable(adaptiveRegion))
    .build();
```

### 5. Leverage Caching for Performance

```java
// Cache histories that are used multiple times
public class StateFactory {
    private static final String BUTTON_CACHE_KEY = "main-button";
    
    public StateImage createButton() {
        return new StateImage.Builder()
            .addPatterns("button.png")
            .withActionHistory(MockActionHistoryFactory.cached(
                BUTTON_CACHE_KEY,
                () -> MockActionHistoryFactory.reliableButton(buttonRegion)))
            .build();
    }
}
```

## Troubleshooting

### Patterns Not Found in Mock Mode

**Problem**: Find operations fail with "No matches found" despite mock mode being enabled.

**Solution**: Ensure ActionHistory is set:
```java
// ❌ Wrong - No ActionHistory
StateImage image = new StateImage.Builder()
    .addPatterns("pattern.png")
    .build();

// ✅ Correct - ActionHistory included
StateImage image = new StateImage.Builder()
    .addPatterns("pattern.png")
    .withActionHistory(MockActionHistoryFactory.reliable(region))
    .build();
```

### Compilation Errors

**Problem**: "cannot find symbol: MockActionHistoryFactory"

**Solution**: Add the required import:
```java
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryBuilder;
```

### Inconsistent Test Results

**Problem**: Tests pass/fail randomly in mock mode.

**Solution**: Use deterministic success rates:
```java
// For deterministic tests, use 100% or 0% success rates
ActionHistory alwaysFound = MockActionHistoryBuilder.Presets.alwaysFound(region);
ActionHistory neverFound = MockActionHistoryBuilder.Presets.neverFound();
```

## Related Documentation

### Testing Guides
- **[Mock Mode Guide](./mock-mode-guide.md)** - Complete mock mode reference and configuration
- **[Integration Testing](./integration-testing.md)** - Testing strategies and best practices
- **[Unit Testing](./unit-testing.md)** - Unit testing patterns with Brobot
- **[Profile-Based Testing](./profile-based-testing.md)** - Test profile configuration and management
- **[Debugging Pattern Matching](./debugging-pattern-matching.md)** - Troubleshooting pattern matching issues
- **[Test Utilities](./test-utilities.md)** - Available testing utilities and helpers

### Core API References
- **[States in Brobot](../../01-getting-started/states.md)** - Understanding @State annotation and state management
- **[StateImage API](../03-core-library/user-guides/search-regions-and-fixed-locations.md)** - StateImage builder reference and search regions
<!-- ActionRecord API documentation not yet available -->

### Testing Infrastructure
- **[BrobotTestBase](./test-utilities.md#brobottestbase)** - Base class for Brobot tests with mock mode support
- **[TestUtil Classes](./test-utilities.md#testutil-classes)** - Utility classes for test setup and validation
- **[Mocking Guide](./mock-mode-guide.md#mocking-strategies)** - Advanced mocking strategies and patterns
- **[Action Recording](./action-recording.md)** - Recording actions for test replay (if exists)