---
sidebar_position: 16
---

# Search Regions and Fixed Locations

Understanding the difference between normal search regions and fixed locations is crucial for efficient pattern matching in Brobot. This guide explains these concepts and how they work together.

## Quick Start

Define a search region and mark a pattern as fixed in just a few lines:

```java
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;

// Define where to search (lower left quarter)
Region searchArea = Region.builder()
    .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
    .build();

// Create pattern with search region
StateImage logo = new StateImage.Builder()
    .addPatterns("logo.png")
    .setSearchRegionForAllPatterns(searchArea)
    .build();

// Mark as fixed - will remember location after first find
logo.getPatterns().forEach(p -> p.setFixed(true));
```

**Result**: First search uses the defined region. Subsequent searches go directly to the saved location for near-instant results.

## Overview

Brobot uses two complementary concepts for locating visual elements:
- **Search Regions**: Areas where patterns are searched for
- **Fixed Locations**: Remembered positions where patterns were previously found

### Visual Flow: How Search Regions and Fixed Locations Work Together

```
┌─────────────────────────────────────────────────────────────┐
│ Screen                                                      │
│                                                             │
│  ┌────────────────────────────────┐                        │
│  │ Search Region (defined)        │                        │
│  │                                │                        │
│  │    Pattern                     │                        │
│  │    First Search                │                        │
│  │    ├─ Search entire region     │                        │
│  │    └─ Found at (100, 200) ✓    │                        │
│  │         ↓                       │                        │
│  │    Fixed Location Set           │                        │
│  │    └─ Saves position (100, 200) │                        │
│  │                                │                        │
│  └────────────────────────────────┘                        │
│                                                             │
│  Subsequent Searches:                                       │
│  ┌──────┐ ← Fixed Location (100, 200)                      │
│  │ Logo │    Searches ONLY here                            │
│  └──────┘    Near-instant result!                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘

Flow:
  Define Region → First Search → Save Location → Fast Subsequent Searches
   (50% of screen)  (150ms)      (fixed region)   (< 1ms, no image matching)
```

## Normal Search Regions

Normal search regions define the areas where Brobot will look for patterns. They limit the search space, improving performance and accuracy.

### Defining Search Regions

```java
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;

// Define a search region for the lower left quarter of the screen
Region lowerLeftQuarter = Region.builder()
    .withScreenPercentage(0.0, 0.5, 0.5, 0.5)  // x=0%, y=50%, width=50%, height=50%
    .build();

// Apply to a StateImage
StateImage prompt = new StateImage.Builder()
    .addPatterns("prompt-1", "prompt-2", "prompt-3")
    .setName("Prompt")
    .setSearchRegionForAllPatterns(lowerLeftQuarter)
    .build();
```

> For more details on `Region.builder()` and percentage-based positioning, see [Screen Adaptive Regions](./screen-adaptive-regions.md).

### Multiple Search Regions

You can define multiple search regions for more complex search patterns:

```java
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;

// Define multiple regions
Region topRegion = Region.builder()
    .withScreenPercentage(0.0, 0.0, 1.0, 0.25)  // Top 25% of screen
    .build();

Region bottomRegion = Region.builder()
    .withScreenPercentage(0.0, 0.75, 1.0, 0.25)  // Bottom 25% of screen
    .build();

// Add multiple search regions to a pattern
Pattern pattern = new Pattern("button.png");
pattern.addSearchRegion(topRegion);
pattern.addSearchRegion(bottomRegion);
// Pattern will be searched in both regions
```

## Fixed Locations

Fixed locations are used for UI elements that always appear in the same position. When a pattern marked as "fixed" is found, Brobot remembers its location for faster subsequent searches.

### How Fixed Locations Work

1. **Initial Search**: Pattern is searched within defined search regions
2. **Location Memory**: When found, the location is saved as the fixed region
3. **Subsequent Searches**: Future searches are restricted to the fixed location only

