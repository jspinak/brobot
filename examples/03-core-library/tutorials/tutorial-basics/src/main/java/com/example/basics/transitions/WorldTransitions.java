package com.example.basics.transitions;

import org.springframework.stereotype.Component;

import com.example.basics.states.HomeState;
import com.example.basics.states.WorldState;
import com.example.basics.states.IslandState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.ToTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the World state using the new unified annotation format.
 * Contains FromTransitions from other states TO World,
 * and a ToTransition to verify arrival at World.
 */
@TransitionSet(state = WorldState.class, description = "World state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class WorldTransitions {
    
    private final HomeState homeState;
    private final WorldState worldState;
    private final IslandState islandState;
    private final Action action;
    
    /**
     * Navigate from Home to World by clicking the world button.
     * This is the primary entry point to the World state.
     */
    @FromTransition(from = HomeState.class, priority = 1, description = "Navigate from Home to World")
    public boolean fromHome() {
        log.info("Navigating from Home to World");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
    
    /**
     * Navigate from Island back to World.
     * Returns from a specific island to the world map.
     */
    @FromTransition(from = IslandState.class, priority = 2, description = "Navigate from Island to World")
    public boolean fromIsland() {
        log.info("Navigating from Island to World");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // Assuming there's a back button or world map button in IslandState
        return action.click(islandState.getBackToWorldButton()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the World state.
     * Checks for the presence of world map elements.
     */
    @ToTransition(description = "Verify arrival at World state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at World state");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        // Check for presence of world-specific elements
        boolean foundIsland = action.find(worldState.getIsland1()).isSuccess();
        
        if (foundIsland) {
            log.info("Successfully confirmed World state is active");
            return true;
        } else {
            log.error("Failed to confirm World state - world elements not found");
            return false;
        }
    }
}