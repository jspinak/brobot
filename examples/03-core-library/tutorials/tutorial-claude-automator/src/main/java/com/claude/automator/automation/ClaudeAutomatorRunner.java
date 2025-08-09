package com.claude.automator.automation;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Demonstrates the claude-automator functionality.
 * 
 * Key features shown:
 * - State-based automation
 * - Automatic state transitions
 * - Declarative search regions that update dynamically
 * - Documentation examples from /docs/03-core-library/tutorials/tutorial-claude-automator/
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClaudeAutomatorRunner implements CommandLineRunner {
    
    private final Action action;
    private final StateNavigator stateNavigator;
    private final PromptState promptState;
    private final WorkingState workingState;
    
    // Optional automation examples - may not be available depending on configuration
    @Autowired(required = false)
    private EventDrivenAutomation eventDrivenAutomation;
    
    @Autowired(required = false)
    private ReactiveAutomation reactiveAutomation;
    
    @Autowired(required = false)
    private ClaudeMonitoringAutomation monitoringAutomation;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== Claude Automator Tutorial ===");
        log.info("This example demonstrates:");
        log.info("1. State-based automation");
        log.info("2. Declarative search regions");
        log.info("3. Cross-state object dependencies");
        log.info("4. Documentation examples - different automation approaches");
        log.info("");
        
        // Run the original tutorial example
        monitorClaude();
        
        // Run documentation examples if available
        runDocumentationExamples();
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
    
    /**
     * Run documentation examples if they are available.
     * From: /docs/03-core-library/tutorials/tutorial-claude-automator/automation.md
     */
    private void runDocumentationExamples() {
        log.info("\n=== Documentation Examples ===");
        
        // Event-Driven Approach
        if (eventDrivenAutomation != null) {
            log.info(">>> Event-Driven Automation Example <<<");
            try {
                eventDrivenAutomation.simulateStateChanges();
            } catch (Exception e) {
                log.error("Error running event-driven example", e);
            }
        } else {
            log.info("Event-driven automation not available");
        }
        
        log.info("");
        
        // Reactive Approach  
        if (reactiveAutomation != null) {
            log.info(">>> Reactive Automation Example <<<");
            try {
                reactiveAutomation.demonstrateReactiveMonitoring();
                Thread.sleep(3000); // Let reactive streams run for a bit
            } catch (Exception e) {
                log.error("Error running reactive example", e);
            }
        } else {
            log.info("Reactive automation not available (requires Project Reactor)");
        }
        
        log.info("");
        
        // Continuous Monitoring
        if (monitoringAutomation != null) {
            log.info(">>> Continuous Monitoring Status <<<");
            try {
                log.info("Monitoring automation is running: {}", monitoringAutomation.isRunning());
                log.info("Scheduler is active: {}", monitoringAutomation.isSchedulerRunning());
                log.info("Continuous monitoring is enabled via configuration");
                log.info("(To enable: set claude.automator.monitoring.enabled=true)");
            } catch (Exception e) {
                log.error("Error checking monitoring status", e);
            }
        } else {
            log.info("Continuous monitoring not enabled");
            log.info("To enable: set claude.automator.monitoring.enabled=true");
        }
        
        log.info("\n=== Documentation Examples Complete ===");
        
        // Show configuration information
        showConfigurationOptions();
    }
    
    /**
     * Display configuration options for the different automation approaches.
     */
    private void showConfigurationOptions() {
        log.info("\n=== Configuration Options ===");
        log.info("Available automation approaches from documentation:");
        log.info("");
        log.info("1. Event-Driven Approach:");
        log.info("   - Automatic (always available)");
        log.info("   - Reacts to Spring events");
        log.info("");
        log.info("2. Reactive Approach:");
        log.info("   - Requires: Project Reactor dependency");
        log.info("   - Add: implementation 'io.projectreactor:reactor-core'");
        log.info("");  
        log.info("3. Continuous Monitoring:");
        log.info("   - Enable: claude.automator.monitoring.enabled=true");
        log.info("   - Configure: claude.automator.monitoring.check-interval=2");
        log.info("");
        log.info("Each approach demonstrates different patterns for automation:");
        log.info("- Scheduled monitoring (ClaudeMonitoringAutomation)");
        log.info("- Event-driven reactions (EventDrivenAutomation)");
        log.info("- Reactive streams (ReactiveAutomation)");
    }
}