# Motion Detection Examples

This project demonstrates Brobot's **motion detection** capabilities for finding and tracking moving objects on screen. Motion detection analyzes consecutive screen captures to identify objects that change position between frames.

> **⚠️ Note:** This project focuses on **detecting motion** (finding moving objects), not **controlling mouse movement**. For mouse control examples, see the mouse-movement example project.

## Overview

Motion detection is useful for:

- **Game automation** - Tracking moving characters, vehicles, or projectiles
- **UI animation detection** - Waiting for animations to complete or detecting animated elements
- **Dynamic content** - Finding content that changes position (sliding panels, scrolling lists)
- **Movement-based triggers** - Triggering actions when specific objects move
- **Object tracking** - Following objects as they move across the screen

## What is Motion Detection?

Motion detection works by:

1. **Capturing scene 1** - Taking a screenshot of the screen
2. **Waiting briefly** - Allowing time for movement to occur
3. **Capturing scene 2** - Taking a second screenshot
4. **Comparing scenes** - Identifying objects that moved between frames
5. **Returning matches** - Providing location and movement data for detected objects

## Project Structure

```
motion-detection-examples/
├── src/main/java/com/example/movement/
│   ├── MovementApplication.java          # Spring Boot main
│   ├── MovementRunner.java               # Runs all examples
│   └── examples/
│       └── SimpleMotionExample.java      # Motion detection examples
├── src/main/resources/
│   └── application.yml                   # Configuration
├── images/                               # (Empty - for future examples)
├── build.gradle
└── settings.gradle
```

## Motion Detection API

### MotionFindOptions

The `MotionFindOptions` class configures motion detection behavior.

**Basic Motion Detection:**
```java
MotionFindOptions motionOptions = new MotionFindOptions.Builder()
    .setMaxMovement(200)           // Max pixels an object can move
    .setPauseBeforeBegin(1.0)      // Wait 1 second before first capture
    .setMaxMatchesToActOn(10)      // Track up to 10 moving objects
    .build();

ActionResult result = action.perform(motionOptions);

// Process detected moving objects
for (Match match : result.getMatchList()) {
    log.info("Moving object detected at: {}", match.getTarget());
}
```

### Available Configuration Options

**MotionFindOptions supports:**

| Option | Type | Description | Example |
|--------|------|-------------|---------|
| `maxMovement` | int | Maximum pixel distance objects can move between frames | 200 |
| `maxMatchesToActOn` | int | Maximum number of moving objects to track | 10 |
| `pauseBeforeBegin` | double | Delay before capturing first scene (seconds) | 1.0 |
| `pauseBetweenActions` | double | Delay between scene captures (seconds) | 0.5 |

**Note:** The MotionFindOptions API is intentionally minimal. Advanced filtering (by size, shape, color) should be performed on the returned matches using standard ActionResult processing.

## Example: Basic Motion Detection

```java
@Component
public class SimpleMotionExample {

    @Autowired
    private Action action;

    public void basicMotionDetection() {
        log.info("=== Basic Motion Detection ===");

        // Configure motion detection
        MotionFindOptions motionOptions = new MotionFindOptions.Builder()
            .setMaxMovement(200)           // Objects moving up to 200 pixels
            .setPauseBeforeBegin(1.0)      // Wait 1 second before first capture
            .setMaxMatchesToActOn(10)      // Track up to 10 objects
            .build();

        // Perform motion detection
        ActionResult result = action.perform(motionOptions);

        // Process results
        if (result.isSuccess()) {
            log.info("Detected {} moving objects", result.getMatchList().size());

            for (Match match : result.getMatchList()) {
                log.info("Moving object at: {}", match.getTarget());
                log.info("  Region: {}", match.getRegion());
            }
        } else {
            log.info("No motion detected");
        }
    }
}
```

## Example: Movement Tracking

Track objects over multiple frames to analyze their movement patterns:

```java
public void trackMovement() {
    log.info("=== Movement Tracking ===");

    List<Match> previousMatches = new ArrayList<>();

    for (int frame = 0; frame < 5; frame++) {
        log.info("Capturing frame {}", frame);

        MotionFindOptions options = new MotionFindOptions.Builder()
            .setMaxMovement(150)
            .setPauseBeforeBegin(0.5)
            .build();

        ActionResult result = action.perform(options);

        if (result.isSuccess()) {
            List<Match> currentMatches = result.getMatchList();

            // Compare with previous frame
            if (!previousMatches.isEmpty()) {
                analyzeMovementPatterns(previousMatches, currentMatches);
            }

            previousMatches = new ArrayList<>(currentMatches);
        }
    }
}

private void analyzeMovementPatterns(List<Match> previous, List<Match> current) {
    // Analyze direction, speed, and patterns
    log.info("Objects moved from {} to {} positions",
             previous.size(), current.size());

    // Calculate average movement distance, direction, etc.
}
```

