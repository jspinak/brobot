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
    private MatchAdjustmentOptions adjustments; // Position and size adjustments (reuses existing class)
}
```

The `adjustments` field uses the standard `MatchAdjustmentOptions` class for consistency with other Brobot operations.

## Basic Usage

### Simple Relative Region

Define a search region relative to another StateImage:

```java
StateImage searchArea = new StateImage.Builder()
    .addPatterns("search-icon.png")
    .setName("SearchIcon")
    .setSearchRegionOnObject(SearchRegionOnObject.builder()
        .targetType(StateObject.Type.IMAGE)
        .targetStateName("MainMenu")
        .targetObjectName("MenuButton")
        .build())
    .build();
```

### With Adjustments

Apply position and size adjustments to the derived region:

```java
StateImage icon = new StateImage.Builder()
    .addPatterns("status-icon.png")
    .setName("StatusIcon")
    .setSearchRegionOnObject(SearchRegionOnObject.builder()
        .targetType(StateObject.Type.IMAGE)
        .targetStateName("Dashboard")
        .targetObjectName("HeaderBar")
        .adjustments(MatchAdjustmentOptions.builder()
            .addX(10)    // Move 10 pixels right
            .addY(-5)    // Move 5 pixels up
            .addW(50)    // Expand width by 50 pixels
            .addH(20)    // Expand height by 20 pixels
            .build())
        .build())
    .build();
```

### With Fixed Dimensions

Override the calculated dimensions with fixed values:

```java
StateImage button = new StateImage.Builder()
    .addPatterns("submit-button.png")
    .setName("SubmitButton")
    .setSearchRegionOnObject(SearchRegionOnObject.builder()
        .targetType(StateObject.Type.IMAGE)
        .targetStateName("Form")
        .targetObjectName("FormTitle")
        .adjustments(MatchAdjustmentOptions.builder()
            .addY(100)        // Move down 100 pixels
            .absoluteW(200)   // Fixed width of 200 pixels
            .absoluteH(50)    // Fixed height of 50 pixels
            .build())
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
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .targetType(StateObject.Type.IMAGE)
                    .targetStateName("Prompt")
                    .targetObjectName("ClaudePrompt")
                    .adjustments(MatchAdjustmentOptions.builder()
                            .addX(3)      // Slight offset to the right
                            .addY(10)     // Below the prompt
                            .addW(30)     // Wider search area
                            .addH(55)     // Taller search area
                            .build())
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

The `SearchRegionOnObject.builder()` provides a fluent API for configuration:

### Basic Structure
```java
SearchRegionOnObject.builder()
    .targetType(StateObject.Type.IMAGE)  // Required: Type of target
    .targetStateName("StateName")        // Required: State containing target
    .targetObjectName("ObjectName")      // Required: Name of target object
    .adjustments(...)                    // Optional: Position/size adjustments using MatchAdjustmentOptions
    .build()
```

### MatchAdjustmentOptions Builder
```java
.adjustments(MatchAdjustmentOptions.builder()
    .addX(10)         // Add to x position
    .addY(20)         // Add to y position  
    .addW(30)         // Add to width
    .addH(40)         // Add to height
    .absoluteW(200)   // Override with fixed width (optional)
    .absoluteH(100)   // Override with fixed height (optional)
    .targetPosition(Position.CENTER)  // Target position within region (optional)
    .targetOffset(new Location(5, 5)) // Additional offset (optional)
    .build())
```

### Key Differences from Standard Match Adjustments
- When used with SearchRegionOnObject, only position and dimension adjustments apply
- `targetPosition` and `targetOffset` are ignored for search region calculation
- Use negative values in `addX`/`addY` to move left/up
- Use `absoluteW`/`absoluteH` set to -1 (default) to not override dimensions

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
        .targetStateName("Login")  // Different state
        .targetObjectName("LoginButton")
        .yAdjust(-50)  // Above the login button
        .build())
    .build();
