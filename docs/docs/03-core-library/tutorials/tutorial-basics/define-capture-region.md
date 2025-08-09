---
sidebar_position: 6
---

# Define the Capture Region

:::info Version Note
This tutorial demonstrates Brobot 1.1.0's declarative region definition using `SearchRegionOnObject`. This approach eliminates manual region calculations by defining regions relative to other UI elements.
:::

We need to define the region where we will capture the image of the island. 
The island always appears near the search button, so we define the capture region
relative to the search button's location using the declarative approach.

## State Definition with Declarative Region

First, we define the island capture region declaratively in the `IslandState`:

```java
package com.example.basics.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import lombok.Getter;
import org.springframework.stereotype.Component;

@State
@Component
@Getter
public class IslandState {
    
    private final StateImage islandName;
    private final StateImage islandCapture;
    
    public IslandState() {
        islandName = new StateImage.Builder()
            .addPatterns("islandName")
            .build();
            
        // Define the island capture region relative to the search button
        // The island appears near the search button, so we define the capture
        // region relative to its location
        islandCapture = new StateImage.Builder()
            .setName("IslandCapture")
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                .targetType(StateObject.Type.IMAGE)
                .targetStateName("World")  // @State removes "State" suffix from class name
                .targetObjectName("searchButton")
                .adjustments(MatchAdjustmentOptions.builder()
                    .addX(-50)      // 50 pixels to the left of search button
                    .addY(-250)     // 250 pixels above search button
                    .absoluteW(200) // Fixed width of 200 pixels
                    .absoluteH(200) // Fixed height of 200 pixels
                    .build())
                .build())
            .build();
    }
}
```

## Using the Declarative Region

With the declarative approach, the region calculation is automatic:

```java
@Component
public class IslandRegion {
    
    private final Action action;
    private final WorldState world;
    private final IslandState island;
    
    public IslandRegion(Action action, WorldState world, IslandState island) {
        this.action = action;
        this.world = world;
        this.island = island;
    }
    
    /**
     * Ensure the search button has been found so the island capture region
     * can be calculated relative to it.
     * 
     * @return true if the search button was found and the region is ready
     */
    public boolean ensureRegionReady() {
        // Find the search button to establish the reference point
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .build();
        ObjectCollection searchButton = new ObjectCollection.Builder()
                .withImages(world.getSearchButton())
                .build();
        
        ActionResult result = action.perform(findOptions, searchButton);
        
        // If the search button is found, the island capture region
        // will automatically be updated relative to its location
        return result.isSuccess();
    }
    
    /**
     * Capture an image of the island using the dynamically calculated region.
     * The region is automatically positioned relative to the search button.
     * 
     * @return ActionResult containing the captured image
     */
    public ActionResult captureIsland() {
        // The search region for islandCapture is automatically calculated
        // relative to the search button's last known location
        PatternFindOptions captureOptions = new PatternFindOptions.Builder()
                .setCaptureImage(true)
                .build();
        
        ObjectCollection islandCapture = new ObjectCollection.Builder()
                .withImages(island.getIslandCapture())
                .build();
        
        return action.perform(captureOptions, islandCapture);
    }
}
```

## Key Benefits of Declarative Approach

1. **No Manual Calculations**: The region is automatically calculated based on the search button's location
2. **Dynamic Updates**: If the search button moves, the island capture region automatically adjusts
3. **Cleaner Code**: Region definition is part of the state definition, not scattered in action code
4. **Maintainable**: Changes to region positioning require only updating the adjustments in one place

## How It Works

1. When `WorldState.searchButton` is found, its location is recorded
2. The `islandCapture` StateImage has a `SearchRegionOnObject` that references the search button
3. When searching for `islandCapture`, Brobot automatically:
   - Takes the search button's last known location
   - Applies the adjustments (-50 pixels left, -250 pixels up)
   - Sets the dimensions (200x200 pixels)
   - Uses this calculated region for the search

## Learn More

For more details on declarative region definition, see the [Declarative Region Definition Guide](/docs/core-library/guides/declarative-region-definition).
