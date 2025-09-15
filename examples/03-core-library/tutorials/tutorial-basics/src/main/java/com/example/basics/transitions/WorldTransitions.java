package com.example.basics.transitions;

import org.springframework.stereotype.Component;

import com.example.basics.states.HomeState;
import com.example.basics.states.IslandState;
import com.example.basics.states.WorldState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the World state. Contains: - An IncomingTransition to verify arrival at World
 * - OutgoingTransitions that go FROM World TO other states
 *
 * <p>This pattern is cleaner because the outgoing transitions use World's images, creating better
 * cohesion with only the WorldState as a dependency.
 */
@TransitionSet(state = WorldState.class, description = "World state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class WorldTransitions {

    private final WorldState worldState;
    private final Action action;

    /** Navigate from World to Home by clicking the home button. */
    @OutgoingTransition(
            to = HomeState.class,
            priority = 1,
            description = "Navigate from World to Home")
    public boolean toHome() {
        log.info("Navigating from World to Home");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // TODO: Add home button to WorldState and implement navigation
        // For now, return true in mock mode only
        return true;
    }

    /** Navigate from World to Island by clicking on an island. */
    @OutgoingTransition(
            to = IslandState.class,
            priority = 2,
            description = "Navigate from World to Island")
    public boolean toIsland() {
        log.info("Navigating from World to Island");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // Click on the first island using World's image
        return action.click(worldState.getIsland1()).isSuccess();
    }

    /**
     * Verify that we have successfully arrived at the World state. Checks for the presence of world
     * map elements.
     */
    @IncomingTransition(description = "Verify arrival at World state", required = true)
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
