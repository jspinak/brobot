package io.github.jspinak.brobot.runner.execution;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents the current status of an automation execution.
 * This class contains all relevant information about execution progress,
 * state, timing, and errors.
 */
@Getter
@Setter
public class ExecutionStatus {
    // Current state of execution
    private ExecutionState state = ExecutionState.IDLE;
    
    // Timing information
    private Instant startTime;
    private Instant endTime;
    
    // Progress tracking (0.0 to 1.0)
    private double progress = 0.0;
    
    // Current operation description
    private String currentOperation;
    
    // Error information if execution failed
    private Exception error;
    
    /**
     * Gets the current duration of the execution
     * @return Duration of execution or null if not started
     */
    public Duration getDuration() {
        if (startTime == null) {
            return null;
        }
        
        Instant end = endTime != null ? endTime : Instant.now();
        return Duration.between(startTime, end);
    }
    
    /**
     * Check if the execution is in a terminal state
     * @return true if execution is done (completed, error, timeout, or stopped)
     */
    public boolean isFinished() {
        return state == ExecutionState.COMPLETED ||
               state == ExecutionState.ERROR ||
               state == ExecutionState.TIMEOUT ||
               state == ExecutionState.STOPPED;
    }
    
    /**
     * Gets a simple status message based on the current state and operation
     */
    public String getStatusMessage() {
        StringBuilder message = new StringBuilder();
        
        message.append(state.getDescription());
        
        if (currentOperation != null && !currentOperation.isEmpty() && 
                state != ExecutionState.COMPLETED && 
                state != ExecutionState.ERROR && 
                state != ExecutionState.STOPPED) {
            message.append(": ").append(currentOperation);
        }
        
        if (state == ExecutionState.ERROR && error != null) {
            message.append(" - ").append(error.getMessage());
        }
        
        if (startTime != null && (state == ExecutionState.RUNNING || 
                                  state == ExecutionState.PAUSED || 
                                  state == ExecutionState.STOPPING)) {
            message.append(" (Running for ").append(formatDuration(getDuration())).append(")");
        }
        
        return message.toString();
    }
    
    /**
     * Formats a duration in a human-readable way
     */
    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "unknown time";
        }
        
        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return seconds + " seconds";
        }
        
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes < 60) {
            return String.format("%d min %d sec", minutes, seconds);
        }
        
        long hours = minutes / 60;
        minutes = minutes % 60;
        
        return String.format("%d hours %d min", hours, minutes);
    }
    
    /**
     * Create a copy of the current status
     */
    public ExecutionStatus copy() {
        ExecutionStatus copy = new ExecutionStatus();
        copy.state = this.state;
        copy.startTime = this.startTime;
        copy.endTime = this.endTime;
        copy.progress = this.progress;
        copy.currentOperation = this.currentOperation;
        copy.error = this.error;
        return copy;
    }
    
    /**
     * Reset the status to initial state
     */
    public void reset() {
        state = ExecutionState.IDLE;
        startTime = null;
        endTime = null;
        progress = 0.0;
        currentOperation = null;
        error = null;
    }
    
    @Override
    public String toString() {
        return "ExecutionStatus{" +
                "state=" + state +
                ", progress=" + String.format("%.1f%%", progress * 100) +
                ", currentOp='" + (currentOperation != null ? currentOperation : "none") + '\'' +
                ", duration=" + (getDuration() != null ? formatDuration(getDuration()) : "not started") +
                '}';
    }
}