package io.github.jspinak.brobot.runner.execution;

import lombok.Setter;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * Manages the status of an execution and provides methods to update it.
 * Also handles notifying any registered status consumers when the status changes.
 * Thread-safe: All methods are synchronized to prevent concurrent modification.
 */
public class ExecutionStatusManager {
    private final ExecutionStatus status;
    private final Object lock = new Object();
    
    /**
     * -- SETTER --
     *  Sets a consumer that will be notified of status changes
     */
    @Setter
    private volatile Consumer<ExecutionStatus> statusConsumer;

    public ExecutionStatusManager(ExecutionStatus status) {
        this.status = status;
    }

    /**
     * Updates the execution state and notifies consumers
     */
    public void updateState(ExecutionState state) {
        synchronized (lock) {
            status.setState(state);
            notifyStatusChange();
        }
    }

    /**
     * Updates the execution progress and notifies consumers
     */
    public void updateProgress(double progress) {
        synchronized (lock) {
            status.setProgress(progress);
            notifyStatusChange();
        }
    }

    /**
     * Updates the current operation description and notifies consumers
     */
    public void setCurrentOperation(String operation) {
        synchronized (lock) {
            status.setCurrentOperation(operation);
            notifyStatusChange();
        }
    }

    /**
     * Updates the start time and notifies consumers
     */
    public void updateStartTime(Instant startTime) {
        synchronized (lock) {
            status.setStartTime(startTime);
            notifyStatusChange();
        }
    }

    /**
     * Updates the end time and notifies consumers
     */
    public void updateEndTime(Instant endTime) {
        synchronized (lock) {
            status.setEndTime(endTime);
            notifyStatusChange();
        }
    }

    /**
     * Sets an error that occurred during execution
     */
    public void setError(Exception error) {
        synchronized (lock) {
            status.setError(error);
            notifyStatusChange();
        }
    }

    /**
     * Resets the status to initial state
     */
    public void reset() {
        synchronized (lock) {
            status.reset();
            notifyStatusChange();
        }
    }

    /**
     * Notifies any registered consumers of a status change
     * Note: Called within synchronized blocks, so already thread-safe
     */
    private void notifyStatusChange() {
        Consumer<ExecutionStatus> consumer = statusConsumer;
        if (consumer != null) {
            consumer.accept(status.copy());
        }
    }
    
    /**
     * Manually triggers notification of current status to consumers
     */
    public void notifyConsumer() {
        synchronized (lock) {
            notifyStatusChange();
        }
    }
}