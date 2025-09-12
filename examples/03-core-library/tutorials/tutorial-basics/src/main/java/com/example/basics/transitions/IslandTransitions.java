package com.example.basics.transitions;

import org.springframework.stereotype.Component;

import com.example.basics.states.IslandState;
import com.example.basics.states.WorldState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.ToTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Island state using the new unified annotation format.
 * Contains FromTransitions from other states TO Island,
 * and a ToTransition to verify arrival at Island.
 */
@TransitionSet(state = IslandState.class, description = "Island state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class IslandTransitions {
    
    private final IslandState islandState;
    private final WorldState worldState;
    private final Action action;
    
    /**
     * Navigate from World to Island by clicking on an island.
     * This transition occurs when the user selects a specific island from the world map.
     */
    @FromTransition(from = WorldState.class, priority = 1, description = "Navigate from World to Island")
    public boolean fromWorld() {
        log.info("Navigating from World to Island");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // Click on one of the islands in the world map
        return action.click(worldState.getCastle()).isSuccess() ||
               action.click(worldState.getFarms()).isSuccess() ||
               action.click(worldState.getMines()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the Island state.
     * Checks for the presence of island-specific elements.
     */
    @ToTransition(description = "Verify arrival at Island state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Island state");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }
        
        // Check for presence of island-specific elements
        boolean foundIslandName = action.find(islandState.getIslandName()).isSuccess();
        
        if (foundIslandName) {
            log.info("Successfully confirmed Island state is active");
            return true;
        } else {
            log.error("Failed to confirm Island state - island elements not found");
            return false;
        }
    }
}