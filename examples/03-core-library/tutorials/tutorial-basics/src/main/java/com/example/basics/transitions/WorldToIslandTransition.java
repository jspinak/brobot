package com.example.basics.transitions;

import com.example.basics.states.WorldState;
import com.example.basics.states.IslandState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.annotations.Transition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transition from WORLD to ISLAND state.
 * Demonstrates finding and clicking on dynamic content.
 */
@Transition(from = WorldState.class, to = IslandState.class)
@RequiredArgsConstructor
@Slf4j
public class WorldToIslandTransition {
    
    private final WorldState worldState;
    private final Action action;
    
    /**
     * Execute the transition
     */
    public boolean execute() {
        log.info("Transitioning from WORLD to ISLAND");
        
        // Find all islands
        PatternFindOptions findAllIslands = new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .setPauseBeforeBegin(1.0)  // Wait for world to load
            .build();
        
        // Try castle islands first
        ObjectCollection castleCollection = new ObjectCollection.Builder()
            .withImages(worldState.getCastleIsland())
            .withRegions(worldState.getMapRegion())  // Search within map area
            .build();
            
        ActionResult castleResult = action.perform(findAllIslands, castleCollection);
        
        if (castleResult.isSuccess()) {
            log.info("Found {} castle islands", castleResult.getMatchList().size());
            // Click on the first one
            Location clickLoc = new Location(castleResult.getMatchList().get(0).getRegion());
            ObjectCollection clickTarget = new ObjectCollection.Builder()
                .withLocations(clickLoc)
                .build();
            return action.perform(new ClickOptions.Builder().build(), clickTarget).isSuccess();
        }
        
        // Try mines islands
        ObjectCollection minesCollection = new ObjectCollection.Builder()
            .withImages(worldState.getMinesIsland())
            .withRegions(worldState.getMapRegion())
            .build();
            
        ActionResult minesResult = action.perform(findAllIslands, minesCollection);
        
        if (minesResult.isSuccess()) {
            log.info("Found {} mines islands", minesResult.getMatchList().size());
            Location clickLoc = new Location(minesResult.getMatchList().get(0).getRegion());
            ObjectCollection clickTarget = new ObjectCollection.Builder()
                .withLocations(clickLoc)
                .build();
            return action.perform(new ClickOptions.Builder().build(), clickTarget).isSuccess();
        }
        
        log.error("No islands found on the world map");
        return false;
    }
}