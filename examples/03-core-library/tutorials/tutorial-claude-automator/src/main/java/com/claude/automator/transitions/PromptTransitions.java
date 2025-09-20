package com.claude.automator.transitions;

import org.springframework.stereotype.Component;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Prompt state. Contains: - An IncomingTransition to verify arrival at
 * Prompt - OutgoingTransitions that go FROM Prompt TO other states
 *
 * <p>This pattern is cleaner because the outgoing transitions use Prompt's images, creating better
 * cohesion with only the PromptState as a dependency.
 */
@TransitionSet(state = PromptState.class, description = "Claude Prompt state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptTransitions {

    private final PromptState promptState;
    private final Action action;

    /** Navigate from Prompt to Working by submitting a command. */
    @OutgoingTransition(
            to = WorkingState.class,
            pathCost = 1,
            description = "Navigate from Prompt to Working")
    public boolean toWorking() {
        log.info("Navigating from Prompt to Working");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }

        // Type a command and submit it
        // This will trigger Claude to start working
        boolean typedCommand = action.type(promptState.getContinueCommand()).isSuccess();
        if (typedCommand) {
            // Press Enter to submit
            return action.type("\n").isSuccess();
        }
        return false;
    }

    /**
     * Verify that we have successfully arrived at the Prompt state. Checks for the presence of the
     * Claude prompt input area.
     */
    @IncomingTransition(description = "Verify arrival at Prompt state", required = true)
    public boolean verifyArrival() {
        log.info("Verifying arrival at Prompt state");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful verification");
            return true;
        }

        // Check for presence of prompt-specific elements
        boolean foundPrompt = action.find(promptState.getClaudePrompt()).isSuccess();

        if (foundPrompt) {
            log.info("Successfully confirmed Prompt state is active");
            return true;
        } else {
            log.error("Failed to confirm Prompt state - prompt elements not found");
            return false;
        }
    }
}
