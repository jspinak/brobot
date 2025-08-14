---
sidebar_position: 3
title: 'ActionHistory and Mock Snapshots'
---

# ActionHistory and Mock Snapshots

## Overview

ActionHistory is a critical component for mock mode testing in Brobot. It provides historical data about pattern matches and actions, enabling realistic simulation of GUI interactions without actual screen access. This is **required** for patterns to be "found" in mock mode.

## Key Concepts

### What is ActionHistory?

ActionHistory stores a collection of `ActionRecord` objects that represent past interactions with GUI elements. Each record contains:
- Match information (location, similarity score)
- Action configuration (find options, strategies)
- Success/failure status
- Execution duration

### Why ActionHistory is Required in Mock Mode

In mock mode, Brobot doesn't perform real pattern matching. Instead, it uses ActionHistory to:
1. Determine if a pattern should be "found"
2. Provide realistic match locations and scores
3. Simulate timing and performance characteristics
4. Enable deterministic or probabilistic testing

**Important**: Without ActionHistory, patterns will never be found in mock mode, causing all find operations to fail.

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
ActionHistory history = createCustomHistory();
StateImage image = new StateImage.Builder()
    .addPatterns("pattern.png")
    .withActionHistory(history)
    .build();
```

#### 2. Supplier Function (Lazy Initialization)

```java
StateImage image = new StateImage.Builder()
    .addPatterns("pattern.png")
    .withActionHistory(() -> createComplexHistory())
    .build();
```

#### 3. Single ActionRecord

```java
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
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryBuilder;

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
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;

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

// Element at specific screen position
ActionHistory centerElement = MockActionHistoryFactory.forScreenPosition(
    Positions.Name.MIDDLEMIDDLE, 100, 50);  // width=100, height=50

// Element in lower-left (common for status/chat)
ActionHistory lowerLeft = MockActionHistoryFactory.lowerLeftElement(200, 80);
```

### Custom Configuration

```java
// Use custom configuration with lambda
ActionHistory custom = MockActionHistoryFactory.withConfig(config -> 
    config.successRate(0.85)
          .recordCount(15)
          .matchRegion(new Region(100, 100, 50, 50))
          .minDuration(20)
          .maxDuration(80));
```

### Caching for Performance

```java
// Cache frequently used histories
ActionHistory cached = MockActionHistoryFactory.cached("main-button", 
    () -> MockActionHistoryFactory.reliableButton(buttonRegion));

// Clear cache between test suites
MockActionHistoryFactory.clearCache();
```

## Complete Example: Claude Automator

Here's a real-world example showing how to use the new ActionHistory features in a state class:

```java
package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.testing.mock.history.MockActionHistoryFactory;
import lombok.Getter;

@State(initial = true)
@Getter
public class PromptState {
    
    private final StateImage claudePrompt;
    
    public PromptState() {
        // Define search region for lower-left quarter of screen
        Region lowerLeftQuarter = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
            .build();
        
        // Create StateImage with integrated ActionHistory for mock mode
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1.png",
                        "prompt/claude-prompt-2.png",
                        "prompt/claude-prompt-3.png")
            .setName("ClaudePrompt")
            .setSearchRegionForAllPatterns(lowerLeftQuarter)
            .setFixedForAllPatterns(true)
            // ActionHistory is required for mock mode finds
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
if (FrameworkSettings.mock) {
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

- [Mock Mode Guide](./mock-mode-guide.md) - Complete mock mode reference
- [Integration Testing](./integration-testing.md) - Testing strategies
- [Profile-Based Architecture](./profile-based-architecture.md) - Test profile configuration
- [StateImage API](../03-core-library/guides/search-regions-and-fixed-locations.md) - StateImage builder reference