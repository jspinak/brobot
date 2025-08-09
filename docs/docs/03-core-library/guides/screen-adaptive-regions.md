---
sidebar_position: 8
---

# Screen-Adaptive Regions

Create resolution-independent regions that adapt automatically to different screen sizes using the enhanced `RegionBuilder` with Position integration.

## Overview

The `RegionBuilder` class provides a fluent API for creating regions that adapt to different screen resolutions and sizes. By integrating with the `Position` class and `Positions.Name` enum, it offers intuitive, percentage-based positioning that works across different environments.

### Key Features

- **Resolution Independence**: Define regions using percentages instead of absolute pixels
- **Position Integration**: Leverage the Position class for precise percentage-based placement
- **Named Positions**: Use semantic positions like TOPLEFT, MIDDLEMIDDLE, BOTTOMRIGHT
- **Relative Positioning**: Position regions relative to other regions
- **Screen Scaling**: Automatically scale regions designed for specific resolutions
- **Fluent API**: Chain methods for readable region construction

## Basic Usage

### Creating a RegionBuilder

```java
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.RegionBuilder;
import io.github.jspinak.brobot.model.element.Position;
import static io.github.jspinak.brobot.model.element.Positions.Name.*;

// Access the builder through Region's static method
Region region = Region.builder()
    .withSize(200, 150)
    .build();
```

### Absolute Positioning

```java
// Simple region with absolute coordinates
Region absolute = Region.builder()
    .withRegion(100, 100, 200, 150)  // x, y, width, height
    .build();

// Position and size separately
Region separate = Region.builder()
    .withPosition(50, 75)
    .withSize(300, 200)
    .build();
```

## Position-Based Placement

### Using Position Objects

The `Position` class provides percentage-based coordinates (0.0 to 1.0) for resolution-independent placement:

```java
// Place region at 70% width, 30% height of screen
Region customPos = Region.builder()
    .withSize(200, 150)
    .withPosition(new Position(0.7, 0.3))
    .build();

// Center of screen using Position
Region centered = Region.builder()
    .withSize(400, 300)
    .withPosition(new Position(0.5, 0.5))
    .build();
```

### Named Positions

Use the `Positions.Name` enum for semantic positioning:

```java
// Quick placement using named positions
Region topRight = Region.builder()
    .withSize(300, 200)
    .withPosition(TOPRIGHT)
    .build();

Region bottomCenter = Region.builder()
    .withSize(250, 100)
    .withPosition(BOTTOMMIDDLE)
    .build();

// All available named positions:
// TOPLEFT, TOPMIDDLE, TOPRIGHT
// MIDDLELEFT, MIDDLEMIDDLE, MIDDLERIGHT
// BOTTOMLEFT, BOTTOMMIDDLE, BOTTOMRIGHT
```

### Convenience Methods

RegionBuilder provides helper methods for common positions:

```java
// Center on screen
Region dialog = Region.builder()
    .withSize(600, 400)
    .centerOnScreen()
    .build();

// Corner positions
Region topLeft = Region.builder()
    .withSize(100, 100)
    .topLeft()
    .build();

Region bottomRight = Region.builder()
    .withSize(120, 80)
    .bottomRight()
    .build();

// Edge positions
Region topBar = Region.builder()
    .withScreenPercentageSize(1.0, 0.1)  // Full width, 10% height
    .topCenter()
    .build();

Region sidebar = Region.builder()
    .withScreenPercentageSize(0.2, 1.0)  // 20% width, full height
    .leftCenter()
    .build();
```

## Screen Percentage Sizing

Define regions as percentages of screen dimensions:

```java
// Size as percentage of screen
Region halfScreen = Region.builder()
    .withScreenPercentageSize(0.5, 0.5)  // 50% width, 50% height
    .centerOnScreen()
    .build();

// Position and size using percentages
Region searchArea = Region.builder()
    .withScreenPercentage(0.1, 0.1, 0.8, 0.3)  // x%, y%, width%, height%
    .build();

// Just percentage size (position separately)
Region percentSized = Region.builder()
    .withScreenPercentageSize(0.3, 0.4)
    .topRight()
    .build();
```

## Relative Positioning

Position regions relative to other regions:

