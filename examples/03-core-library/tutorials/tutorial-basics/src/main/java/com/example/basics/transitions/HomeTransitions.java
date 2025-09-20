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
 * All transitions for the Home state. Contains: - An IncomingTransition to verify arrival at Home -
 * OutgoingTransitions that go FROM Home TO other states
 *
 * <p>This pattern creates better cohesion - only needs HomeState as dependency since all outgoing
 * transitions use Home's images.
 */
@TransitionSet(state = HomeState.class, description = "Home state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class HomeTransitions {

    private final HomeState homeState;
    private final Action action;

    /** Navigate from Home to World by clicking the world button. */
    @OutgoingTransition(
            to = WorldState.class,
            pathCost = 1,
            description = "Navigate from Home to World")
    public boolean toWorld() {
        log.info("Navigating from Home to World");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // Click the world button using Home's image
        return action.click(homeState.getToWorldButton()).isSuccess();
    }

    /** Navigate from Home to Island by clicking the island shortcut. */
    @OutgoingTransition(
            to = IslandState.class,
            pathCost = 2,
            description = "Navigate from Home to Island")
    public boolean toIsland() {
        log.info("Navigating from Home to Island");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // TODO: Add island shortcut to HomeState and implement navigation
        // For now, return true in mock mode only
        return true;
    }

    /**
     * Verify that we have successfully arrived at the Home state. Checks for the presence of key
     * home state elements.
     */
    @IncomingTransition(description = "Verify arrival at Home state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Home state");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }

        // Check for presence of home-specific elements
        boolean foundWorldButton = action.find(homeState.getToWorldButton()).isSuccess();
        boolean foundSearchButton = action.find(homeState.getSearchButton()).isSuccess();

        if (foundWorldButton || foundSearchButton) {
            log.info("Successfully confirmed Home state is active");
            return true;
        } else {
            log.error("Failed to confirm Home state - home elements not found");
            return false;
        }
    }
}
