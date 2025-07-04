---
sidebar_position: 3
---

# Motion

_Finding moving objects is available with version 1.0.6._

Brobot also has functionality for finding moving objects. Movement is detected from three scenes, either
provided to the function as files, as Brobot images, or taken from the screen. The reason three scenes is used is that 
changed pixels between two scenes does not tell us the direction of movement. Three scenes allows us to 
ascertain at which point the object started and where it ended.   

## Action Results

The results of a motion finding action are stored in the ActionResult object. The matches returned are those in the last
scene. The matches for each scene can be found within the ActionResult object, in the corresponding SceneAnalysis
objects (ActionResult -> SceneAnalysisCollection -> SceneAnalysis objects). If action history is
enabled, the analysis of each scene will be saved to the history folder. Below is an example of what would appear
in the history folder for a motion finding action:  

Objects are identified with a pink box. You may have to zoom in to see the boxes.   

In the first scene we see the three objects that were identified in their starting positions. 
Two people were correctly identified and one was not. The moving fire instead was identified instead of the 
third person because the third person moves behind a house in the last scene and confuses the algorithm.    
![motion_history.png](/img/motion/motion1.png)

The second scene identifies all three people correctly, including the one that moves into the forest. 
The game shows an outline of the person behind the trees, allowing the Brobot algorithm to follow it.  
![motion_history.png](/img/motion/motion2.png)

In the third scene, the third person disappears behind a house, and its absence is selected as the match.  
![motion_history.png](/img/motion/motion3.png)

## Input Methods with New API

The three different input methods for finding motion using the new ActionConfig API:

### Example 1: Using files  

```java
BrobotSettings.saveHistory = true;
BrobotSettings.mock = true;
BrobotSettings.screenshots.add("screen15.png");
BrobotSettings.screenshots.add("screen16.png");
BrobotSettings.screenshots.add("screen17.png");

MotionFindOptions motionOptions = new MotionFindOptions.Builder()
        .setMaxMatches(10)
        .setMinArea(50)
        .setMaxMovement(100)
        .setAnalysisMethod(MotionFindOptions.Method.OPTICAL_FLOW)
        .build();

ActionResult result = new ActionResult();
result.setActionConfig(motionOptions);

ActionInterface motionAction = actionService.getAction(motionOptions);
motionAction.perform(result, new ObjectCollection());
```

### Example 2: Using Brobot images  

```java
BrobotSettings.saveHistory = true;
BrobotSettings.mock = true;

MotionFindOptions motionOptions = new MotionFindOptions.Builder()
        .setMaxMatches(10)
        .setMinArea(50)
        .setMaxMovement(100)
        .build();

ObjectCollection screenshots = new ObjectCollection.Builder()
        .withScenes(
            motionState.getScreen1(), 
            motionState.getScreen2(), 
            motionState.getScreen3()
        )
        .build();

ActionResult result = new ActionResult();
result.setActionConfig(motionOptions);

ActionInterface motionAction = actionService.getAction(motionOptions);
motionAction.perform(result, screenshots);
```

### Example 3: Using the screen  

```java
BrobotSettings.mock = false;

MotionFindOptions motionOptions = new MotionFindOptions.Builder()
        .setMaxMatches(10)
        .setMinArea(50)
        .setMaxMovement(100)
        .setCaptureInterval(500)  // milliseconds between captures
        .setSceneCount(3)         // number of scenes to capture
        .build();

ActionResult result = new ActionResult();
result.setActionConfig(motionOptions);

ActionInterface motionAction = actionService.getAction(motionOptions);
motionAction.perform(result, new ObjectCollection());
```

## Configuration Options

### Basic Options

- **MinArea**: Limits the results to objects of a certain size. Often there are small changes on screen that we don't want to follow, like the moving of grass or clouds.
- **MaxMovement**: Limits the distance an object can move between scenes. By limiting the distance, you can eliminate false results caused by some movement in the same direction of the target object, but farther away.

### Advanced Options with MotionFindOptions

```java
MotionFindOptions advancedMotion = new MotionFindOptions.Builder()
        // Basic filtering
        .setMinArea(50)
        .setMaxArea(5000)
        .setMinMovement(10)      // Minimum pixels moved
        .setMaxMovement(100)     // Maximum pixels moved
        
        // Motion detection settings
        .setAnalysisMethod(MotionFindOptions.Method.OPTICAL_FLOW)
        .setMotionThreshold(0.3)
        .setDirectionFilter(MotionFindOptions.Direction.ANY)
        
        // Scene capture settings
        .setCaptureInterval(500)  // ms between captures
        .setSceneCount(3)         // number of scenes
        
        // Output settings
        .setTrackingMode(true)    // Track objects across scenes
        .setDrawMotionVectors(true) // Show motion vectors in history
        .build();
```

## Motion Analysis Methods

The new API supports different motion analysis methods:

```java
// Optical Flow - Best for smooth motion
MotionFindOptions opticalFlow = new MotionFindOptions.Builder()
        .setAnalysisMethod(MotionFindOptions.Method.OPTICAL_FLOW)
        .build();

// Frame Differencing - Fast, good for simple motion
MotionFindOptions frameDiff = new MotionFindOptions.Builder()
        .setAnalysisMethod(MotionFindOptions.Method.FRAME_DIFFERENCE)
        .build();

// Background Subtraction - Good for stationary camera
MotionFindOptions bgSubtract = new MotionFindOptions.Builder()
        .setAnalysisMethod(MotionFindOptions.Method.BACKGROUND_SUBTRACTION)
        .build();
```

## Directional Motion Detection

Filter motion by direction:

```java
// Only detect upward motion
MotionFindOptions upwardMotion = new MotionFindOptions.Builder()
        .setDirectionFilter(MotionFindOptions.Direction.UP)
        .setDirectionTolerance(30)  // degrees
        .build();

// Detect horizontal motion
MotionFindOptions horizontalMotion = new MotionFindOptions.Builder()
        .setDirectionFilter(MotionFindOptions.Direction.HORIZONTAL)
        .build();
```

## Working with Motion Results

```java
ActionResult motionResult = // ... perform motion detection

// Get all moving objects
List<Match> movingObjects = motionResult.getMatchList();

// Access scene-by-scene analysis
SceneAnalysisCollection scenes = motionResult.getSceneAnalysis();
for (SceneAnalysis scene : scenes.getScenes()) {
    System.out.println("Scene " + scene.getSceneNumber() + 
                      " found " + scene.getMatches().size() + " objects");
    
    // Get motion vectors for this scene
    for (MotionVector vector : scene.getMotionVectors()) {
        System.out.println("Object moved from " + vector.getStart() + 
                          " to " + vector.getEnd());
    }
}
```

## Migration from ActionOptions

If you're migrating from the old API:

**Old way:**
```java
ActionOptions findMotion = new ActionOptions.Builder()
        .setAction(ActionOptions.Action.FIND)
        .setFind(ActionOptions.Find.MOTION)
        .setMaxMatchesToActOn(10)
        .setMinArea(50)
        .setMaxMovement(100)
        .build();
```

**New way:**
```java
MotionFindOptions findMotion = new MotionFindOptions.Builder()
        .setMaxMatches(10)
        .setMinArea(50)
        .setMaxMovement(100)
        .build();
```

The new API provides more motion-specific options and better type safety.

For more information on the new ActionConfig API, see the [Migration Guide](/docs/core-library/guides/migration-guide).