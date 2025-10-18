package com.claude.automator.states;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the Working state where Claude is actively processing.
 *
 * <p>This demonstrates the declarative search region feature: The ClaudeIcon's search region is
 * automatically calculated relative to where the ClaudePrompt was found, even though they're in
 * different states.
 */
@State
@Getter
@Slf4j
public class WorkingState {

    private final StateImage claudeIcon;
    private final StateImage workingIndicator;

    public WorkingState() {
        log.info("Creating WorkingState");

        // Create the claude icon with declarative search region
        // The highlight of this tutorial: search region is calculated relative to ClaudePrompt
        // from PromptState, even though they're in different states!
        claudeIcon =
                new StateImage.Builder()
                        .addPatterns(
                                "working/claude-icon-1",
                                "working/claude-icon-2",
                                "working/claude-icon-3",
                                "working/claude-icon-4")
                        .setName("ClaudeIcon")
                        .setSearchRegionOnObject(
                                SearchRegionOnObject.builder()
                                        .setTargetType(StateObject.Type.IMAGE)
                                        .setTargetStateName("Prompt")
                                        .setTargetObjectName("ClaudePrompt")
                                        .setAdjustments(
                                                MatchAdjustmentOptions.builder()
                                                        .setAddX(3)
                                                        .setAddY(10)
                                                        .setAddW(30)
                                                        .setAddH(55)
                                                        .build())
                                        .build())
                        .build();

        // Create the working indicator (same as claude icon for now)
        // This represents the visual indicator that Claude is actively processing
        workingIndicator =
                new StateImage.Builder()
                        .addPatterns(
                                "working/claude-icon-1",
                                "working/claude-icon-2",
                                "working/claude-icon-3",
                                "working/claude-icon-4")
                        .setName("WorkingIndicator")
                        .build();

        log.info("WorkingState created successfully");
    }
}
