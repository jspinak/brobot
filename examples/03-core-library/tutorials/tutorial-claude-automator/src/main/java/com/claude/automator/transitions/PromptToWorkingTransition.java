package com.claude.automator.transitions;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.annotations.Transition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transition from Prompt to Working state.
 * This occurs when the user submits a prompt and Claude begins processing.
 */
@Transition(from = PromptState.class, to = WorkingState.class)
@RequiredArgsConstructor
@Slf4j
public class PromptToWorkingTransition {

    private final PromptState promptState;
    private final Action action;
    
    public boolean execute() {
        try {
            log.info("Executing transition from Prompt to Working state");
            
            // Using the fluent API to chain actions: find -> click -> type
            PatternFindOptions findClickType = new PatternFindOptions.Builder()
                    .setPauseAfterEnd(0.5) // Pause before clicking
                    .then(new ClickOptions.Builder()
                            .setPauseAfterEnd(0.5) // Pause before typing
                            .build())
                    .then(new TypeOptions.Builder()
                            .build())
                    .build();
            
            // Create target objects for the chained action
            ObjectCollection target = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt()) // For find & click
                    .withStrings(promptState.getContinueCommand()) // For type (continue with Enter)
                    .build();
            
            // Execute the chained action
            ActionResult result = action.perform(findClickType, target);
            
            if (result.isSuccess()) {
                log.info("Successfully executed transition from Prompt to Working");
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
}