```

### How Cross-State Dependencies Work

When you define a cross-state dependency:

1. **Registration Phase**: When states are loaded, the `SearchRegionDependencyInitializer` automatically registers all dependencies with the `DynamicRegionResolver`.

2. **Runtime Resolution**: When a FIND operation succeeds:
   - The `FindPipeline` calls `updateDependentSearchRegions()`
   - All objects depending on the found object have their search regions updated
   - The updates apply the configured adjustments

3. **Automatic Updates**: Search regions are dynamically updated each time the target object is found in a new location.

Example flow:
```java
// 1. ClaudePrompt is found at location (100, 200)
// 2. ClaudeIcon's search region is automatically updated to (103, 210, width+30, height+55)
// 3. Next search for ClaudeIcon uses this updated region
```

## Immediate Search Region Updates

Starting with Brobot 1.1.0, search regions are updated immediately when dependencies are found, ensuring that dependent objects always use the most current region information.

### How Immediate Updates Work

When using declarative regions with dependencies:

1. **During Search**: StateImages are automatically ordered by dependencies
   - Images without dependencies are searched first
   - Dependent images are searched after their dependencies

2. **On Match Found**: As soon as a match is found:
   - All objects depending on the found object have their regions updated immediately
   - Remaining objects in the current search batch are updated before being searched
   - This ensures dependent objects always search in the correct location

3. **Example Flow**:
```java
// Given this setup:
StateImage prompt = new StateImage.Builder()
    .addPatterns("prompt.png")
    .setName("ClaudePrompt")
    .build();

StateImage icon = new StateImage.Builder()
    .addPatterns("icon.png")
    .setName("ClaudeIcon")
    .setSearchRegionOnObject(SearchRegionOnObject.builder()
        .targetType(StateObject.Type.IMAGE)
        .targetStateName("Prompt")
        .targetObjectName("ClaudePrompt")
        .adjustments(MatchAdjustmentOptions.builder()
            .addX(3).addY(10).addW(30).addH(55)
            .build())
        .build())
    .build();

// When searching for both:
action.perform(findOptions, prompt, icon);

// The execution order is:
// 1. Search for ClaudePrompt (no dependencies)
// 2. If ClaudePrompt found at (100, 200):
//    - ClaudeIcon's search region immediately updated to (103, 210, w+30, h+55)
// 3. Search for ClaudeIcon in the updated region
```

This immediate update mechanism ensures:
- Dependent objects are always searched in the correct location
- No wasted searches in incorrect regions
- Better performance through targeted searching

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
       .setSearchRegionOnObject(SearchRegionOnObject.builder()
           .targetType(StateObject.Type.IMAGE)
           .targetStateName("Base")  // @State removes "State" suffix from class name
           .targetObjectName("BaseImage")
           .adjustments(MatchAdjustmentOptions.builder()
               .addX(10)
               .addY(50)
               .addW(20)
               .addH(0)
               .build())
           .build())
       .build();
   ```

3. **Remove Manual Region Management**
   - Delete region calculation code
   - Remove region storage variables
   - Simplify action methods

## Implementation Architecture

The declarative region system consists of several key components:

### Core Components

1. **SearchRegionOnObject**: The configuration object that defines the dependency
   - Holds target state/object information
   - Contains adjustment and dimension settings
   - Attached to StateImages during state construction

2. **SearchRegionDependencyRegistry**: Tracks all dependencies
   - Maps source objects to their dependents
   - Provides lookup for dependent objects when sources are found
   - Thread-safe for concurrent access

3. **DynamicRegionResolver**: Resolves and updates regions
   - Calculates actual regions based on found objects
   - Updates dependent object search regions
   - Handles both same-state and cross-state dependencies

4. **SearchRegionDependencyInitializer**: Initializes the system
   - Listens for `StatesRegisteredEvent`
   - Collects all StateObjects with dependencies
   - Registers them with the DynamicRegionResolver

