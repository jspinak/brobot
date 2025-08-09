package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the Working state where Claude is actively processing.
 * 
 * This demonstrates the declarative search region feature:
 * The ClaudeIcon's search region is automatically calculated relative
 * to where the ClaudePrompt was found, even though they're in different states.
 */
@State
@Getter
@Slf4j
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        log.info("Creating WorkingState");
        
        // Create the claude icon images
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .setName("ClaudeIcon")
            .build();
        
        log.info("WorkingState created successfully");
    }
}