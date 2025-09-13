package com.claude.automator.transitions;

import org.springframework.stereotype.Component;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.ToTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Prompt state using the new unified annotation format. Contains
 * FromTransitions from other states TO Prompt, and a ToTransition to verify arrival at Prompt.
 */
@TransitionSet(state = PromptState.class, description = "Claude Prompt state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptTransitions {

    private final PromptState promptState;
    private final WorkingState workingState;
    private final Action action;

    /**
     * Navigate from Working state back to Prompt. This occurs when Claude finishes processing and
     * returns to the prompt.
     */
    @FromTransition(
            from = WorkingState.class,
            priority = 1,
            description = "Navigate from Working to Prompt")
    public boolean fromWorking() {
        log.info("Navigating from Working to Prompt");
        // In mock mode, just return true for testing
        if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
            log.info("Mock mode: simulating successful navigation");
            return true;
        }

        // Wait for Claude to finish processing and return to prompt
        // This might involve waiting for the working indicator to disappear
        // and the prompt to reappear
        return action.find(promptState.getClaudePrompt()).isSuccess();
    }

    /**
     * Verify that we have successfully arrived at the Prompt state. Checks for the presence of the
     * Claude prompt input area.
     */
    @ToTransition(description = "Verify arrival at Prompt state", required = true)
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
