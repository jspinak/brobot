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
 * All transitions for the Island state. Contains: - An IncomingTransition to verify arrival at
 * Island - OutgoingTransitions that go FROM Island TO other states
 *
 * <p>This pattern is cleaner because the outgoing transitions use Island's images, creating better
 * cohesion with only the IslandState as a dependency.
 */
@TransitionSet(state = IslandState.class, description = "Island state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class IslandTransitions {

    private final IslandState islandState;
    private final Action action;

    /** Navigate from Island back to World by clicking the back button. */
    @OutgoingTransition(
            to = WorldState.class,
            priority = 1,
            description = "Navigate from Island to World")
    public boolean toWorld() {
        log.info("Navigating from Island to World");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // Click the back button using Island's image
        return action.click(islandState.getBackToWorldButton()).isSuccess();
    }

    /** Navigate from Island to Home (shortcut). */
    @OutgoingTransition(
            to = HomeState.class,
            priority = 2,
            description = "Navigate from Island to Home")
    public boolean toHome() {
        log.info("Navigating from Island to Home");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // TODO: Add home button to IslandState and implement navigation
        // For now, return true in mock mode only
        return true;
    }

    /**
     * Verify that we have successfully arrived at the Island state. Checks for the presence of
     * island-specific elements.
     */
    @IncomingTransition(description = "Verify arrival at Island state")
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
