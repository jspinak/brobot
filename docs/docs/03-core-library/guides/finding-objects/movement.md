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

The results of a Find.MOTION action are stored in the Matches object. The matches returned are those in the last
scene. The matches for each scene can be found within in the Matches object, in the corresponding SceneAnalysis
objects (Matches -> SceneAnalysisCollection -> SceneAnalysis objects). If action history is
enabled, the analysis of each scene will be saved to the history folder. Below is an example of what would appear
in the history folder for a Find.MOTION action:  

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

## Input Methods

The three different input methods for finding motion are shown in the examples below:

### Example 1: Using files  

        BrobotSettings.saveHistory = true;
        BrobotSettings.mock = true;
        BrobotSettings.screenshots.add("screen15.png");
        BrobotSettings.screenshots.add("screen16.png");
        BrobotSettings.screenshots.add("screen17.png");
        ActionOptions findMotion = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.MOTION)
                .setMaxMatchesToActOn(10)
                .setMinArea(50)
                .setMaxMovement(100)
                .build();
        action.perform(findMotion);

### Example 2: Using Brobot images  

        BrobotSettings.saveHistory = true;
        BrobotSettings.mock = true;
        ActionOptions findMotion = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.MOTION)
                .setMaxMatchesToActOn(10)
                .setMinArea(50)
                .setMaxMovement(100)
                .build();
        ObjectCollection screenshots = new ObjectCollection.Builder()
                .withScenes(motionState.getScreen1(), motionState.getScreen2(), motionState.getScreen3())
                .build();
        action.perform(findMotion, screenshots);

### Example 3: Using the screen  

        BrobotSettings.mock = false;
        ActionOptions findMotion = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.MOTION)
                .setMaxMatchesToActOn(10)
                .setMinArea(50)
                .setMaxMovement(100)
                .build();
        action.perform(findMotion);

## Configuration Options

_MinArea_ limits the results to objects of a certain size. Often there are small changes on screen that we don't 
want to follow, like the moving of grass or clouds.  

_MaxMovement_ limits the distance an object can move between scenes. By limiting the distance, you can eliminate
false results caused by some movement in the same direction of the target object, but farther away.  