> **Important**: Once a fixed region is set, searches use **only** that region. There is no automatic fallback to the original search regions if the pattern is not found. To search elsewhere again, you must explicitly call `resetFixedSearchRegion()`.

### Marking Patterns as Fixed

```java
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;

// Define region for top of screen
Region topOfScreen = Region.builder()
    .withScreenPercentage(0.0, 0.0, 1.0, 0.15)  // Top 15% of screen
    .build();

StateImage fixedElement = new StateImage.Builder()
    .addPatterns("status-bar.png")
    .setName("StatusBar")
    .setSearchRegionForAllPatterns(topOfScreen)
    .build();

// Mark patterns as fixed
fixedElement.getPatterns().forEach(p -> p.setFixed(true));
```

## The Relationship Between Search Regions and Fixed Locations

The key concept to understand:

> **The normal search region defines a limited area in which to search. StateImages marked as fixed will set the fixed region when found. However, until the image has been found, it will continue to search within the search regions defined for it.**

This dual approach provides:
- **Efficiency**: Once found, fixed elements are located instantly (no image matching required)
- **Flexibility**: Initial searches can cover broader areas
- **Stability**: Fixed regions work best for static UI elements; call `resetFixedSearchRegion()` if the element moves

## Practical Example: Claude Automator

Here's a real-world example from the Claude Automator project:

```java
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import lombok.Getter;

@State(initial = true)
@Getter
public class PromptState {

    private final StateImage claudePrompt;

    public PromptState() {
        // Define search region for lower left quarter
        Region lowerLeftQuarter = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
            .build();

        // Create StateImage with search region
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1",
                        "prompt/claude-prompt-2",
                        "prompt/claude-prompt-3")
            .setName("ClaudePrompt")
            .setSearchRegionForAllPatterns(lowerLeftQuarter)
            .build();

        // Mark as fixed - will remember location once found
        claudePrompt.getPatterns().forEach(p -> p.setFixed(true));
    }
}
```

### What Happens During Execution

1. **First Search**:
   - Searches for claude-prompt patterns in the lower left quarter
   - When found, saves the exact location as the fixed region

2. **Subsequent Searches**:
   - Searches only within the saved fixed location
   - To search the lower left quarter again, call `claudePrompt.getPatterns().forEach(p -> p.resetFixedSearchRegion())`

## Best Practices

### When to Use Fixed Locations

Use fixed locations for:
- Navigation bars
- Status indicators
- Menu buttons
- Any UI element with a consistent position

### When to Use Search Regions Only

Use search regions without fixed for:
- Dynamic content
- Popup dialogs
- Moving elements
- Content that appears in varying positions

### Combining Both Approaches

For maximum efficiency:
1. Define a reasonable search region (not full screen unless necessary)
2. Mark as fixed if the element typically stays in one place
3. The initial search region is used for the first search; fixed location for subsequent searches

```java
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;

// Define region for top menu bar
Region topMenuBar = Region.builder()
    .withScreenPercentage(0.0, 0.0, 1.0, 0.1)  // Top 10% of screen
    .build();

// Robust pattern configuration
StateImage menuButton = new StateImage.Builder()
    .addPatterns("menu.png")
    .setName("Menu")
    .setSearchRegionForAllPatterns(topMenuBar)  // Initial search area
    .build();

menuButton.getPatterns().forEach(p -> p.setFixed(true));  // Remember location after first find
```

## Advanced: Resetting Fixed Locations

Sometimes you need to reset fixed locations (e.g., after window resize or UI changes):

```java
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;

public class ResetFixedExample {

    public void resetExamples(StateImage stateImage) {
        // Reset fixed location for a single pattern
        Pattern pattern = stateImage.getPatterns().get(0);
        pattern.resetFixedSearchRegion();

        // Reset all fixed locations in a StateImage
        stateImage.getPatterns().forEach(p -> p.resetFixedSearchRegion());

        // After reset, the next search will use the original search regions
        // and set a new fixed location if found
    }
}
```

