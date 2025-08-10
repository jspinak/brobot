---
sidebar_position: 16
---

# Search Regions and Fixed Locations

Understanding the difference between normal search regions and fixed locations is crucial for efficient pattern matching in Brobot. This guide explains these concepts and how they work together.

## Overview

Brobot uses two complementary concepts for locating visual elements:
- **Search Regions**: Areas where patterns are searched for
- **Fixed Locations**: Remembered positions where patterns were previously found

## Normal Search Regions

Normal search regions define the areas where Brobot will look for patterns. They limit the search space, improving performance and accuracy.

### Defining Search Regions

```java
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

### Multiple Search Regions

You can define multiple search regions for more complex search patterns:

```java
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
3. **Subsequent Searches**: Future searches check the fixed location first

### Marking Patterns as Fixed

```java
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
- **Efficiency**: Once found, fixed elements are located quickly
- **Flexibility**: Initial searches can cover broader areas
- **Robustness**: If a fixed element moves, it can be re-discovered

## Practical Example: Claude Automator

Here's a real-world example from the Claude Automator project:

```java
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
   - Checks the saved fixed location first
   - Falls back to searching the lower left quarter if not found at fixed location

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

For maximum robustness:
1. Define a reasonable search region (not full screen unless necessary)
2. Mark as fixed if the element typically stays in one place
3. The search region acts as a fallback if the fixed location fails

```java
// Robust pattern configuration
StateImage menuButton = new StateImage.Builder()
    .addPatterns("menu.png")
    .setName("Menu")
    .setSearchRegionForAllPatterns(topMenuBar)  // Fallback search area
    .build();

menuButton.getPatterns().forEach(p -> p.setFixed(true));  // Remember location
```

## Advanced: Resetting Fixed Locations

Sometimes you need to reset fixed locations (e.g., after window resize):

```java
// Reset fixed location for a pattern
pattern.resetFixedSearchRegion();

// Reset all fixed locations in a StateImage
stateImage.getPatterns().forEach(p -> p.resetFixedSearchRegion());
```

## Performance Considerations

1. **Search Region Size**: Smaller regions = faster searches
2. **Fixed Location Benefits**: Near-instant location for fixed elements
3. **Full Screen Searches**: Avoid unless absolutely necessary

### Performance Comparison

| Approach | Initial Search | Subsequent Searches | Use Case |
|----------|---------------|-------------------|----------|
| Full Screen | Slowest | Slowest | Last resort |
| Search Region | Fast | Fast | Dynamic content |
| Fixed + Region | Fast | Fastest | Static UI elements |

## Debugging Search Regions

Enable visual feedback to see search regions:

```properties
# In application.properties
brobot.console.actions.show-search-regions=true
brobot.logging.verbose.show-search-regions=true
```

This will highlight search regions during execution, helping you verify your configuration.

## Summary

- **Search regions** limit where patterns are searched for
- **Fixed locations** remember where patterns were previously found
- Fixed patterns search their saved location first, then fall back to search regions
- This dual approach balances efficiency with robustness
- Proper configuration significantly improves automation performance