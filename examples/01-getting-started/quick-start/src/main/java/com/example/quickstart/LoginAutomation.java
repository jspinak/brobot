package com.example.quickstart;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Demonstrates automatic navigation between states using StateNavigator.
 *
 * StateNavigator automatically figures out the path from current state
 * to target state and executes the necessary transitions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginAutomation {

    private final StateNavigator navigator;

    /**
     * Navigate to the dashboard state automatically.
     *
     * Brobot will:
     * 1. Determine current state by checking @IncomingTransition methods
     * 2. Find the path to DashboardState via @OutgoingTransition annotations
     * 3. Execute the necessary transitions automatically
     *
     * @return true if navigation was successful
     */
    public boolean navigateToDashboard() {
        log.info("Attempting automatic navigation to Dashboard state");

        // Brobot automatically figures out the path and executes transitions!
        boolean success = navigator.openState("Dashboard");

        if (success) {
            log.info("Successfully navigated to Dashboard");
        } else {
            log.error("Failed to navigate to Dashboard");
        }

        return success;
    }

    /**
     * Alternative method that navigates by state ID.
     * Note: State IDs are assigned at runtime by Brobot.
     * In practice, navigation by name (above) is more common.
     */
    public boolean navigateToDashboardById(Long stateId) {
        log.info("Navigating to Dashboard by state ID: {}", stateId);
        boolean success = navigator.openState(stateId);
        log.info("Navigation result: {}", success ? "SUCCESS" : "FAILED");
        return success;
    }
}