5. **FindPipeline Integration**: Triggers updates
   - Calls `updateDependentSearchRegions()` after successful finds
   - Ensures dependent regions are updated before next search

### Initialization Flow

```
Application Start
    ↓
States Loaded (@State classes instantiated)
    ↓
StatesRegisteredEvent Published
    ↓
SearchRegionDependencyInitializer Receives Event
    ↓
Collects All StateObjects with SearchRegionOnObject
    ↓
Registers Dependencies with DynamicRegionResolver
    ↓
System Ready for Dynamic Region Updates
```

### Runtime Flow (Updated in 1.1.0+)

```
FIND Operation Starts
    ↓
FindPipeline Orders StateImages by Dependencies
    ↓
Search for Non-Dependent Images First
    ↓
For Each Match Found:
    - Immediately Update All Dependent Search Regions
    - Clear Fixed Regions if Outside New Region
    - Update Remaining Images in Current Batch
    ↓
Continue Searching Dependent Images
    ↓
All Images Use Updated Regions
```

Key changes in 1.1.0+:
- **Dependency Ordering**: Images are sorted so dependencies are resolved first
- **Immediate Updates**: Regions update as soon as dependencies are found
- **Batch Processing**: Remaining images in a search batch are updated before being searched
- **Fixed Region Management**: Fixed regions are cleared proactively, not reactively

## Fixed Regions and Declarative Regions

### Interaction with Fixed Regions

Fixed regions are automatically set when a StateImage with `fixed=true` is found for the first time. This optimization improves performance by limiting future searches to the exact location where the pattern was previously found.

However, fixed regions can conflict with declarative regions when the UI layout changes. Brobot 1.1.0+ includes intelligent fixed region management:

#### Automatic Fixed Region Clearing

When a declarative region is calculated based on `SearchRegionOnObject`:

1. **Fixed Region Check**: The system checks if there's an existing fixed region
2. **Containment Test**: It verifies if the fixed region is within the new declarative region
3. **Automatic Clearing**: If the fixed region is outside the declarative region, it's automatically cleared
4. **Region Update**: The search region is updated to use the declarative region

**Important**: Fixed regions are cleared immediately when a declarative region is applied. This happens as soon as the dependency is found, not when the dependent object is searched.

This ensures that declarative regions take precedence when the UI layout changes:

```java
// Example scenario:
// 1. ClaudeIcon is found at (100, 200) and sets a fixed region
// 2. User moves the window, ClaudePrompt is now at (500, 300)
// 3. ClaudeIcon's declarative region is calculated as (503, 310, w+30, h+55)
// 4. Fixed region (100, 200) is outside the new region → automatically cleared
// 5. Next search uses the declarative region (503, 310, w+30, h+55)
```

#### Best Practices for Fixed Regions

1. **Don't Set ActionHistory with Declarative Regions**:
   ```java
   // ❌ WRONG: ActionHistory creates a fixed region that conflicts
   StateImage icon = new StateImage.Builder()
       .addPatterns("icon.png")
       .setSearchRegionOnObject(...)
       .withActionHistory(MockActionHistoryBuilder.Presets.reliable(region))
       .build();
   
   // ✅ CORRECT: Let declarative regions manage the search area
   StateImage icon = new StateImage.Builder()
       .addPatterns("icon.png")
       .setSearchRegionOnObject(...)
       .build();
   ```

2. **Use Fixed Regions for Static UI Elements**:
   ```java
   // Good for elements that never move
   StateImage logo = new StateImage.Builder()
       .addPatterns("company-logo.png")
       .setFixed(true)  // Will lock to first found location
       .build();
   ```

3. **Use Declarative Regions for Dynamic UI**:
   ```java
   // Good for elements that move relative to others
   StateImage button = new StateImage.Builder()
       .addPatterns("submit-button.png")
       .setSearchRegionOnObject(...)  // Adapts to UI changes
       .build();
   ```

#### Debugging Fixed Region Conflicts