## Running the Examples

### 1. In Mock Mode (Default)

Mock mode simulates motion detection for testing without a GUI:

```bash
./gradlew bootRun
```

**What happens in mock mode:**
- Motion detection simulates finding moving objects
- Results are generated with realistic data
- No actual screen capturing occurs
- Perfect for CI/CD pipelines and testing

### 2. With Real Screen Capture

To detect actual motion on your screen:

**Step 1:** Disable mock mode in `application.yml`:
```yaml
brobot:
  core:
    mock: false  # Enable real screen capture
```

**Step 2:** Run the application:
```bash
./gradlew bootRun
```

**Step 3:** While the application is running, move windows or objects on your screen during the pause periods to create motion.

### 3. Custom Example

Modify `MovementRunner.java` to run specific detection scenarios:

```java
@Component
public class MovementRunner implements CommandLineRunner {

    @Autowired
    private SimpleMotionExample motionExample;

    @Override
    public void run(String... args) throws Exception {
        // Run only specific examples
        motionExample.basicMotionDetection();
        // motionExample.trackMovement();
    }
}
```

## Use Cases and Patterns

### 1. **Waiting for Animation to Complete**

```java
// Detect when UI elements stop moving
public boolean waitForAnimationComplete() {
    for (int attempt = 0; attempt < 10; attempt++) {
        MotionFindOptions options = new MotionFindOptions.Builder()
            .setMaxMovement(5)  // Very small movement threshold
            .setPauseBeforeBegin(0.2)
            .build();

        ActionResult result = action.perform(options);

        if (!result.isSuccess()) {
            // No motion detected - animation complete
            return true;
        }

        // Still moving, wait and retry
        Thread.sleep(200);
    }

    return false;  // Animation didn't stop
}
```

### 2. **Tracking Moving Game Characters**

```java
public void trackCharacter() {
    MotionFindOptions tracking = new MotionFindOptions.Builder()
        .setMaxMovement(300)  // Characters can move quickly
        .setMaxMatchesToActOn(1)  // Track only the player character
        .setPauseBeforeBegin(0.1)  // Fast tracking
        .build();

    ActionResult result = action.perform(tracking);

    if (result.isSuccess()) {
        Match character = result.getBestMatch().get();
        log.info("Character at: {}", character.getTarget());

        // React to character movement
        reactToMovement(character);
    }
}
```

### 3. **Detecting UI State Changes**

```java
public boolean detectStateChange() {
    // Detect when UI elements move (dialog appears, panel slides, etc.)
    MotionFindOptions stateDetection = new MotionFindOptions.Builder()
        .setMaxMovement(500)  // Large movements indicate major UI changes
        .setPauseBeforeBegin(0.5)
        .build();

    ActionResult result = action.perform(stateDetection);

    return result.isSuccess();  // True if UI changed
}
```

## Configuration

Key settings in `application.yml`:

```yaml
brobot:
  core:
    mock: true                    # Use mock mode for examples
    verbose: true                 # Detailed logging

  action:
    max-wait: 5                   # Maximum wait time for actions

  find:
    similarity: 0.8               # Image similarity threshold
```

**Motion Detection Specific:**
- The `pauseBeforeBegin` and `pauseBetweenActions` are set per MotionFindOptions instance
- No global motion detection configuration is needed

## Best Practices

### 1. **Choose Appropriate maxMovement**

```java
// Small movements (UI elements, buttons)
.setMaxMovement(50)

// Medium movements (windows, panels)
.setMaxMovement(200)

// Large movements (full-screen animations)
.setMaxMovement(500)
```

### 2. **Adjust Timing for Your Use Case**

```java
// Fast tracking (games, animations)
.setPauseBeforeBegin(0.1)
.setPauseAfterEnd(0.05)

// Slow/stable detection (UI changes)
.setPauseBeforeBegin(1.0)
.setPauseAfterEnd(0.5)
```

### 3. **Filter Results**

Since MotionFindOptions doesn't have built-in filtering, process results after detection:

```java
ActionResult result = action.perform(motionOptions);

// Filter by size
List<Match> largeObjects = result.getMatchList().stream()
    .filter(m -> m.getRegion().w() > 100 && m.getRegion().h() > 100)
    .collect(Collectors.toList());

// Filter by location
List<Match> topHalfOnly = result.getMatchList().stream()
    .filter(m -> m.getRegion().y() < screenHeight / 2)
    .collect(Collectors.toList());
```

### 4. **Handle No Motion Detected**

```java
ActionResult result = action.perform(motionOptions);

if (!result.isSuccess() || result.getMatchList().isEmpty()) {
    log.info("No motion detected - scene is stable");
    // This might be the desired outcome (waiting for animation to stop)
}
```

