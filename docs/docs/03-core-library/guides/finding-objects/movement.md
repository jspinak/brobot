---
sidebar_position: 3
---

# Motion

_Finding moving objects is available with version 1.0.6._

Brobot has functionality for finding moving objects. Movement is detected from three scenes, either
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

## Using MotionFindOptions

The new MotionFindOptions class provides a type-safe way to configure motion detection:

```java
MotionFindOptions motionOptions = new MotionFindOptions.Builder()
        .setMaxMovement(100)  // Maximum pixels an object can move between scenes
        .setMinArea(50)       // Minimum area to filter out noise
        .setMaxMatchesToActOn(10)  // Limit number of moving objects to track
        .build();
```

## Input Methods

The three different input methods for finding motion:

### Example 1: Using files  

```java
// Configure in application.yml:
// brobot:
//   core:
//     mock: true
//   screenshot:
//     save-history: true
// 
// Or in application.properties:
// brobot.mock=true
// brobot.screenshot.save-history=true
//
// Screenshots should be placed in the directory configured by
// brobot.screenshot.path (default: screenshots/)

MotionFindOptions motionOptions = new MotionFindOptions.Builder()
        .setMaxMovement(100)
        .setMinArea(50)
        .setMaxMatchesToActOn(10)
        .setSimilarity(0.7)  // Similarity threshold for matching objects across scenes
        .build();

ObjectCollection objectCollection = new ObjectCollection.Builder().build();

// Execute motion finding
@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(motionOptions, objectCollection);
```

### Example 2: Using Brobot images  

```java
// Ensure these are configured in your application properties:
// brobot.screenshot.save-history=true
// brobot.mock=true

MotionFindOptions motionOptions = new MotionFindOptions.Builder()
        .setMaxMovement(100)
        .setMinArea(50)
        .setMaxMatchesToActOn(10)
        .build();

// Provide scenes as Brobot images
ObjectCollection screenshots = new ObjectCollection.Builder()
        .withScenes(
            motionState.getScreen1(), 
            motionState.getScreen2(), 
            motionState.getScreen3()
        )
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(motionOptions, screenshots);
```

### Example 3: Using the screen  

```java
// Configure for live mode in application properties:
// brobot.mock=false

MotionFindOptions motionOptions = new MotionFindOptions.Builder()
        .setMaxMovement(100)
        .setMinArea(50)
        .setMaxMatchesToActOn(10)
        .setPauseBeforeBegin(1.0)  // Wait before capturing first scene
        .setPauseBetweenActions(0.5)  // Pause between scene captures
        .build();

ObjectCollection objectCollection = new ObjectCollection.Builder().build();

// The motion finding action will capture three screenshots from the screen
// with appropriate delays between captures
@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(motionOptions, objectCollection);
```

## Configuration Options

### Basic Options

The MotionFindOptions class provides these configuration options:

- **MaxMovement**: Limits the distance an object can move between scenes. By limiting the distance, you can eliminate false results caused by some movement in the same direction of the target object, but farther away. Set using `setMaxMovement(int)`.

- **MinArea**: Limits the results to objects of a certain size. Often there are small changes on screen that we don't want to follow, like the moving of grass or clouds. This is configured through `setMinArea(int)` in the base options.

- **MaxMatchesToActOn**: Limits the number of moving objects to track. Set using `setMaxMatchesToActOn(int)`.

- **Similarity**: The similarity threshold for matching objects across scenes. Set using `setSimilarity(double)`.

### Example with all options:

```java
MotionFindOptions advancedMotion = new MotionFindOptions.Builder()
        .setMaxMovement(150)     // Maximum pixels moved between scenes
        .setMinArea(100)         // Minimum area of moving objects
        .setMaxArea(5000)        // Maximum area of moving objects  
        .setMaxMatchesToActOn(5) // Track up to 5 moving objects
        .setSimilarity(0.75)     // 75% similarity required to match objects
        .setIllustrate(MotionFindOptions.Illustrate.YES)  // Save visual history
        .build();
```

## Working with Motion Results

```java
MotionFindOptions motionOptions = new MotionFindOptions.Builder()
        .setMaxMovement(100)
        .setMaxMatchesToActOn(10)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult motionResult = action.perform(motionOptions, objectCollection);

// Get all moving objects
List<Match> movingObjects = motionResult.getMatchList();
System.out.println("Found " + movingObjects.size() + " moving objects");

// Access scene-by-scene analysis
SceneAnalysisCollection scenes = motionResult.getSceneAnalysis();
if (scenes != null) {
    for (SceneAnalysis scene : scenes.getScenes()) {
        System.out.println("Scene " + scene.getSceneNumber() + 
                          " found " + scene.getMatches().size() + " objects");
        
        // Get matches for this scene
        for (Match match : scene.getMatches()) {
            System.out.println("  Object at " + match.getRegion());
        }
    }
}

// Check if motion detection was successful
if (motionResult.isSuccess()) {
    System.out.println("Motion detection successful!");
}
```

## Practical Example: Tracking Game Characters

```java
// Configure motion detection for tracking characters
MotionFindOptions trackCharacters = new MotionFindOptions.Builder()
        .setMaxMovement(200)    // Characters can move up to 200 pixels
        .setMinArea(500)        // Character sprites are at least 500 pixels
        .setMaxArea(10000)      // But no larger than 10000 pixels
        .setMaxMatchesToActOn(3) // Track up to 3 characters
        .setSimilarity(0.8)     // High similarity for character matching
        .build();

// Configure for mock mode in application properties:
// brobot.mock=true
// Place screenshots in the directory configured by brobot.screenshot.path

// Execute motion tracking
@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult characters = action.perform(
    trackCharacters, 
    new ObjectCollection.Builder().build()
);

// Process results
for (Match character : characters.getMatchList()) {
    System.out.println("Character moved to: " + character.getTarget());
}
```

## Tips for Better Motion Detection

1. **Scene Timing**: Allow sufficient time between scene captures for objects to move noticeably
2. **Lighting**: Consistent lighting between scenes improves detection accuracy
3. **Background**: Static backgrounds help distinguish moving objects
4. **Object Size**: Set appropriate minArea to filter out noise and small movements
5. **Movement Range**: Adjust maxMovement based on expected object speeds

## Integration with Other Actions

Motion finding can be combined with other actions using ActionChainOptions:

```java
// First detect motion, then click on moving objects
MotionFindOptions findMotion = new MotionFindOptions.Builder()
        .setMaxMovement(100)
        .setMaxMatchesToActOn(1)  // Find the best moving object
        .build();

ClickOptions clickMoving = new ClickOptions.Builder()
        .setNumberOfClicks(1)
        .build();

ActionChainOptions trackAndClick = new ActionChainOptions.Builder(findMotion)
        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
        .then(clickMoving)
        .build();

@Autowired
private Action action; // obtain from Spring context or dependency injection

ActionResult result = action.perform(trackAndClick, objectCollection);
```

For more information on find operations, see the other guides in this section:
- [Using Color](using-color.md)
- [Combining Finds](combining-finds.md)
- [Configuration Note](configuration-note.md)