```java
Region mainWindow = Region.builder()
    .withSize(800, 600)
    .centerOnScreen()
    .build();

// Position tooltip above the main window
Region tooltip = Region.builder()
    .withSize(200, 50)
    .positionRelativeTo(mainWindow, TOPMIDDLE)
    .adjustY(-10)  // 10px gap above
    .build();

// Position button in bottom-right of window
Region button = Region.builder()
    .withSize(100, 40)
    .positionRelativeTo(mainWindow, BOTTOMRIGHT)
    .adjustX(-10)  // 10px margin from right
    .adjustY(-10)  // 10px margin from bottom
    .build();

// Custom relative position (25% from left, 75% from top)
Region marker = Region.builder()
    .withSize(20, 20)
    .positionRelativeTo(mainWindow, new Position(0.25, 0.75))
    .build();
```

## Screen Scaling and Adaptation

### Scaling from Base Resolution

Design for a specific resolution and automatically scale to the current screen:

```java
// Design for 1920x1080, scale to current resolution
Region scaledButton = Region.builder()
    .withBaseScreenSize(1920, 1080)  // Original design resolution
    .withBaseRegion(1800, 1000, 100, 50)  // Original button position
    .adjustToCurrentScreen()  // Automatically scale
    .build();

// Complex scaling with adjustments
Region scaledDialog = Region.builder()
    .withBaseScreenSize(1920, 1080)
    .withBaseRegion(560, 240, 800, 600)
    .adjustToCurrentScreen()
    .expand(10)  // Add 10px padding after scaling
    .build();
```

### Predefined Screen Regions

Quick methods for common screen divisions:

```java
// Full screen
Region fullScreen = Region.builder()
    .fullScreen()
    .build();

// Half regions
Region topHalf = Region.builder()
    .topHalf()
    .build();

Region bottomHalf = Region.builder()
    .bottomHalf()
    .build();

Region leftHalf = Region.builder()
    .leftHalf()
    .build();

Region rightHalf = Region.builder()
    .rightHalf()
    .build();
```

## Adjustments and Modifications

### Fine-tuning Position and Size

```java
// Adjust individual dimensions
Region adjusted = Region.builder()
    .withRegion(100, 100, 200, 150)
    .adjustX(10)      // Move 10px right
    .adjustY(-20)     // Move 20px up
    .adjustWidth(50)  // Increase width by 50px
    .adjustHeight(30) // Increase height by 30px
    .build();

// Adjust all at once
Region bulkAdjusted = Region.builder()
    .withRegion(100, 100, 200, 150)
    .adjustBy(10, -5, 20, 20)  // x, y, width, height adjustments
    .build();
```

### Expanding and Contracting

```java
// Expand region by pixels on all sides
Region expanded = Region.builder()
    .withRegion(100, 100, 200, 150)
    .expand(20)  // Grows by 20px on all sides
    .build();
// Result: x=80, y=80, width=240, height=190

// Contract region (negative expansion)
Region contracted = Region.builder()
    .withRegion(100, 100, 200, 150)
    .expand(-10)  // Shrinks by 10px on all sides
    .build();
```

### Working with Existing Regions

```java
Region original = new Region(100, 100, 200, 150);

// Modify existing region
Region modified = Region.builder()
    .fromRegion(original)
    .adjustWidth(50)
    .adjustHeight(50)
    .build();

// Create region relative to existing
Region adjacent = Region.builder()
    .fromRegion(original)
    .adjustX(original.getW() + 10)  // Position to the right
    .build();
```

## Custom Anchoring

Control how regions are positioned using anchor points:

```java
// Default anchor is TOPLEFT (0.0, 0.0)
Region defaultAnchor = Region.builder()
    .withSize(100, 100)
    .withPosition(500, 300)  // Top-left corner at (500, 300)
    .build();

// Center anchor - position specifies center point
Region centerAnchor = Region.builder()
    .withSize(100, 100)
    .withAnchor(MIDDLEMIDDLE)
    .withPosition(500, 300)  // Center at (500, 300)
    .build();

// Custom anchor point (75% width, 25% height)
Region customAnchor = Region.builder()
    .withSize(100, 100)
    .withAnchor(new Position(0.75, 0.25))
    .withPosition(500, 300)  // Anchor point at (500, 300)
    .build();
```

## Constraints

### Screen Boundary Constraints

