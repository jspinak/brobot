---
sidebar_position: 3
---

# Using Color

Color analysis in Brobot helps improve object detection accuracy by filtering matches based on color properties.

## Color-Based Finding

Brobot can use color information to:
- Filter false positives
- Identify specific UI elements by color
- Handle dynamic content with consistent colors

## Basic Color Matching

```java
ActionOptions colorFind = new ActionOptions.Builder()
    .setAction(ActionOptions.Action.FIND)
    .setColor(Color.RED)
    .setColorTolerance(50)
    .build();
```

## Advanced Color Analysis

### Color Histograms

Color histograms provide statistical analysis of color distribution in images.

### Color Classification

Brobot can classify regions based on dominant colors:
- Primary color detection
- Color clustering
- Multi-color pattern matching

## Examples

### Finding Red Buttons

```java
StateImageObject redButton = new StateImageObject.Builder()
    .withImage("button.png")
    .withColor(Color.RED)
    .build();
```

### Color-Based State Validation

```java
State colorState = new State.Builder("COLOR_STATE")
    .withColorValidation(region -> 
        region.getDominantColor().equals(Color.BLUE))
    .build();
```

## Best Practices

1. **Use color as a secondary filter** - Don't rely solely on color
2. **Account for display variations** - Colors may vary across monitors
3. **Test under different lighting** - Ambient light affects color perception
4. **Set appropriate tolerances** - Too strict = misses, too loose = false positives