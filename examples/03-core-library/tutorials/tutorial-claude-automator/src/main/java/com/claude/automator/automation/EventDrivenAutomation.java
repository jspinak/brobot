package com.claude.automator.automation;

import com.claude.automator.states.WorkingState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event-Driven Approach example from documentation.
 * From: /docs/03-core-library/tutorials/tutorial-claude-automator/automation.md
 * 
 * This demonstrates how to react to state changes using Spring events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventDrivenAutomation {
    
    /**
     * Example event class for state changes.
     * In a real implementation, this would be part of the Brobot framework.
     */
    public static class StateChangeEvent {
        private final String stateName;
        private final boolean isActive;
        
        public StateChangeEvent(String stateName, boolean isActive) {
            this.stateName = stateName;
            this.isActive = isActive;
        }
        
        public String getState() {
            return stateName;
        }
        
        public boolean isActive() {
            return isActive;
        }
    }
    
    /**
     * React to state changes using Spring's @EventListener.
     * This is called automatically when StateChangeEvent is published.
     */
    @EventListener
    public void onStateChange(StateChangeEvent event) {
        log.info("Received state change event: {} -> {}", 
            event.getState(), event.isActive() ? "ACTIVE" : "INACTIVE");
            
        if (event.getState().equals("WORKING")) {
            if (event.isActive()) {
                log.info("Working state activated - Claude is processing");
                onWorkingStateActivated();
            } else {
                log.info("Working state deactivated - Claude finished processing");
                onWorkingStateDeactivated();
            }
        }
        
        if (event.getState().equals("PROMPT")) {
            if (event.isActive()) {
                log.info("Prompt state activated - Claude is ready for input");
                onPromptStateActivated();
            }
        }
    }
    
    private void onWorkingStateActivated() {
        // React to Working state becoming active
        log.debug("Setting up monitoring for Working state");
        
        // Could start specific monitoring, adjust UI, etc.
        // Example: Start monitoring Claude's output for completion
    }
    
    private void onWorkingStateDeactivated() {
        // React to Working state becoming inactive
        log.debug("Working state finished - preparing for next interaction");
        
        // Could trigger cleanup, prepare for next prompt, etc.
        // Example: Save conversation state, reset UI elements
    }
    
    private void onPromptStateActivated() {
        // React to Prompt state becoming active
        log.debug("Prompt state ready - user can input new query");
        
        // Could enable input fields, show suggestions, etc.
        // Example: Focus on input field, load conversation history
    }
    
    /**
     * Programmatic event publishing example.
     * In practice, these events would be published by the state management system.
     */
    public void simulateStateChanges() {
        log.info("=== Simulating Event-Driven State Changes ===");
        
        // Simulate state transitions
        onStateChange(new StateChangeEvent("PROMPT", true));
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        onStateChange(new StateChangeEvent("PROMPT", false));
        onStateChange(new StateChangeEvent("WORKING", true));
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        onStateChange(new StateChangeEvent("WORKING", false));
        onStateChange(new StateChangeEvent("PROMPT", true));
        
        log.info("=== Event-Driven Simulation Complete ===");
    }
}