### 5. **Combine with Other Find Operations**

```java
// First, detect motion
ActionResult motionResult = action.perform(motionOptions);

// Then, use pattern matching on the moving objects
if (motionResult.isSuccess()) {
    for (Match movingObject : motionResult.getMatchList()) {
        // Analyze the moving object's image
        StateImage objectImage = movingObject.toStateImage();

        // Perform additional analysis
        // (pattern matching, color analysis, etc.)
    }
}
```

## Troubleshooting

### No Motion Detected

**Possible Causes:**
1. No actual motion occurring on screen
2. `maxMovement` threshold too small
3. Motion occurring outside the screen capture area
4. `pauseBeforeBegin` too short for motion to occur

**Solutions:**
- Increase `maxMovement` value
- Increase `pauseBeforeBegin` to allow more time for movement
- Ensure motion is happening during the detection window
- Disable mock mode if testing with real screen

### Too Many False Positives

**Possible Causes:**
1. `maxMovement` threshold too large
2. Background changes (window updates, cursor blinking)
3. Screen refresh artifacts

**Solutions:**
- Reduce `maxMovement` value
- Increase `pauseBeforeBegin` for more stable captures
- Filter results by size or location
- Use region-based detection (specify search areas)

### Performance Issues

**Possible Causes:**
1. Tracking too many objects
2. Large screen resolution
3. Too frequent captures

**Solutions:**
- Reduce `maxMatchesToActOn`
- Increase pause times between captures
- Use regions instead of full screen detection
- Optimize image processing settings

## Differences from Mouse Movement

This project focuses on **motion detection** (finding moving objects), not **mouse movement control**:

| Feature | Motion Detection (This Project) | Mouse Movement |
|---------|--------------------------------|----------------|
| **Purpose** | Find moving objects on screen | Control mouse cursor position |
| **Input** | Screen captures | Target coordinates |
| **Output** | List of moving objects | Mouse at new position |
| **API** | `MotionFindOptions` | `MouseMoveOptions` |
| **Use Case** | Tracking, waiting for animations | Clicking, hovering, gestures |

**For mouse movement control**, see the `mouse-movement` example project which covers:
- MouseMoveOptions - Moving cursor to locations
- DragOptions - Drag and drop operations
- ScrollOptions - Mouse wheel control

## Known Limitations

### Current API Limitations

The `MotionFindOptions` API currently has limited configuration options:

**What's Available:**
- ✅ `maxMovement` - Control movement detection threshold
- ✅ `maxMatchesToActOn` - Limit number of tracked objects
- ✅ Timing controls - `pauseBeforeBegin`, `pauseBetweenActions`

**Not Available (use post-processing instead):**
- ❌ `minArea` / `maxArea` - Filter by object size (filter results after detection)
- ❌ `similarity` - Color/pattern matching (use separate pattern find)
- ❌ `illustrate` - Visual debugging (use general Brobot debugging features)

### Workarounds

**Size filtering:**
```java
List<Match> filtered = result.getMatchList().stream()
    .filter(m -> m.size() >= minArea && m.size() <= maxArea)
    .collect(Collectors.toList());
```

**Pattern matching moving objects:**
```java
// First detect motion
ActionResult motion = action.perform(motionOptions);

// Then match patterns on moving objects
for (Match obj : motion.getMatchList()) {
    PatternFindOptions pattern = new PatternFindOptions.Builder()
        .setSimilarity(0.9)
        .build();
    // Use pattern matching on obj.getRegion()
}
```

## Next Steps

1. **Experiment with different scenarios**
   - Try different maxMovement values
   - Test with various timing configurations
   - Combine with other Brobot actions

2. **Add your own examples**
   - Create custom motion tracking logic
   - Implement application-specific motion handlers
   - Build motion-triggered workflows

3. **Integrate with your application**
   - Use motion detection in state transitions
   - Trigger actions based on movement patterns
   - Implement adaptive timing based on motion

4. **Provide feedback**
   - Report issues or suggestions
   - Contribute improvements
   - Share your use cases

## Related Documentation

- [Brobot Finding Objects Guide](../../../../docs/docs/03-core-library/guides/finding-objects/)
- [Motion Detection Documentation](../../../../docs/docs/03-core-library/guides/finding-objects/movement.md)
- [Action Configuration Reference](../05-reference.md)
- [State Management](../../../../docs/docs/02-core-concepts/states.md)

## Project Metadata

- **Brobot Version:** 1.1.0
- **Spring Boot Version:** 3.2.0
- **Java Version:** 21
- **Build Tool:** Gradle 8.14.2

---

**Last Updated:** 2025-10-16
**Example Type:** Motion Detection (Direct Action Pattern)
