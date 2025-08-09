---
sidebar_position: 8
---

# Save Labeled Images

:::info Version Note
This tutorial has been updated for Brobot 1.1.0. The code now uses `StateNavigator` for state navigation instead of the older `StateTransitionsManagement` class.
:::

## The SaveLabeledImages Class

This class demonstrates how to capture and save labeled images. In a real application, this could be used to build training data or document different UI states.

```java
package com.example.basics.automation;

import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import static com.example.basics.StateNames.ISLAND;

@Slf4j
@Component
public class SaveLabeledImages {
    
    private StateNavigator stateNavigator;
    private GetNewIsland getNewIsland;
    private IslandRegion islandRegion;
    
    public SaveLabeledImages(StateNavigator stateNavigator,
                             GetNewIsland getNewIsland,
                             IslandRegion islandRegion) {
        this.stateNavigator = stateNavigator;
        this.getNewIsland = getNewIsland;
        this.islandRegion = islandRegion;
    }
    
    public void saveImages(int maxImages) {
        String directory = "labeledImages/";
        // Navigate to ISLAND state
        stateNavigator.openState(ISLAND);
        for (int i=0; i<maxImages; i++) {
            String newIslandType = getNewIsland.getIsland();
            log.info("text = {}", newIslandType);
            if (!newIslandType.isEmpty() && islandRegion.ensureRegionReady()) {
                // Capture the island image using the declarative region
                islandRegion.captureIsland();
                // In a real implementation, you would save the captured image to file here
                log.info("Would save captured island to: {}{}", directory, newIslandType);
            }
        }
    }
}
```

## Key Changes from Previous Versions

### State Navigation
- **Old (v1.0.7)**: Used `StateTransitionsManagement.openState(ISLAND)`
- **New (v1.1.0)**: Uses `StateNavigator.openState(ISLAND)` for navigation

### Image Utilities
- **Old**: Used `ImageUtils.saveRegionToFile()`
- **New**: Image capture is handled through the action framework

### Region Definition
- **Old**: Used `islandRegion.defined()` to check if region exists
- **New**: Uses `islandRegion.ensureRegionReady()` with declarative regions

## How It Works

1. **Loop through image captures**: The method iterates up to `maxImages` times
2. **Get new island**: Calls `getNewIsland.getIsland()` to navigate to a new island
3. **Ensure region is ready**: Checks that the capture region can be calculated
4. **Capture the image**: Uses the declarative region to capture the island
5. **Save (simulated)**: Logs where the image would be saved

## Run 'saveImages' from the Main Application

After initializing the states in your main application class, you can run the image capture:

```java
@Component
public class TutorialRunner implements CommandLineRunner {
    
    @Autowired
    private SaveLabeledImages saveLabeledImages;
    
    @Override
    public void run(String... args) throws Exception {
        // Save up to 100 labeled images
        saveLabeledImages.saveImages(100);
    }
}
```

## Benefits of the Declarative Approach

With the declarative region definition in `IslandState`, the capture region:
- Automatically adjusts based on the search button's location
- Handles different screen sizes and resolutions
- Reduces manual region calculations
- Makes the code more maintainable

That's it! Your application can now capture and label images based on dynamic UI elements.