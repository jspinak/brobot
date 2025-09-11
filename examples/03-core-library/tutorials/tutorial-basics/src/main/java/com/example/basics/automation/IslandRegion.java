package com.example.basics.automation;

import org.springframework.stereotype.Component;

import com.example.basics.states.IslandState;
import com.example.basics.states.WorldState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

/**
 * Demonstrates declarative region definition.
 *
 * <p>With the new declarative approach using SearchRegionOnObject, we no longer need manual region
 * calculations. The island capture region is automatically calculated relative to the search
 * button's location whenever it's found.
 */
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
     * Ensure the search button has been found so the island capture region can be calculated
     * relative to it.
     *
     * @return true if the search button was found and the region is ready
     */
    public boolean ensureRegionReady() {
        // Find the search button to establish the reference point
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ObjectCollection searchButton =
                new ObjectCollection.Builder().withImages(world.getSearchButton()).build();

        ActionResult result = action.perform(findOptions, searchButton);

        // If the search button is found, the island capture region
        // will automatically be updated relative to its location
        return result.isSuccess();
    }

    /**
     * Capture an image of the island using the dynamically calculated region. The region is
     * automatically positioned relative to the search button.
     *
     * @return ActionResult containing the captured image
     */
    public ActionResult captureIsland() {
        // The search region for islandCapture is automatically calculated
        // relative to the search button's last known location
        PatternFindOptions captureOptions =
                new PatternFindOptions.Builder().setCaptureImage(true).build();

        ObjectCollection islandCapture =
                new ObjectCollection.Builder().withImages(island.getIslandCapture()).build();

        return action.perform(captureOptions, islandCapture);
    }
}
