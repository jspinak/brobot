package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State(name = "Working", description = "Claude is actively responding")
@Getter
@Slf4j
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        log.info("Creating WorkingState");
        
        // Create the claude icon images
        claudeIcon = new StateImage.Builder()
            .setName("ClaudeIcon")
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .build();
        
        log.info("WorkingState created successfully");
    }
}