# Movement Examples

This project demonstrates Brobot's movement actions including mouse movement, drag and drop, scrolling, and advanced gestures.

## Overview

Movement actions are essential for UI automation. This example covers:

- **Basic mouse movement** - Positioning with different patterns and speeds
- **Drag and drop** - Moving objects between locations
- **Scrolling** - Mouse wheel control for navigation
- **Advanced gestures** - Complex movements like drawing and swiping
- **Timing control** - Precise control over movement duration and pauses

## Project Structure

```
movement-examples/
├── src/main/java/com/example/movement/
│   ├── MovementApplication.java          # Spring Boot main
│   ├── MovementRunner.java               # Runs all examples
│   └── examples/
│       ├── BasicMovementExample.java     # Mouse movement basics
│       ├── DragDropExample.java          # Drag and drop operations
│       ├── ScrollingExample.java         # Mouse wheel scrolling
│       └── AdvancedMovementExample.java  # Complex movements
├── src/main/resources/
│   └── application.yml                   # Configuration
├── images/                               # Place test images here
│   ├── ui-elements/
│   ├── drag-drop/
│   ├── scrollable/
│   └── interactive/
├── build.gradle
└── settings.gradle
```

## Movement Actions

### 1. MouseMoveOptions

Move the mouse to specific locations with control over timing and pattern.

**Basic Movement:**
```java
MouseMoveOptions moveTo = new MouseMoveOptions.Builder()
    .setLocation(new Location(500, 300))
    .setMoveTime(1.0)  // 1 second movement
    .build();

action.perform(moveTo);
```

**Movement Patterns:**
```java
MouseMoveOptions smoothMove = new MouseMoveOptions.Builder()
    .setLocation(targetLocation)
    .setMoveTime(1.5)
    .setMovementPattern(MovementPattern.SMOOTH)  // Natural curve
    .build();

// Available patterns:
// - SMOOTH: Natural curved movement
// - LINEAR: Direct line movement  
// - SIGMOID: S-curve movement
// - RANDOM: Random variations
```

**Movement with Pauses:**
```java
MouseMoveOptions pausedMove = new MouseMoveOptions.Builder()
    .setLocation(destination)
    .setMoveTime(1.0)
    .setPauseBeforeBegin(0.5)  // Wait before moving
    .setPauseAfterEnd(0.5)     // Wait after arriving
    .build();
```

### 2. DragOptions

Drag and drop objects between locations.

**Simple Drag and Drop:**
```java
DragOptions dragDrop = new DragOptions.Builder()
    .setFromOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setToOptions(new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build())
    .setHoldTime(0.5)  // Hold before dragging
    .build();

action.perform(dragDrop, sourceObject, targetObject);
```

**Drag with Speed Control:**
```java
DragOptions slowDrag = new DragOptions.Builder()
    .setFromOptions(findOptions)
    .setToOptions(findOptions)
    .setDragSpeed(DragSpeed.SLOW)
    .build();

// Speed options: SLOW, NORMAL, FAST
```

### 3. Custom Drag Sequences

For precise control, use MouseDown/Move/Up sequences.

```java
// Press mouse button
MouseDownOptions press = new MouseDownOptions.Builder()
    .setPressOptions(new MousePressOptions.Builder()
        .setButton(MouseButton.LEFT)
        .setPauseAfterMouseDown(0.5)
        .build())
    .build();

// Move while holding
MouseMoveOptions drag = new MouseMoveOptions.Builder()
    .setLocation(dropLocation)
    .setMoveTime(2.0)  // Slow drag
    .build();

// Release mouse button
MouseUpOptions release = new MouseUpOptions.Builder()
    .setPressOptions(new MousePressOptions.Builder()
        .setButton(MouseButton.LEFT)
        .build())
    .build();

// Execute sequence
action.perform(press, dragHandle);
action.perform(drag);
action.perform(release);
```

### 4. ScrollMouseWheelOptions

Control mouse wheel scrolling.

**Basic Scrolling:**
```java
ScrollMouseWheelOptions scrollDown = new ScrollMouseWheelOptions.Builder()
    .setDirection(ScrollDirection.DOWN)
    .setClicks(5)  // Scroll 5 clicks
    .build();

action.perform(scrollDown, scrollableArea);
```

**Scroll with Timing:**
```java
ScrollMouseWheelOptions slowScroll = new ScrollMouseWheelOptions.Builder()
    .setDirection(ScrollDirection.UP)
    .setClicks(10)
    .setPauseBetweenScrolls(0.5)  // Slow scrolling
    .build();
```

