package io.github.jspinak.brobot.runner.ui.automation.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model representing the current status of automation execution.
 * Immutable snapshot of automation state at a point in time.
 */
@Data
@Builder
@EqualsAndHashCode
public class AutomationStatus {
    
    /**
     * Whether any automation is currently running.
     */
    private final boolean running;
    
    /**
     * Whether the automation is paused.
     */
    private final boolean paused;
    
    /**
     * Name of the currently running automation.
     */
    private final String currentAutomationName;
    
    /**
     * Current action being executed.
     */
    private final String currentAction;
    
    /**
     * Progress percentage (0.0 to 1.0).
     */
    @Builder.Default
    private final double progress = 0.0;
    
    /**
     * Elapsed time in milliseconds.
     */
    @Builder.Default
    private final long elapsedTime = 0;
    
    /**
     * Number of completed actions/steps.
     */
    @Builder.Default
    private final int completedCount = 0;
    
    /**
     * Total number of actions/steps.
     */
    @Builder.Default
    private final int totalCount = 0;
    
    /**
     * Whether an error has occurred.
     */
    @Builder.Default
    private final boolean hasError = false;
    
    /**
     * Error message if hasError is true.
     */
    private final String errorMessage;
    
    /**
     * Creates an idle status (nothing running).
     */
    public static AutomationStatus idle() {
        return AutomationStatus.builder()
                .running(false)
                .paused(false)
                .currentAutomationName("None")
                .build();
    }
    
    /**
     * Creates a running status.
     */
    public static AutomationStatus running(String automationName) {
        return AutomationStatus.builder()
                .running(true)
                .paused(false)
                .currentAutomationName(automationName)
                .build();
    }
    
    /**
     * Creates a paused status.
     */
    public static AutomationStatus paused(String automationName, double progress) {
        return AutomationStatus.builder()
                .running(true)
                .paused(true)
                .currentAutomationName(automationName)
                .progress(progress)
                .build();
    }
    
    /**
     * Creates an error status.
     */
    public static AutomationStatus error(String automationName, String errorMessage) {
        return AutomationStatus.builder()
                .running(false)
                .paused(false)
                .currentAutomationName(automationName)
                .hasError(true)
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * Gets the execution state as a string.
     */
    public String getStateString() {
        if (!running) {
            return hasError ? "ERROR" : "IDLE";
        }
        return paused ? "PAUSED" : "RUNNING";
    }
    
    /**
     * Gets progress as a percentage string.
     */
    public String getProgressPercentage() {
        return String.format("%.0f%%", progress * 100);
    }
    
    /**
     * Checks if the automation is actively executing (running and not paused).
     */
    public boolean isActivelyRunning() {
        return running && !paused;
    }
    
    /**
     * Gets a completion ratio string.
     */
    public String getCompletionRatio() {
        if (totalCount <= 0) {
            return "";
        }
        return String.format("%d / %d", completedCount, totalCount);
    }
    
    /**
     * Calculates estimated time remaining based on progress and elapsed time.
     */
    public long getEstimatedTimeRemaining() {
        if (progress <= 0 || progress >= 1.0) {
            return 0;
        }
        
        double remainingProgress = 1.0 - progress;
        double timePerProgress = elapsedTime / progress;
        return (long) (timePerProgress * remainingProgress);
    }
}