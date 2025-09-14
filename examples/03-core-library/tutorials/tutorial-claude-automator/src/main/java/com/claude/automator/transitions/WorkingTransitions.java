package com.claude.automator.transitions;

import org.springframework.stereotype.Component;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Working state.
 * Contains:
 * - An IncomingTransition to verify arrival at Working
 * - OutgoingTransitions that go FROM Working TO other states
 *
 * This pattern is cleaner because the outgoing transitions use Working's images,
 * creating better cohesion with only the WorkingState as a dependency.
 */
@TransitionSet(state = WorkingState.class, description = "Claude Working state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkingTransitions {

    private final WorkingState workingState;
    private final Action action;

    /**
     * Navigate from Working to Prompt when work is complete.
     * This occurs when Claude finishes processing and returns to the prompt.
     */
    @OutgoingTransition(
            to = PromptState.class,
            priority = 1,
            description = "Navigate from Working to Prompt")
    public boolean toPrompt() {
        try {
            log.info("Navigating from Working to Prompt");

            // In mock mode, just return true for testing
            if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
                log.info("Mock mode: simulating successful navigation");
                return true;
            }

            // Wait for work to complete
            // The working indicator should disappear when Claude is done
            // We might need to wait or check for the absence of the working indicator
            int maxWaitTime = 30; // seconds
            int checkInterval = 1; // second

            for (int i = 0; i < maxWaitTime; i++) {
                if (!action.find(workingState.getWorkingIndicator()).isSuccess()) {
                    log.info("Working indicator disappeared, Claude has finished processing");
                    return true;
                }
                try {
                    Thread.sleep(checkInterval * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            log.warn("Timeout waiting for Claude to finish working");
            return false;

        } catch (Exception e) {
            log.error("Error during Working to Prompt transition", e);
            return false;
        }
    }

    /**
     * Verify that we have successfully arrived at the Working state. Checks for the presence of the
     * working indicator.
     */
    @IncomingTransition(description = "Verify arrival at Working state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Working state");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }

        // Check for presence of working-specific elements
        boolean foundWorkingIndicator = action.find(workingState.getWorkingIndicator()).isSuccess();

        if (foundWorkingIndicator) {
            log.info("Successfully confirmed Working state is active");
            return true;
        } else {
            log.error("Failed to confirm Working state - working indicator not found");
            return false;
        }
    }
}