## Performance Metrics

### Real-World Performance Data

Based on typical Brobot automation scenarios (1920x1080 resolution, PNG patterns):

| Approach | Initial Search | Subsequent Searches | Memory | Use Case |
|----------|---------------|---------------------|--------|----------|
| **Full Screen** | ~150-200ms | ~150-200ms | Baseline | Last resort only |
| **Search Region (50%)** | ~75-100ms | ~75-100ms | Baseline | Dynamic content |
| **Search Region (25%)** | ~40-50ms | ~40-50ms | Baseline | Targeted areas |
| **Fixed Location** | ~75-100ms | **< 1ms** | +50 bytes/pattern | Static UI elements |

### Performance Benefits

**Fixed locations provide dramatic speedup:**

```
Without Fixed Location:
├─ Every search: Full image matching (~100ms)
├─ 100 searches: 10 seconds total
└─ CPU: Constant image processing

With Fixed Location:
├─ First search: Full image matching (~100ms)
├─ Next 99 searches: Direct location check (< 1ms each)
├─ 100 searches: ~0.2 seconds total
└─ CPU: 99% reduction in processing

Performance Gain: 50x faster for repeated searches
```

### Overhead Analysis

| Component | Cost | When Incurred |
|-----------|------|---------------|
| **Region Definition** | ~0.1ms | Once at StateImage creation |
| **Region Calculation** | ~0.01ms | Per search operation |
| **Fixed Location Save** | ~0.05ms | Once when pattern found |
| **Fixed Location Check** | < 0.01ms | Per subsequent search |
| **Memory per Fixed Pattern** | ~50 bytes | While pattern is fixed |

### Performance Recommendations

✅ **USE fixed locations when:**
- Pattern appears 10+ times in automation
- UI element never moves (navigation bars, logos)
- Search time is critical for responsiveness
- Pattern matching is CPU-intensive (high resolution, complex patterns)

❌ **AVOID fixed locations when:**
- Pattern appears in different locations each time
- UI layout changes frequently
- Element only searched once or twice
- Memory is extremely constrained (embedded systems)

**Bottom Line**: For static UI elements searched repeatedly, fixed locations provide **50x performance improvement** with negligible memory overhead (~50 bytes per pattern).

## Common Pitfalls

### 1. Expecting Automatic Fallback

❌ **WRONG**: Assuming fixed location automatically falls back to search regions

```java
StateImage logo = new StateImage.Builder()
    .addPatterns("logo.png")
    .setSearchRegionForAllPatterns(topOfScreen)
    .build();

logo.getPatterns().forEach(p -> p.setFixed(true));

// First search finds logo at (100, 50) - fixed location saved
action.find(logo);  // ✓ Found

// UI changes, logo moves to (500, 50)
action.find(logo);  // ✗ NOT FOUND - only checks (100, 50)
// ERROR: No automatic fallback to topOfScreen region!
```

✅ **CORRECT**: Explicitly reset when UI changes

```java
// Detect UI change (window resize, layout shift, etc.)
if (uiLayoutChanged()) {
    // Reset fixed location to search original region
    logo.getPatterns().forEach(p -> p.resetFixedSearchRegion());
}

action.find(logo);  // ✓ Searches topOfScreen region again
```

### 2. Using Fixed for Dynamic Content

❌ **WRONG**: Marking dynamic content as fixed

```java
// Notification popup appears in different positions
StateImage notification = new StateImage.Builder()
    .addPatterns("notification.png")
    .build();

notification.getPatterns().forEach(p -> p.setFixed(true));  // BAD IDEA!

// First notification at top-right
action.find(notification);  // ✓ Found at (800, 50)

// Next notification appears at bottom-right
action.find(notification);  // ✗ Still searches (800, 50) only!
```

✅ **CORRECT**: Use search regions without fixed for dynamic content

