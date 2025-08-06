package com.example.claudeautomator.transitions;

import com.example.claudeautomator.states.PromptState;
import com.example.claudeautomator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
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
    
    private final Action action;
    private final WorkingState workingState;
    
    /**
     * Checks if Claude has transitioned to the Working state by
     * looking for the Claude icon that appears during processing.
     * 
     * Note: The search region for the icon is automatically calculated
     * based on where the prompt was found, thanks to declarative regions.
     */
    public boolean execute() {
        log.info("Checking for transition to Working state");
        
        // The search region for claudeIcon is automatically set based on
        // where ClaudePrompt was last found - no manual region calculation needed!
        boolean found = action.find(workingState.getClaudeIcon()).isSuccess();
        
        if (found) {
            log.info("Claude is now in Working state");
        }
        
        return found;
    }
}