package io.github.jspinak.brobot.runner.execution;

/**
 * Enum representing the possible states of an automation execution.
 */
public enum ExecutionState {
    IDLE("Idle"),
    STARTING("Starting execution"),
    RUNNING("Running"),
    PAUSED("Paused"),
    STOPPING("Stopping"),
    COMPLETED("Completed successfully"),
    ERROR("Failed with error"),
    TIMEOUT("Timed out"),
    STOPPED("Stopped");
    
    private final String description;
    
    ExecutionState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this state indicates execution is active
     */
    public boolean isActive() {
        return this == STARTING || this == RUNNING || this == PAUSED || this == STOPPING;
    }
    
    /**
     * Check if this state indicates execution completed successfully
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    /**
     * Check if this state indicates execution failed
     */
    public boolean isFailed() {
        return this == ERROR || this == TIMEOUT;
    }
    
    /**
     * Check if this state indicates execution was terminated externally
     */
    public boolean isTerminated() {
        return this == STOPPED;
    }
}