```java
// Define possible areas for notifications
Region topRight = Region.builder()
    .withScreenPercentage(0.75, 0.0, 0.25, 0.25)
    .build();

Region bottomRight = Region.builder()
    .withScreenPercentage(0.75, 0.75, 0.25, 0.25)
    .build();

StateImage notification = new StateImage.Builder()
    .addPatterns("notification.png")
    .build();

// Add both regions - will search both areas
notification.getPatterns().forEach(p -> {
    p.addSearchRegion(topRight);
    p.addSearchRegion(bottomRight);
});
// Don't mark as fixed - content is dynamic
```

### 3. Forgetting to Define Search Regions

❌ **WRONG**: Relying on full-screen search by default

```java
// No search region defined
StateImage button = new StateImage.Builder()
    .addPatterns("button.png")
    .build();

// Searches entire screen - SLOW!
action.find(button);  // Takes ~150-200ms every time
```

✅ **CORRECT**: Define appropriate search regions

```java
// Button is in bottom toolbar
Region bottomToolbar = Region.builder()
    .withScreenPercentage(0.0, 0.9, 1.0, 0.1)  // Bottom 10%
    .build();

StateImage button = new StateImage.Builder()
    .addPatterns("button.png")
    .setSearchRegionForAllPatterns(bottomToolbar)
    .build();

button.getPatterns().forEach(p -> p.setFixed(true));

// First search: ~20-30ms (only searches 10% of screen)
// Subsequent: < 1ms (uses fixed location)
```

### 4. Fixed Location Set Too Early

❌ **WRONG**: Marking as fixed before first successful find

```java
StateImage logo = new StateImage.Builder()
    .addPatterns("logo.png")
    .build();

// Mark as fixed immediately
logo.getPatterns().forEach(p -> p.setFixed(true));

// First search fails - but no error yet
action.find(logo);  // Pattern not found anywhere

// Problem: No fixed location was ever set!
// All future searches will also fail if pattern still doesn't exist
```

✅ **CORRECT**: Mark as fixed, but verify it was found first

```java
StateImage logo = new StateImage.Builder()
    .addPatterns("logo.png")
    .build();

logo.getPatterns().forEach(p -> p.setFixed(true));

// Verify first search succeeds
ActionResult result = action.find(logo);
if (!result.isSuccess()) {
    log.warn("Logo not found - fixed location not set");
    // Handle error appropriately
}
```

### 5. Confusing Search Region with Match Region

❌ **WRONG**: Thinking search region affects the matched size

```java
// Define small search region
Region smallArea = Region.builder()
    .withScreenPercentage(0.4, 0.4, 0.2, 0.2)  // Small 20x20% area
    .build();

StateImage largeLogo = new StateImage.Builder()
    .addPatterns("large-logo.png")  // Image is 300x100 pixels
    .setSearchRegionForAllPatterns(smallArea)
    .build();

// Misconception: "Search region limits match size"
// Reality: Search region only limits WHERE to search
// The pattern can be ANY size, as long as it's WITHIN the search region
```

✅ **CORRECT**: Understanding search region vs match region

```java
// Search region: WHERE to look
// Pattern size: WHAT to look for (defined by the image file)

Region searchArea = Region.builder()
    .withScreenPercentage(0.0, 0.0, 0.5, 0.5)  // Top-left quarter
    .build();

StateImage logo = new StateImage.Builder()
    .addPatterns("logo.png")  // Will match actual pattern size from image
    .setSearchRegionForAllPatterns(searchArea)  // Limits WHERE to search
    .build();

// Search region must be LARGER than the pattern
// Otherwise pattern can't be found within it
```

### 6. Not Resetting After Window Resize

❌ **WRONG**: Keeping fixed locations after window/resolution changes

```java
// User resizes application window
// Fixed locations are still pointing to old coordinates
// All subsequent searches will fail!
```

✅ **CORRECT**: Reset fixed locations on layout changes