```java
// Constrain to screen boundaries (default: true)
Region constrained = Region.builder()
    .withPosition(10000, 10000)  // Way off screen
    .withSize(200, 150)
    .constrainToScreen(true)
    .build();
// Will be repositioned to stay within screen bounds

// Allow off-screen positioning
Region offScreen = Region.builder()
    .withPosition(-50, -50)
    .withSize(200, 150)
    .constrainToScreen(false)
    .build();
// Maintains negative coordinates
```

### Aspect Ratio Maintenance

```java
// Maintain aspect ratio when resizing
Region aspectLocked = Region.builder()
    .withSize(400, 300)  // 4:3 ratio
    .maintainAspectRatio(true)
    .withWidth(600)  // Height automatically becomes 450
    .build();
```

## Complex Layout Examples

### Dashboard Layout

```java
// Create a dashboard with header, sidebar, and content area
Region header = Region.builder()
    .withScreenPercentageSize(1.0, 0.1)  // Full width, 10% height
    .topCenter()
    .build();

Region sidebar = Region.builder()
    .withScreenPercentageSize(0.2, 0.9)  // 20% width, 90% height
    .positionRelativeTo(header, BOTTOMLEFT)
    .build();

Region content = Region.builder()
    .positionRelativeTo(sidebar, MIDDLERIGHT)
    .withScreenPercentageSize(0.8, 0.9)
    .build();

Region footer = Region.builder()
    .withScreenPercentageSize(1.0, 0.05)
    .bottomCenter()
    .build();
```

### Modal Dialog with Overlay

```java
// Semi-transparent overlay
Region overlay = Region.builder()
    .fullScreen()
    .build();

// Centered modal dialog
Region modal = Region.builder()
    .withScreenPercentageSize(0.5, 0.6)  // 50% width, 60% height
    .centerOnScreen()
    .build();

// Close button in modal's top-right
Region closeButton = Region.builder()
    .withSize(30, 30)
    .positionRelativeTo(modal, TOPRIGHT)
    .adjustX(-10)
    .adjustY(10)
    .build();
```

### Grid Layout

```java
// Create a 3x3 grid of regions
List<Region> grid = new ArrayList<>();
double cellWidth = 0.33;
double cellHeight = 0.33;

for (int row = 0; row < 3; row++) {
    for (int col = 0; col < 3; col++) {
        Region cell = Region.builder()
            .withScreenPercentage(
                col * cellWidth,      // x position
                row * cellHeight,     // y position
                cellWidth * 0.9,      // width with gap
                cellHeight * 0.9      // height with gap
            )
            .build();
        grid.add(cell);
    }
}
```

## Best Practices

### 1. Use Percentages for Cross-Platform Compatibility

```java
// Good: Percentage-based, works on any screen
Region adaptive = Region.builder()
    .withScreenPercentageSize(0.3, 0.4)
    .centerOnScreen()
    .build();

// Avoid: Fixed pixels may not work on different screens
Region fixed = Region.builder()
    .withRegion(640, 360, 640, 480)
    .build();
```

### 2. Design for a Base Resolution

```java
// Design for common resolution, then scale
Region scalable = Region.builder()
    .withBaseScreenSize(1920, 1080)
    .withBaseRegion(500, 300, 400, 200)
    .adjustToCurrentScreen()
    .build();
```

### 3. Use Named Positions for Clarity

```java
// Clear intent
Region notification = Region.builder()
    .withSize(300, 100)
    .topRight()
    .adjustX(-20)
    .adjustY(20)
    .build();

// Less clear
Region notification2 = Region.builder()
    .withSize(300, 100)
    .withAnchor(new Position(1.0, 0.0))
    .build();
```

### 4. Combine Relative and Absolute Positioning

```java
// Start with relative, fine-tune with absolute
Region precise = Region.builder()
    .withScreenPercentageSize(0.25, 0.2)
    .centerOnScreen()
    .adjustX(-10)  // Fine adjustments
    .adjustY(5)
    .build();
```

### 5. Test on Multiple Resolutions

