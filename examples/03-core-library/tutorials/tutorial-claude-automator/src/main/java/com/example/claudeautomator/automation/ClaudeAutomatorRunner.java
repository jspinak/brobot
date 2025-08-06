package com.example.claudeautomator.automation;

import com.example.claudeautomator.states.PromptState;
import com.example.claudeautomator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Demonstrates the claude-automator functionality.
 * 
 * Key features shown:
 * - State-based automation
 * - Automatic state transitions
 * - Declarative search regions that update dynamically
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClaudeAutomatorRunner implements CommandLineRunner {
    
    private final Action action;
    private final StateNavigator stateNavigator;
    private final PromptState promptState;
    private final WorkingState workingState;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== Claude Automator Tutorial ===");
        log.info("This example demonstrates:");
        log.info("1. State-based automation");
        log.info("2. Declarative search regions");
        log.info("3. Cross-state object dependencies");
        log.info("");
        
        // Simulate monitoring Claude's interface
        monitorClaude();
    }
    
    private void monitorClaude() throws InterruptedException {
        log.info("Starting Claude monitoring...");
        
        // Check initial state
        ObjectCollection promptCollection = new ObjectCollection.Builder()
            .withImages(promptState.getClaudePrompt())
            .build();
        if (action.perform(
                new PatternFindOptions.Builder().build(),
                promptCollection).isSuccess()) {
            log.info("Claude is in Prompt state - ready for input");
            
            // Simulate user typing a prompt
            simulateUserInput();
            
            // Wait for Claude to transition to Working state
            // The StateNavigator will automatically detect the transition
            Thread.sleep(2000);
            
            // Check if we're now in Working state
            ObjectCollection workingCollection = new ObjectCollection.Builder()
                .withImages(workingState.getClaudeIcon())
                .build();
            if (action.perform(
                    new PatternFindOptions.Builder().build(),
                    workingCollection).isSuccess()) {
                log.info("Claude has transitioned to Working state!");
                log.info("Note: The icon was found using a search region calculated from the prompt location");
            }
        } else {
            log.warn("Claude prompt not found. Please open Claude in your browser.");
        }
    }
    
    private void simulateUserInput() {
        log.info("Simulating user input...");
        // In a real scenario, you might:
        // - Click on the prompt area
        // - Type a message
        // - Press Enter
        
        // For this tutorial, we'll just log the action
        log.info("User would type prompt and press Enter here");
    }
}