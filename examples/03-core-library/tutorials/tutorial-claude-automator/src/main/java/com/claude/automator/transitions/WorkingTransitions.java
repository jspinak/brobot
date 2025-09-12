package com.claude.automator.transitions;

import org.springframework.stereotype.Component;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.annotations.FromTransition;
import io.github.jspinak.brobot.annotations.ToTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All transitions for the Working state using the new unified annotation format.
 * Contains FromTransitions from other states TO Working,
 * and a ToTransition to verify arrival at Working.
 */
@TransitionSet(state = WorkingState.class, description = "Claude Working state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkingTransitions {
    
    private final PromptState promptState;
    private final WorkingState workingState;
    private final Action action;
    
    /**
     * Navigate from Prompt to Working by submitting a command.
     * This transition occurs when the user submits a prompt and Claude begins processing.
     */
    @FromTransition(from = PromptState.class, priority = 1, description = "Navigate from Prompt to Working")
    public boolean fromPrompt() {
        try {
            log.info("Navigating from Prompt to Working");
            
            // In mock mode, just return true for testing
            if (io.github.jspinak.brobot.config.core.FrameworkSettings.mock) {
                log.info("Mock mode: simulating successful navigation");
                return true;
            }

            // Using the fluent API to chain actions: find -> click -> type
            PatternFindOptions findClickType =
                    new PatternFindOptions.Builder()
                            .setPauseAfterEnd(0.5) // Pause before clicking
                            .then(
                                    new ClickOptions.Builder()
                                            .setPauseAfterEnd(0.5) // Pause before typing
                                            .build())
                            .then(new TypeOptions.Builder().build())
                            .build();

            // Create target objects for the chained action
            ObjectCollection target =
                    new ObjectCollection.Builder()
                            .withImages(promptState.getClaudePrompt()) // For find & click
                            .withStrings(
                                    promptState
                                            .getContinueCommand()) // For type (continue with Enter)
                            .build();

            // Execute the chained action
            ActionResult result = action.perform(findClickType, target);

            if (result.isSuccess()) {
                log.info("Successfully triggered transition from Prompt to Working");
                return true;
            } else {
                log.warn("Failed to execute transition: {}", result.getActionDescription());
                return false;
            }

        } catch (Exception e) {
            log.error("Error during Prompt to Working transition", e);
            return false;
        }
    }
    
    /**
     * Verify that we have successfully arrived at the Working state.
     * Checks for the presence of the working indicator.
     */
    @ToTransition(description = "Verify arrival at Working state", required = true)
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