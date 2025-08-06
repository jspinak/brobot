package com.example.claudeautomator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Getter;

/**
 * Represents the Working state where Claude is actively processing.
 * 
 * This demonstrates the declarative search region feature:
 * The ClaudeIcon's search region is automatically calculated relative
 * to where the ClaudePrompt was found, even though they're in different states.
 */
@State
@Getter
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        // The icon's search region is defined relative to the prompt location
        // This demonstrates cross-state search region dependencies
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .setName("ClaudeIcon")
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .targetType(StateObject.Type.IMAGE)
                    .targetStateName("Prompt")           // References PromptState
                    .targetObjectName("ClaudePrompt")    // References the prompt image
                    .adjustments(MatchAdjustmentOptions.builder()
                            .addX(3)      // Slight offset to the right
                            .addY(10)     // Below the prompt
                            .addW(30)     // Wider search area
                            .addH(55)     // Taller search area
                            .build())
                    .build())
            .build();
    }
}