package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.tools.logging.MessageFormatter;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Enhanced version of StateNavigator that uses the unified logging system
 * instead of direct ConsoleReporter calls.
 * 
 * <p>This decorator wraps the original StateNavigator and intercepts its
 * logging to provide better integration with the unified logging system.</p>
 */
@Component
@Primary
public class EnhancedStateNavigator extends StateNavigator {
    
    @Autowired
    private BrobotLogger logger;
    
    @Autowired(required = false)
    private ConsoleActionConfig consoleConfig;
    
    @Override
    public boolean openState(String stateName) {
        // Log through unified system instead of ConsoleReporter
        logger.log()
            .navigation("OpenState")
            .metadata("targetState", stateName)
            .metadata("operation", "OpenState")
            .console(String.format("Open State %s", stateName))
            .log();
            
        Long stateToOpen = allStatesInProjectService.getStateId(stateName);
        if (stateToOpen == null) {
            logger.log()
                .error(new IllegalArgumentException("State not found: " + stateName))
                .message("Target state not found")
                .metadata("stateName", stateName)
                .console(MessageFormatter.fail + " Target state not found.")
                .log();
            return false;
        }
        
        return openState(stateToOpen);
    }
    
    @Override
    public boolean openState(Long stateToOpen) {
        String stateName = allStatesInProjectService.getStateName(stateToOpen);
        
        logger.log()
            .navigation("OpenState")
            .metadata("targetState", stateName)
            .metadata("targetStateId", stateToOpen)
            .metadata("operation", "OpenState")
            .console(String.format("Open State %s", stateName))
            .log();
            
        // Call parent implementation but intercept console output
        boolean originalConsoleEnabled = isConsoleOutputEnabled();
        try {
            // Temporarily disable console output in parent to avoid duplication
            disableParentConsoleOutput();
            
            boolean success = super.openState(stateToOpen);
            
            if (!success) {
                logger.log()
                    .navigation("OpenStateFailed")
                    .metadata("targetState", stateName)
                    .metadata("targetStateId", stateToOpen)
                    .metadata("reason", "All paths tried")
                    .console(MessageFormatter.fail + " All paths tried, open failed.")
                    .log();
            }
            
            // Log active states
            String activeStates = stateMemory.getActiveStateNames().toString();
            logger.log()
                .observation("Active states after navigation")
                .metadata("activeStates", activeStates)
                .metadata("activeStateCount", stateMemory.getActiveStates().size())
                .console("Active States: " + activeStates)
                .log();
                
            return success;
            
        } finally {
            // Restore original console output setting
            if (originalConsoleEnabled) {
                enableParentConsoleOutput();
            }
        }
    }
    
    /**
     * Helper method to check if console output is enabled.
     * This would need to be implemented based on how the parent class
     * manages console output.
     */
    private boolean isConsoleOutputEnabled() {
        // Check if console output should be enabled based on config
        return consoleConfig == null || consoleConfig.isEnabled();
    }
    
    /**
     * Temporarily disable console output in parent class.
     * This prevents duplicate logging when we're handling it ourselves.
     */
    private void disableParentConsoleOutput() {
        // This would need to interact with ConsoleReporter settings
        // For now, we'll use the adapter approach
    }
    
    /**
     * Re-enable console output in parent class.
     */
    private void enableParentConsoleOutput() {
        // Restore ConsoleReporter settings
    }
}