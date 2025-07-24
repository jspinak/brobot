package io.github.jspinak.brobot.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.control.ExecutionStoppedException;
import io.github.jspinak.brobot.navigation.monitoring.BaseAutomation;
import io.github.jspinak.brobot.navigation.monitoring.StateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Example automation script demonstrating pause/resume/stop functionality.
 * 
 * <p>This example shows how to create an automation that:
 * <ul>
 *   <li>Checks for pause points regularly</li>
 *   <li>Can be paused, resumed, and stopped from external control</li>
 *   <li>Handles ExecutionStoppedException gracefully</li>
 * </ul>
 * </p>
 * 
 * <p>Usage example:
 * <pre>{@code
 * // Create and start the automation
 * PausableAutomationExample automation = new PausableAutomationExample(stateHandler);
 * automation.start();
 * 
 * // Control from another thread or UI
 * automation.pause();  // Pauses at next checkpoint
 * automation.resume(); // Continues execution
 * automation.stop();   // Stops gracefully
 * }</pre>
 * </p>
 */
@Slf4j
public class PausableAutomationExample extends BaseAutomation {
    
    private final Action action;
    
    public PausableAutomationExample(StateHandler stateHandler, Action action) {
        super(stateHandler);
        this.action = action;
    }
    
    @Override
    public void start() {
        super.start(); // Sets execution state to RUNNING
        
        // Run automation in a separate thread
        Thread automationThread = new Thread(this::runAutomation, "pausable-automation");
        automationThread.start();
    }
    
    private void runAutomation() {
        log.info("Starting pausable automation");
        
        try {
            // Example: Process items in a loop with pause points
            for (int i = 0; i < 100; i++) {
                // Check for pause/stop at the beginning of each iteration
                checkPausePoint();
                
                log.info("Processing item {}", i);
                
                // Perform some automation action
                ActionOptions clickOptions = new ActionOptions.Builder()
                    .setAction(ActionOptions.Action.CLICK)
                    .build();
                
                // The action execution also checks pause points internally
                action.perform(clickOptions, "button" + i);
                
                // Simulate some work
                Thread.sleep(1000);
                
                // Check pause point again after work
                checkPausePoint();
            }
            
            log.info("Automation completed successfully");
            
        } catch (ExecutionStoppedException e) {
            log.info("Automation stopped by user request");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Automation interrupted");
        } catch (Exception e) {
            log.error("Automation failed with error", e);
        } finally {
            // Reset state when done
            reset();
            log.info("Automation cleanup completed");
        }
    }
    
    /**
     * Example of a long-running operation with multiple pause points.
     */
    private void performComplexOperation() throws ExecutionStoppedException, InterruptedException {
        log.info("Starting complex operation");
        
        // Step 1: Initialize
        checkPausePoint();
        log.info("Step 1: Initializing...");
        Thread.sleep(500);
        
        // Step 2: Process
        checkPausePoint();
        log.info("Step 2: Processing...");
        Thread.sleep(1000);
        
        // Step 3: Verify
        checkPausePoint();
        log.info("Step 3: Verifying...");
        Thread.sleep(500);
        
        // Step 4: Complete
        checkPausePoint();
        log.info("Step 4: Completing...");
        
        log.info("Complex operation completed");
    }
    
    /**
     * Example showing pause/resume during state transitions.
     */
    private void navigateWithPauseSupport() throws ExecutionStoppedException, InterruptedException {
        String[] states = {"Login", "Dashboard", "Settings", "Profile"};
        
        for (String stateName : states) {
            // Check if we should pause before transitioning
            checkPausePoint();
            
            log.info("Navigating to state: {}", stateName);
            // Note: StateHandler doesn't have goToState method - this is just an example
            // In real implementation, you would use your navigation logic here
            boolean success = performStateNavigation(stateName);
            
            if (!success) {
                log.warn("Failed to navigate to state: {}", stateName);
                // Could check pause point here to allow user intervention
                checkPausePoint();
            }
            
            // Pause point after successful navigation
            checkPausePoint();
            
            // Perform actions in the state
            performStateActions(stateName);
        }
    }
    
    private void performStateActions(String stateName) throws ExecutionStoppedException, InterruptedException {
        log.info("Performing actions in state: {}", stateName);
        
        // Multiple pause points within state actions
        for (int i = 0; i < 3; i++) {
            checkPausePoint();
            log.info("Action {} in {}", i + 1, stateName);
            Thread.sleep(500);
        }
    }
    
    /**
     * Example navigation method - replace with actual navigation logic.
     */
    private boolean performStateNavigation(String stateName) {
        // This is just a placeholder - implement your actual navigation logic
        return Math.random() > 0.1; // 90% success rate for example
    }
}