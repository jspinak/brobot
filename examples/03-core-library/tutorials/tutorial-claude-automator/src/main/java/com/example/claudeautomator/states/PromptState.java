package com.example.claudeautomator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;

/**
 * Represents the Prompt state where Claude is ready to receive input.
 * This is the initial state of the application.
 */
@State(initial = true)
@Getter
public class PromptState {
    
    private final StateImage claudePrompt;
    
    public PromptState() {
        // The prompt area that appears when Claude is ready for input
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1", 
                        "prompt/claude-prompt-2", 
                        "prompt/claude-prompt-3")
            .setName("ClaudePrompt")
            .build();
    }
}