Enable debug logging to see when fixed regions are cleared:

```properties
logging.level.io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver=DEBUG
```

Log output will show:
```
INFO: Fixed region R[100,200,50,50] for ClaudeIcon is outside new declarative region R[503,310,80,105], clearing fixed region
```

## Troubleshooting

### Region Not Found
- Verify target state and object names match exactly
- Ensure target state is active when searching
- Check that target object has been found at least once
- Enable logging: `logging.level.io.github.jspinak.brobot.action.internal.region=DEBUG`

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

### Dependencies Not Working
- Verify SearchRegionDependencyInitializer is being instantiated
- Check logs for "Registered search region dependency" messages
- Ensure Spring component scanning includes brobot packages
- Verify target object names match exactly (case-sensitive)

### Fixed Region Overriding Declarative Region
- Check if you're setting ActionHistory on the StateImage (remove it)
- Enable debug logging to see if fixed region is being cleared
- Verify the declarative region is being calculated correctly
- Consider manually resetting the fixed region:
  ```java
  stateImage.getPatterns().forEach(pattern -> {
      pattern.getSearchRegions().resetFixedRegion();
      pattern.setFixed(false);
  });
  ```

### Performance Considerations
- Region resolution happens on each search
- Dependencies are registered once at startup
- Updates only occur when source objects are found
- Consider using fixed regions for static layouts

## Complete Example: Real-World Usage

Here's a complete example showing all the new features working together:

```java
@State(initial = true)
public class PromptState {
    private final StateImage claudePrompt;
    
    public PromptState() {
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt.png")
            .setName("ClaudePrompt")
            .build();
    }
}

@State
public class WorkingState {
    private final StateImage claudeIcon;
    
    public WorkingState() {
        // Icon depends on prompt location
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1.png",
                        "working/claude-icon-2.png")
            .setName("ClaudeIcon")
            .setFixed(true)  // Will be cleared when prompt moves
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                .targetType(StateObject.Type.IMAGE)
                .targetStateName("Prompt")
                .targetObjectName("ClaudePrompt")
                .adjustments(MatchAdjustmentOptions.builder()
                    .addX(3).addY(10).addW(30).addH(55)
                    .build())
                .build())
            .build();
    }
}

// In your automation code:
public class ClaudeAutomator {
    @Autowired
    private Action action;
    
    @Autowired
    private PromptState promptState;
    
    @Autowired
    private WorkingState workingState;
    
    public void findElements() {
        // Search for both - dependency ordering happens automatically
        ActionResult result = action.perform(
            new PatternFindOptions.Builder().build(),
            promptState.getClaudePrompt(),
            workingState.getClaudeIcon()
        );
        
        // What happens internally:
        // 1. FindPipeline orders images: ClaudePrompt first (no dependencies)
        // 2. ClaudePrompt is found at (100, 200)
        // 3. ClaudeIcon's search region immediately updated to (103, 210, w+30, h+55)
        // 4. ClaudeIcon's fixed region (if any) is cleared
        // 5. ClaudeIcon is searched in the updated region
        // 6. If found, ClaudeIcon sets a new fixed region at the found location
    }
}
```

## Key Benefits of the New Implementation

1. **No Manual Region Management**: Dependencies are resolved automatically
2. **Immediate Updates**: Search regions update as soon as dependencies are found
3. **Intelligent Fixed Region Handling**: Fixed regions cleared when they conflict with declarative regions
4. **Optimized Search Order**: Dependencies are searched in the correct order
5. **Better Performance**: Fewer false matches due to targeted searching

## Summary

Declarative region definition in Brobot 1.1.0+ provides:
- Cleaner, more maintainable code
- Dynamic adaptation to UI changes
- Better separation of concerns
- Seamless integration with state management
- Immediate search region updates for optimal performance
- Intelligent fixed region management

By defining regions declaratively, you create more robust automation that adapts to UI variations while keeping your action code focused on business logic rather than region calculations.