---
sidebar_position: 15
---

# Declarative Region Definition

Brobot 1.1.0 introduces a powerful declarative approach to defining search regions for StateImages. This guide explains how to define regions that are dynamically calculated relative to other state objects.

## Overview

The declarative approach allows you to:
- Define search regions relative to other state objects
- Apply adjustments and fixed dimensions
- Eliminate manual region calculations in your action code
- Create more maintainable and reusable state definitions

## SearchRegionOnObject

The `SearchRegionOnObject` class enables dynamic region definition:

```java
public class SearchRegionOnObject {
    private StateObject.Type targetType;     // Type of target object (IMAGE, REGION, etc.)
    private String targetStateName;          // Name of the state containing the target
    private String targetObjectName;         // Name of the specific object
    private AdjustOptions adjustments;       // Position and size adjustments
    private AbsoluteDimensions absoluteDimensions; // Fixed width/height overrides
}
```

## Basic Usage

### Simple Relative Region

Define a search region relative to another StateImage:

```java
StateImage searchArea = new StateImage.Builder()
    .addPatterns("search-icon.png")
    .setName("SearchIcon")
    .setSearchRegionOnObject(new SearchRegionOnObject.Builder()
        .targetType(StateObject.Type.IMAGE)
        .targetState("MainMenu")
        .targetObject("MenuButton")
        .build())
    .build();
```

### With Adjustments

Apply position and size adjustments to the derived region:

```java
StateImage icon = new StateImage.Builder()
    .addPatterns("status-icon.png")
    .setName("StatusIcon")
    .setSearchRegionOnObject(new SearchRegionOnObject.Builder()
        .targetType(StateObject.Type.IMAGE)
        .targetState("Dashboard")
        .targetObject("HeaderBar")
        .adjustments(10, -5, 50, 20)  // x, y, width, height adjustments
        .build())
    .build();
```

### With Fixed Dimensions

Override the calculated dimensions with fixed values:

```java
StateImage button = new StateImage.Builder()
    .addPatterns("submit-button.png")
    .setName("SubmitButton")
    .setSearchRegionOnObject(new SearchRegionOnObject.Builder()
        .targetType(StateObject.Type.IMAGE)
        .targetState("Form")
        .targetObject("FormTitle")
        .yAdjust(100)               // Move down 100 pixels
        .absoluteDimensions(200, 50) // Fixed 200x50 region
        .build())
    .build();
```

## Real-World Example: Claude Automator

The claude-automator project demonstrates this pattern effectively:

```java
@State
@Getter
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        // The search region will be dynamically defined relative to the prompt
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .setName("ClaudeIcon")
            .setSearchRegionOnObject(new SearchRegionOnObject.Builder()
                    .targetType(StateObject.Type.IMAGE)
                    .targetState("Prompt")
                    .targetObject("ClaudePrompt")
                    .adjustments(3, 10, 30, 55)  // Relative to prompt location
                    .build())
            .build();
    }
}
```

This declarative approach eliminates the need for manual region calculations:

```java
// Before: Manual calculation in action code
private void setupIconRegion(Region promptRegion) {
    Region iconRegion = new Region(promptRegion);
    iconRegion.adjust(3, 10, 30, 55);
    workingState.getClaudeIcon().setSearchRegions(iconRegion);
}

// After: Automatic calculation based on declaration
// No manual setup needed - just use the StateImage directly
ActionResult result = action.perform(findOptions, workingState.getClaudeIcon());
```

## Builder Methods

The `SearchRegionOnObject.Builder` provides multiple ways to configure adjustments:

### Individual Adjustments
```java
.xAdjust(10)      // Adjust x position
.yAdjust(20)      // Adjust y position  
.wAdjust(30)      // Adjust width
.hAdjust(40)      // Adjust height
```

### Combined Adjustments
```java
.adjustments(10, 20, 30, 40)  // x, y, width, height
```

### Absolute Dimensions
```java
.width(200)       // Fixed width
.height(100)      // Fixed height
.absoluteDimensions(200, 100)  // Both at once
```

## Cross-State References

SearchRegionOnObject supports referencing objects from different states:

