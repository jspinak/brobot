package com.example.basics.transitions;

import org.springframework.stereotype.Component;

import com.example.basics.states.HomeState;
import com.example.basics.states.WorldState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.ToTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Home state using the new unified annotation format.
 * Contains FromTransitions from other states TO Home,
 * and a ToTransition to verify arrival at Home.
 */
@TransitionSet(state = HomeState.class, description = "Home state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class HomeTransitions {
    
    private final HomeState homeState;
    private final WorldState worldState;
    private final Action action;
    
    /**
     * Navigate from World back to Home.
     * This transition brings the user back to the home screen.
     */
    @FromTransition(from = WorldState.class, priority = 1, description = "Navigate from World to Home")
    public boolean fromWorld() {
        log.info("Navigating from World to Home");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }
        // Assuming there's a home button or back navigation in WorldState
        return action.click(worldState.getBackButton()).isSuccess();
    }
    
    /**
     * Verify that we have successfully arrived at the Home state.
     * Checks for the presence of key home state elements.
     */
    @ToTransition(description = "Verify arrival at Home state", required = true)
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