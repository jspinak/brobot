package com.claude.automator.automation;

import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
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
            String workingStateName = "Working";
            Long workingStateId = stateService.getStateId(workingStateName);
            
            if (workingStateId == null || !stateMemory.getActiveStates().contains(workingStateId)) {
                log.debug("Working state is not active, skipping check");
                return;
            }
            
            // Quick find to check if icon is still visible with automatic logging
            PatternFindOptions quickFind = new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(0.5)
                    .withBeforeActionLog("Checking Claude icon status...")
                    .withSuccessLog("Claude icon is still visible")
                    .withFailureLog("Claude icon has disappeared - need to reopen Working state")
                    .build();
            
            ObjectCollection objects = new ObjectCollection.Builder()
                    .withImages(workingState.getClaudeIcon())
                    .build();
            boolean iconFound = action.perform(quickFind, objects).isSuccess();
            
            if (!iconFound) {
                // Remove Working from active states
                stateMemory.removeInactiveState(workingStateId);
                
                // Reopen Working state using enhanced StateNavigator
                // The state navigator will handle its own logging
                boolean success = stateNavigator.openState(workingStateName);
                
                if (!success) {
                    log.error("Failed to reopen Working state after icon disappeared");
                }
            }
        } catch (Exception e) {
            log.error("Error during Claude icon monitoring", e);
        }
    }
}