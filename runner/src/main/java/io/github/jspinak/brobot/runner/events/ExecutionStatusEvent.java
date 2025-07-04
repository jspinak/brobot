package io.github.jspinak.brobot.runner.events;

import lombok.Data;

import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;

/**
 * Event representing a change in execution status.
 * Contains the current execution status details.
 */
@Data
public class ExecutionStatusEvent extends BrobotEvent {
    private final ExecutionStatus status;
    private final String message;

    public ExecutionStatusEvent(EventType eventType, Object source, ExecutionStatus status, String message) {
        super(eventType, source);
        this.status = status;
        this.message = message;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ExecutionState getState() {
        return status.getState();
    }

    public double getProgress() {
        return status.getProgress();
    }

    public String getDetails() {
        return message;
    }

    /**
     * Factory method to create an execution started event
     */
    public static ExecutionStatusEvent started(Object source, ExecutionStatus status, String message) {
        return new ExecutionStatusEvent(EventType.EXECUTION_STARTED, source, status, message);
    }

    /**
     * Factory method to create an execution progress event
     */
    public static ExecutionStatusEvent progress(Object source, ExecutionStatus status, String message) {
        return new ExecutionStatusEvent(EventType.EXECUTION_PROGRESS, source, status, message);
    }

    /**
     * Factory method to create an execution completed event
     */
    public static ExecutionStatusEvent completed(Object source, ExecutionStatus status, String message) {
        return new ExecutionStatusEvent(EventType.EXECUTION_COMPLETED, source, status, message);
    }

    /**
     * Factory method to create an execution failed event
     */
    public static ExecutionStatusEvent failed(Object source, ExecutionStatus status, String message) {
        return new ExecutionStatusEvent(EventType.EXECUTION_FAILED, source, status, message);
    }

    /**
     * Factory method to create an execution paused event
     */
    public static ExecutionStatusEvent paused(Object source, ExecutionStatus status, String message) {
        return new ExecutionStatusEvent(EventType.EXECUTION_PAUSED, source, status, message);
    }

    /**
     * Factory method to create an execution resumed event
     */
    public static ExecutionStatusEvent resumed(Object source, ExecutionStatus status, String message) {
        return new ExecutionStatusEvent(EventType.EXECUTION_RESUMED, source, status, message);
    }

    /**
     * Factory method to create an execution stopped event
     */
    public static ExecutionStatusEvent stopped(Object source, ExecutionStatus status, String message) {
        return new ExecutionStatusEvent(EventType.EXECUTION_STOPPED, source, status, message);
    }
}