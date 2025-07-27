package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State(initial = true, name = "Prompt", description = "Claude prompt interface")
@Getter
@Slf4j
public class PromptState {
    
    private final StateImage claudePrompt;
    private final StateString continueCommand;
    
    public PromptState() {
        log.info("Creating PromptState");
        
        // Initialize the claude prompt image
        claudePrompt = new StateImage.Builder()
            .setName("ClaudePrompt")
            .addPatterns("prompt/claude-prompt-1",
                        "prompt/claude-prompt-2",
                        "prompt/claude-prompt-3")
            .build();
        
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
        
        log.info("PromptState created successfully");
    }
}