```java
// Create test suite for different resolutions
int[][] testResolutions = {
    {1920, 1080}, {1366, 768}, {2560, 1440}, {3840, 2160}
};

for (int[] res : testResolutions) {
    Region testRegion = Region.builder()
        .withBaseScreenSize(1920, 1080)
        .withBaseRegion(100, 100, 200, 150)
        .adjustToCurrentScreen()  // Will scale based on actual screen
        .build();
    
    // Verify region stays within bounds and maintains proportions
    assert testRegion.isDefined();
    assert testRegion.getW() > 0 && testRegion.getH() > 0;
}
```

## Integration with Brobot Actions

### Using RegionBuilder in State Definitions

```java
@Component
public class LoginState implements State {
    
    private final Region usernameField = Region.builder()
        .withScreenPercentage(0.4, 0.4, 0.2, 0.05)
        .build();
    
    private final Region passwordField = Region.builder()
        .positionRelativeTo(usernameField, BOTTOMMIDDLE)
        .withSize(usernameField.getW(), usernameField.getH())
        .adjustY(20)
        .build();
    
    private final Region loginButton = Region.builder()
        .positionRelativeTo(passwordField, BOTTOMMIDDLE)
        .withSize(100, 40)
        .adjustY(30)
        .build();
    
    // Use in actions
    public void login(String username, String password) {
        action.perform(new ObjectCollection.Builder()
            .withRegions(usernameField)
            .click()
            .type(username)
            .build());
        
        action.perform(new ObjectCollection.Builder()
            .withRegions(passwordField)
            .click()
            .type(password)
            .build());
        
        action.perform(new ObjectCollection.Builder()
            .withRegions(loginButton)
            .click()
            .build());
    }
}
```

### Dynamic Region Creation

```java
public class AdaptiveSearch {
    
    public Region getSearchAreaForScreen() {
        // Detect current screen size and create appropriate search area
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        if (screenSize.width > 2000) {
            // Large screen: search in center third
            return Region.builder()
                .withScreenPercentage(0.33, 0.33, 0.34, 0.34)
                .build();
        } else if (screenSize.width > 1400) {
            // Medium screen: search in center half
            return Region.builder()
                .withScreenPercentageSize(0.5, 0.5)
                .centerOnScreen()
                .build();
        } else {
            // Small screen: search most of screen
            return Region.builder()
                .withScreenPercentage(0.1, 0.1, 0.8, 0.8)
                .build();
        }
    }
}
```

## Troubleshooting

### Region Outside Screen Bounds

```java
// Problem: Region appears cut off
Region problem = Region.builder()
    .withPosition(1800, 1000)
    .withSize(300, 200)
    .build();

// Solution: Enable screen constraints
Region solution = Region.builder()
    .withPosition(1800, 1000)
    .withSize(300, 200)
    .constrainToScreen(true)  // Ensures region stays on screen
    .build();
```

### Scaling Issues

```java
// Problem: Region too small after scaling
Region tooSmall = Region.builder()
    .withBaseScreenSize(3840, 2160)  // 4K design
    .withBaseRegion(100, 100, 50, 30)  // Small on 4K
    .adjustToCurrentScreen()  // May be tiny on 1080p
    .build();

// Solution: Use minimum sizes
Region minSize = Region.builder()
    .withBaseScreenSize(3840, 2160)
    .withBaseRegion(100, 100, 50, 30)
    .adjustToCurrentScreen()
    .build();

// Ensure minimum dimensions
if (minSize.getW() < 100) minSize.setW(100);
if (minSize.getH() < 60) minSize.setH(60);
```

### Position Calculation Errors

```java
// Problem: Unexpected position with custom anchor
Region unexpected = Region.builder()
    .withSize(100, 100)
    .withAnchor(MIDDLEMIDDLE)  // Changes reference point
    .withPosition(0, 0)  // Now refers to center, not top-left
    .build();

// Solution: Understand anchor behavior
// With MIDDLEMIDDLE anchor, position (0,0) means center at (0,0)
// So actual top-left will be at (-50, -50)
```

## Summary

The enhanced RegionBuilder with Position integration provides:

- **Resolution Independence**: Define once, run anywhere
- **Intuitive API**: Named positions and percentage-based sizing
- **Flexibility**: Combine relative and absolute positioning
- **Maintainability**: Clear, readable region definitions
- **Adaptability**: Automatic scaling between resolutions

Use RegionBuilder to create robust, screen-adaptive automation that works consistently across different environments and display configurations.