```java
import io.github.jspinak.brobot.model.state.StateImageList;

public class WindowResizeHandler {

    private final StateImageList allStateImages;

    public void onWindowResize() {
        // Reset all fixed locations
        allStateImages.getAllImages().forEach(stateImage ->
            stateImage.getPatterns().forEach(p -> p.resetFixedSearchRegion())
        );

        log.info("Reset all fixed locations due to window resize");
    }
}
```

## Debugging Search Regions

Enable visual feedback to see search regions:

```properties
# In application.properties
brobot.debug.image.visual.show-search-regions=true
brobot.logging.verbose.show-search-regions=true
```

This will highlight search regions during execution, helping you verify your configuration.

> For testing search regions in headless environments or CI/CD pipelines, see [Mock Mode Guide](../../../04-testing/mock-mode-guide.md).

## Testing Search Regions

### Testing with Mock Mode

Search regions work seamlessly with Brobot's mock mode for fast, headless testing:

```java
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.actions.Action;
import io.github.jspinak.brobot.actions.ActionResult;
import io.github.jspinak.brobot.action.config.PatternFindOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

public class SearchRegionTest extends BrobotTestBase {

    @Autowired
    private Action action;

    @Test
    public void testSearchWithDefinedRegion() {
        // Define search region
        Region lowerLeft = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
            .build();

        // Create StateImage with region
        StateImage testImage = new StateImage.Builder()
            .addPatterns("test-pattern.png")
            .setName("TestImage")
            .setSearchRegionForAllPatterns(lowerLeft)
            .build();

        // In mock mode, this will use the defined region
        ActionResult result = action.perform(
            new PatternFindOptions.Builder().build(),
            testImage
        );

        assertTrue(result.isSuccess());
    }

    @Test
    public void testFixedLocationBehavior() {
        // Create StateImage marked as fixed
        StateImage fixedImage = new StateImage.Builder()
            .addPatterns("fixed-element.png")
            .setName("FixedElement")
            .build();

        fixedImage.getPatterns().forEach(p -> p.setFixed(true));

        // First search sets the fixed location
        ActionResult firstResult = action.perform(
            new PatternFindOptions.Builder().build(),
            fixedImage
        );
        assertTrue(firstResult.isSuccess());

        // Subsequent searches use only the fixed location
        ActionResult secondResult = action.perform(
            new PatternFindOptions.Builder().build(),
            fixedImage
        );
        assertTrue(secondResult.isSuccess());

        // Reset to search original regions again
        fixedImage.getPatterns().forEach(p -> p.resetFixedSearchRegion());
    }
}
```

## Summary

- **Search regions** limit where patterns are searched for
- **Fixed locations** remember where patterns were previously found
- Once set, fixed patterns search **only** their saved location (no automatic fallback)
- Use `resetFixedSearchRegion()` to search original regions again
- This approach provides maximum efficiency for static UI elements
- Proper configuration significantly improves automation performance

## Related Documentation

### Core Concepts
- **[Declarative Region Definition](./declarative-region-definition.md)** - Define search regions relative to other objects
- **[Screen Adaptive Regions](./screen-adaptive-regions.md)** - Build regions with Region.builder() and percentage-based positioning
- **[States Guide](../../../01-getting-started/states.md)** - Understanding states and StateImage

### Using Search Regions
- **[ActionConfig Overview](../../action-config/01-overview.md)** - Using search regions with actions
- **[ActionConfig Examples](../../action-config/03-examples.md)** - Practical examples of search regions in automation
- **[Processes as Objects](./processes-as-objects.md)** - Complex UI process modeling with regions

### Testing
- **[Mock Mode Guide](../../../04-testing/mock-mode-guide.md)** - Testing search regions without a real GUI
- **[Integration Testing](../../../04-testing/integration-testing.md)** - Integration test strategies
- **[Unit Testing Guide](../../../04-testing/unit-testing.md)** - Unit test patterns

### Tutorials
- **[Claude Automator Tutorial](../../tutorials/tutorial-claude-automator/intro.md)** - Real-world example using search regions and fixed locations