**Directional Scrolling:**
```java
// Vertical scrolling
ScrollDirection.UP
ScrollDirection.DOWN

// Horizontal scrolling (if supported)
ScrollDirection.LEFT  
ScrollDirection.RIGHT
```

## Advanced Examples

### Circle Drawing Gesture

Draw a circle by calculating points and moving through them:

```java
// Calculate circle points
List<Location> points = calculateCirclePoints(center, radius, 36);

// Move to start
action.perform(new MouseMoveOptions.Builder()
    .setLocation(points.get(0))
    .build());

// Press mouse
action.perform(new MouseDownOptions.Builder()
    .setPressOptions(pressOptions)
    .build());

// Draw circle
for (Location point : points) {
    action.perform(new MouseMoveOptions.Builder()
        .setLocation(point)
        .setMoveTime(0.05)
        .build());
}

// Release mouse
action.perform(new MouseUpOptions.Builder()
    .setPressOptions(releaseOptions)
    .build());
```

### Swipe Gestures

Perform swipe movements:

```java
private void performSwipe(Location start, SwipeDirection direction, 
                         int distance, double duration) {
    Location end = calculateEndPoint(start, direction, distance);
    
    // Quick swipe movement
    action.perform(new MouseMoveOptions.Builder()
        .setLocation(start)
        .setMoveTime(0.2)
        .build());
        
    action.perform(new MouseMoveOptions.Builder()
        .setLocation(end)
        .setMoveTime(duration)
        .setMovementPattern(MovementPattern.LINEAR)
        .build());
}
```

### Hover Effects

Hover over UI elements:

```java
MouseMoveOptions hover = new MouseMoveOptions.Builder()
    .setLocation(buttonCenter)
    .setMoveTime(0.3)
    .setMovementPattern(MovementPattern.SMOOTH)
    .setPauseAfterEnd(1.0)  // Hover for 1 second
    .build();

action.perform(hover);
```

## Running the Examples

1. **In Mock Mode** (default):
   ```bash
   ./gradlew bootRun
   ```
   Simulates movements without actual mouse control.

2. **With Real UI**:
   - Add UI screenshots to `images/` directory
   - Set `brobot.core.mock: false` in `application.yml`
   - Run the application

3. **Individual Examples**:
   Modify `MovementRunner` to run specific examples.

## Best Practices

### 1. **Movement Timing**
- Use appropriate movement duration for natural interaction
- Add pauses for UI elements to respond
- Consider system performance when setting speeds

### 2. **Pattern Selection**
- **SMOOTH**: For natural user-like movements
- **LINEAR**: For precise, direct movements
- **SIGMOID**: For acceleration/deceleration effects
- **RANDOM**: To avoid detection patterns

### 3. **Error Handling**
```java
ActionResult result = action.perform(moveOptions);

if (!result.isSuccess()) {
    log.error("Movement failed: {}", result.getErrorMessage());
    // Handle failure
}
```

### 4. **Coordinate Systems**
- Always use absolute screen coordinates
- Account for multi-monitor setups
- Validate coordinates are within screen bounds

### 5. **Performance**
- Batch movements when possible
- Use appropriate movement speeds
- Consider UI response times

## Configuration

Key settings in `application.yml`:

```yaml
brobot:
  movement:
    default-move-time: 0.5    # Default movement duration
    default-pattern: SMOOTH   # Default movement pattern
    
  drag:
    default-hold-time: 0.5    # Hold time before drag
    default-speed: NORMAL     # Default drag speed
    
  scroll:
    default-clicks: 3         # Default scroll amount
    default-pause: 0.1        # Pause between scrolls
```

## Troubleshooting

### Movement Not Smooth
- Increase movement time
- Use SMOOTH movement pattern
- Check system performance

### Drag and Drop Fails
- Increase hold time
- Verify source and target are found
- Use slower drag speed

### Scrolling Issues
- Ensure mouse is over scrollable area
- Adjust pause between scrolls
- Try different scroll amounts

### Coordinates Off Screen
- Validate location coordinates
- Check screen resolution
- Handle multi-monitor setups

## Next Steps

1. Experiment with different movement patterns
2. Create custom gestures for your application
3. Combine movements with other actions
4. Optimize timing for your specific UI
5. Implement gesture recognition patterns

## Related Documentation

- [Action Configuration Overview](../01-overview.md)
- [Mouse Actions API](../05-reference.md#mouse-movement-options)
- [Complex Workflows](../08-complex-workflows.md)