```java
// In LoginState
StateImage loginButton = new StateImage.Builder()
    .addPatterns("login-button.png")
    .setName("LoginButton")
    .build();

// In DashboardState - reference login button location
StateImage notification = new StateImage.Builder()
    .addPatterns("notification.png")
    .setName("Notification")
    .setSearchRegionOnObject(new SearchRegionOnObject.Builder()
        .targetType(StateObject.Type.IMAGE)
        .targetState("Login")  // Different state
        .targetObject("LoginButton")
        .yAdjust(-50)  // Above the login button
        .build())
    .build();
```

## Integration with State-Aware Scheduling

The declarative approach works seamlessly with StateAwareScheduler:

```java
@Service
public class MonitoringService {
    
    private final StateAwareScheduler scheduler;
    private final WorkingState workingState;
    
    public void startMonitoring() {
        // Configure state checking
        StateCheckConfiguration config = new StateCheckConfiguration.Builder()
            .withRequiredStates(List.of("Prompt", "Working"))
            .build();
        
        // Schedule monitoring - regions are resolved automatically
        scheduler.scheduleWithStateCheck(
            executor,
            this::checkIcon,
            config,
            5, 2, TimeUnit.SECONDS
        );
    }
    
    private void checkIcon() {
        // The search region is automatically calculated based on
        // the current location of ClaudePrompt in PromptState
        ActionResult result = action.perform(
            new PatternFindOptions.Builder().build(),
            workingState.getClaudeIcon()
        );
    }
}
```

## Best Practices

1. **Use Descriptive Names**: Give clear names to both source and target objects
   ```java
   .targetObject("HeaderNavigationBar")  // Clear and specific
   ```

2. **Document Adjustments**: Comment on why specific adjustments are used
   ```java
   .adjustments(0, 50, 0, 0)  // Below header, same width
   ```

3. **Consider State Dependencies**: Ensure target states are loaded when needed
   ```java
   .withRequiredStates(List.of("SourceState", "TargetState"))
   ```

4. **Use Fixed Dimensions Sparingly**: Prefer relative sizing for responsiveness
   ```java
   // Good: Relative adjustment
   .wAdjust(20)  // Slightly wider than source
   
   // Use fixed only when necessary
   .width(100)   // Fixed width for consistent button size
   ```

## Migration from Manual Approach

To migrate existing code:

1. **Identify Manual Region Calculations**
   ```java
   // Old approach
   Region baseRegion = findResult.getRegion();
   Region searchRegion = new Region(
       baseRegion.x() + 10,
       baseRegion.y() + 50,
       baseRegion.w() + 20,
       baseRegion.h()
   );
   stateImage.setSearchRegions(searchRegion);
   ```

2. **Convert to Declarative Definition**
   ```java
   // New approach
   stateImage = new StateImage.Builder()
       .addPatterns("pattern.png")
       .setSearchRegionOnObject(new SearchRegionOnObject.Builder()
           .targetType(StateObject.Type.IMAGE)
           .targetState("BaseState")
           .targetObject("BaseImage")
           .adjustments(10, 50, 20, 0)
           .build())
       .build();
   ```

3. **Remove Manual Region Management**
   - Delete region calculation code
   - Remove region storage variables
   - Simplify action methods

## Troubleshooting

### Region Not Found
- Verify target state and object names match exactly
- Ensure target state is active when searching
- Check that target object has been found at least once

### Incorrect Region Position
- Log the resolved region for debugging:
  ```java
  ActionResult result = action.perform(findOptions, stateImage);
  log.info("Search region: {}", result.getSearchedRegion());
  ```
- Adjust the adjustment values incrementally
- Consider using visual feedback:
  ```java
  action.perform(new HighlightOptions.Builder().build(), stateImage);
  ```

### Performance Considerations
- Region resolution happens on each search
- Cache frequently used regions if performance is critical
- Consider using fixed regions for static layouts

## Summary

Declarative region definition in Brobot 1.1.0 provides:
- Cleaner, more maintainable code
- Dynamic adaptation to UI changes
- Better separation of concerns
- Seamless integration with state management

By defining regions declaratively, you create more robust automation that adapts to UI variations while keeping your action code focused on business logic rather than region calculations.