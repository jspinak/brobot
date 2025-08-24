package com.example.basics.automation;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.text.TextFindOptions;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import com.example.basics.states.IslandState;
import com.example.basics.states.WorldState;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Basic automation examples demonstrating core Brobot features.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BasicAutomation {
    
    private final Action action;
    private final StateMemory stateMemory;
    private final StateNavigator stateNavigator;
    private final IslandState islandState;
    private final WorldState worldState;
    
    /**
     * Navigate through all states
     */
    public void navigateAllStates() {
        log.info("=== Navigate All States Example ===");
        
        // Start from HOME (initial state)
        log.info("Current state: {}", stateMemory.getActiveStateNamesAsString());
        
        // Go to WORLD
        if (stateNavigator.openState("WORLD")) {
            log.info("Successfully navigated to WORLD state");
            exploreWorld();
        }
        
        // Go to ISLAND
        if (stateNavigator.openState("ISLAND")) {
            log.info("Successfully navigated to ISLAND state");
            exploreIsland();
        }
        
        // Return to HOME
        if (stateNavigator.openState("HOME")) {
            log.info("Successfully returned to HOME state");
        }
    }
    
    /**
     * Explore the world map
     */
    private void exploreWorld() {
        log.info("Exploring world map...");
        
        // Find all islands
        PatternFindOptions findAllOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.8)
            .build();
        
        ObjectCollection castleCollection = new ObjectCollection.Builder()
            .withImages(worldState.getCastle())
            .build();
            
        ActionResult castles = action.perform(findAllOptions, castleCollection);
        
        ObjectCollection minesCollection = new ObjectCollection.Builder()
            .withImages(worldState.getMines())
            .build();
            
        ActionResult mines = action.perform(findAllOptions, minesCollection);
        
        int totalIslands = castles.getMatchList().size() + mines.getMatchList().size();
        log.info("Found {} islands on the world map", totalIslands);
    }
    
    /**
     * Explore an island and extract information
     */
    private void exploreIsland() {
        log.info("Exploring island...");
        
        // Read island name using OCR
        TextFindOptions textOptions = new TextFindOptions.Builder()
            .build();
        
        ObjectCollection nameCollection = new ObjectCollection.Builder()
            // .withStrings(islandState.getIslandNameRegion()) // Method doesn't exist
            .build();
            
        ActionResult nameResult = action.perform(textOptions, nameCollection);
        
        if (nameResult.isSuccess() && !nameResult.getText().isEmpty()) {
            log.info("Island name: {}", nameResult.getText());
        } else {
            log.warn("Could not read island name");
        }
        
        // Check resources
        checkResources();
    }
    
    /**
     * Check resource counters on island
     */
    private void checkResources() {
        log.info("Checking resources...");
        
        TextFindOptions textOptions = new TextFindOptions.Builder()
            .build();
        
        // Check gold
        ObjectCollection goldCollection = new ObjectCollection.Builder()
            // .withStrings(islandState.getGoldCounter()) // Method doesn't exist
            .build();
            
        ActionResult goldResult = action.perform(textOptions, goldCollection);
        
        if (goldResult.isSuccess()) {
            log.info("Gold: {}", goldResult.getText());
        }
        
        // Check wood
        ObjectCollection woodCollection = new ObjectCollection.Builder()
            // .withStrings(islandState.getWoodCounter()) // Method doesn't exist
            .build();
            
        ActionResult woodResult = action.perform(textOptions, woodCollection);
        
        if (woodResult.isSuccess()) {
            log.info("Wood: {}", woodResult.getText());
        }
        
        // Check stone
        ObjectCollection stoneCollection = new ObjectCollection.Builder()
            // .withStrings(islandState.getStoneCounter()) // Method doesn't exist
            .build();
            
        ActionResult stoneResult = action.perform(textOptions, stoneCollection);
        
        if (stoneResult.isSuccess()) {
            log.info("Stone: {}", stoneResult.getText());
        }
    }
    
    /**
     * Demonstrate state verification
     */
    public void verifyStates() {
        log.info("=== State Verification Example ===");
        
        // Get current state
        String currentState = stateMemory.getActiveStateNamesAsString();
        log.info("Current state: {}", currentState);
        
        // Check active states
        log.info("Active states: {}", stateMemory.getActiveStateNames());
    }
    
    /**
     * Demonstrate error recovery
     */
    public void errorRecoveryExample() {
        log.info("=== Error Recovery Example ===");
        
        // Try to go to a non-existent state
        if (!stateNavigator.openState("INVALID_STATE")) {
            log.warn("Failed to go to invalid state (expected)");
            
            // Recover by going to HOME
            log.info("Recovering by going to HOME state");
            if (stateNavigator.openState("HOME")) {
                log.info("Successfully recovered to HOME state");
            }
        }
    }
}