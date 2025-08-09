package com.claude.automator.automation;

import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Continuous Monitoring Automation - Example from documentation.
 * From: /docs/03-core-library/tutorials/tutorial-claude-automator/automation.md
 * 
 * The automation continuously monitors Claude's interface, detecting when the AI 
 * has finished responding and automatically reopening the Working state to continue.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "claude.automator.monitoring.enabled", havingValue = "true", matchIfMissing = false)
public class ClaudeMonitoringAutomation {
    private final StateService stateService;
    private final StateMemory stateMemory;
    private final StateNavigator stateNavigator;
    private final Action action;
    private final WorkingState workingState;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean running = false;
    
    @PostConstruct
    public void startMonitoring() {
        log.info("Starting Claude monitoring automation");
        running = true;
        
        // Check every 2 seconds
        scheduler.scheduleWithFixedDelay(this::checkClaudeIconStatus, 
                5, 2, TimeUnit.SECONDS);
    }
    
    @PreDestroy
    public void stopMonitoring() {
        log.info("Stopping Claude monitoring automation");
        running = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private void checkClaudeIconStatus() {
        if (!running) return;
        
        try {
            // Check if Working state is active
            // Note: In a real implementation, we would check if WorkingState is active
            // For now, we'll just proceed with the check
            if (stateMemory.getActiveStateList().isEmpty()) {
                log.debug("No active states, skipping check");
                return;
            }
            
            // Quick find to check if icon is still visible
            PatternFindOptions quickFind = new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(0.5)
                    .build();
            
            boolean iconFound = action.perform(quickFind, workingState.getClaudeIcon()).isSuccess();
            
            if (!iconFound) {
                log.info("Claude icon disappeared - removing Working state and reopening");
                
                // Note: In a real implementation, we would remove the Working state ID
                // For now, we'll just log the action
                
                // Reopen Working state using enhanced StateNavigator
                // Note: WorkingState.Name.WORKING would need to be defined as an enum
                // For this example, we'll use a string-based approach
                boolean success = reopenWorkingState();
                
                if (success) {
                    log.info("Successfully reopened Working state");
                } else {
                    log.error("Failed to reopen Working state");
                }
            } else {
                log.debug("Claude icon still visible");
            }
        } catch (Exception e) {
            log.error("Error during Claude icon monitoring", e);
        }
    }
    
    /**
     * Helper method to reopen the Working state.
     * In a real implementation, this would use StateNavigator with state enums.
     */
    private boolean reopenWorkingState() {
        try {
            // Simulate state reopening logic
            log.info("Attempting to reopen Working state...");
            
            // In practice, you would:
            // 1. Navigate to the Working state
            // 2. Verify the state is active
            // 3. Update state memory accordingly
            
            // For this example, we'll simulate success
            Thread.sleep(1000); // Simulate navigation time
            return true;
            
        } catch (Exception e) {
            log.error("Failed to reopen Working state", e);
            return false;
        }
    }
    
    /**
     * Alternative deep check implementation from documentation
     */
    private boolean performDeepIconCheck() {
        try {
            // More thorough validation
            PatternFindOptions deepFind = new PatternFindOptions.Builder()
                    .setSimilarity(0.95)
                    .setPauseBeforeBegin(0.1)
                    .build();
                    
            return action.perform(deepFind, workingState.getClaudeIcon()).isSuccess();
            
        } catch (Exception e) {
            log.error("Error during deep icon check", e);
            return false;
        }
    }
    
    /**
     * Get current monitoring status
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Get scheduler status
     */
    public boolean isSchedulerRunning() {
        return !scheduler.isShutdown() && !scheduler.isTerminated();
    }
}