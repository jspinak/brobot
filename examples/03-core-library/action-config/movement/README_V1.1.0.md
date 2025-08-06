# Movement Examples - Brobot v1.1.0

## Overview
This example demonstrates comprehensive movement control in Brobot v1.1.0 using the full range of available movement APIs.

## Available Movement APIs

### Mouse Movement
- **`MouseMoveOptions`** - Direct mouse movement control
  - `setMoveMouseDelay(float)` - Control movement speed (0.0 = instant, 1.0 = slow)
  - Full support for moving to locations, regions, and state objects

### Mouse Button Control  
- **`MouseDownOptions`** - Press and hold mouse buttons
- **`MouseUpOptions`** - Release mouse buttons
- Both support `MousePressOptions` for button selection and timing

### Scrolling
- **`ScrollOptions`** - Native mouse wheel scrolling
  - `setDirection(Direction)` - UP or DOWN
  - `setScrollSteps(int)` - Number of scroll wheel clicks
  - No need for keyboard workarounds!

## Examples Included

### BasicMovementExample
- Direct mouse movement with `MouseMoveOptions`
- Variable speed control
- Hover effects
- Complex movement patterns (spiral, figure-8)

### DragDropExample  
- Drag and drop with `DragOptions`
- Multi-stage drags
- Gesture drawing
- Combined with mouse button control

### ScrollingExample
- Native scrolling with `ScrollOptions`
- Smooth and stepped scrolling
- Scroll-to-find functionality
- Precision scrolling in specific areas

### AdvancedMovementExample
- Mouse button control (down/up)
- Complex gestures and drawings
- Combined movement workflows
- Integration with ConditionalActionChain

## Usage Examples

### Direct Mouse Movement
```java
MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
    .setMoveMouseDelay(0.5f)  // Medium speed
    .setPauseAfterEnd(1.0)    // Pause at destination
    .build();

ObjectCollection target = new ObjectCollection.Builder()
    .withLocations(new Location(500, 300))
    .build();

action.move(moveOptions, target);
```

### Native Scrolling
```java
ScrollOptions scrollDown = new ScrollOptions.Builder()
    .setDirection(ScrollOptions.Direction.DOWN)
    .setScrollSteps(5)
    .setPauseAfterEnd(0.5)
    .build();

action.scroll(scrollDown, targetArea);
```

### Mouse Button Control
```java
// Press and hold
MouseDownOptions pressOptions = new MouseDownOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .button(MouseButton.LEFT)
        .pauseAfterMouseDown(0.5)
        .build())
    .build();

action.mouseDown(pressOptions, location);

// Move while holding
action.move(moveOptions, newLocation);

// Release
MouseUpOptions releaseOptions = new MouseUpOptions.Builder()
    .setPressOptions(MousePressOptions.builder()
        .button(MouseButton.LEFT)
        .build())
    .build();

action.mouseUp(releaseOptions, newLocation);
```

### Hover Effects
```java
// True hover using mouse movement
MouseMoveOptions hoverOptions = new MouseMoveOptions.Builder()
    .setMoveMouseDelay(0.3f)
    .setPauseAfterEnd(2.0)  // Hover duration
    .build();

action.move(hoverOptions, buttonLocation);
```

## Advanced Features

### Variable Speed Movement
Control movement speed dynamically:
```java
// Lightning fast
.setMoveMouseDelay(0.05f)

// Smooth and visible  
.setMoveMouseDelay(0.5f)

// Very slow and deliberate
.setMoveMouseDelay(1.0f)
```

### Complex Patterns
Create sophisticated movement patterns:
- Spirals
- Figure-8 patterns
- Bezier curves (via multiple waypoints)
- Random movements within regions

### Precision Scrolling
Fine-grained scroll control:
```java
// Single step scrolling
.setScrollSteps(1)

// Page-like scrolling
.setScrollSteps(10)

// Smooth scrolling effect
for (int i = 0; i < 20; i++) {
    action.scroll(singleStepScroll, area);
}
```

## Best Practices

1. **Movement Speed**: Adjust `moveMouseDelay` based on context
   - Fast (0.1f) for utility movements
   - Medium (0.5f) for user-visible actions
   - Slow (0.8f+) for demonstrations

2. **Scrolling**: Use appropriate step counts
   - 1-3 steps for precise positioning
   - 5-10 steps for page scrolling
   - 20+ steps for quick navigation

3. **Mouse Buttons**: Always pair mouseDown with mouseUp
   - Use try-finally blocks to ensure cleanup
   - Consider timeouts for safety

4. **Hover Effects**: Use movement pause rather than click-with-0-clicks
   - More realistic hover behavior
   - Better tooltip triggering

## Running the Examples
```bash
./gradlew run
```

All examples demonstrate real v1.1.0 movement capabilities with full API access.