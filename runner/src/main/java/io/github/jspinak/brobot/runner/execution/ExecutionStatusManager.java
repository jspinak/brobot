package io.github.jspinak.brobot.runner.execution;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * Manages the status of an execution and provides methods to update it.
 * Also handles notifying any registered status consumers when the status changes.
 */
public class ExecutionStatusManager {
    private final ExecutionStatus status;
    private Consumer<ExecutionStatus> statusConsumer;

    public ExecutionStatusManager(ExecutionStatus status) {
        this.status = status;
    }

    /**
     * Sets a consumer that will be notified of status changes
     */
    public void setStatusConsumer(Consumer<ExecutionStatus> statusConsumer) {
        this.statusConsumer = statusConsumer;
    }

    /**
     * Updates the execution state and notifies consumers
     */
    public void updateState(ExecutionState state) {
        status.setState(state);
        notifyStatusChange();
    }

    /**
     * Updates the execution progress and notifies consumers
     */
    public void updateProgress(double progress) {
        status.setProgress(progress);
        notifyStatusChange();
    }

    /**
     * Updates the current operation description and notifies consumers
     */
    public void setCurrentOperation(String operation) {
        status.setCurrentOperation(operation);
        notifyStatusChange();
    }

    /**
     * Updates the start time and notifies consumers
     */
    public void updateStartTime(Instant startTime) {
        status.setStartTime(startTime);
        notifyStatusChange();
    }

    /**
     * Updates the end time and notifies consumers
     */
    public void updateEndTime(Instant endTime) {
        status.setEndTime(endTime);
        notifyStatusChange();
    }

    /**
     * Sets an error that occurred during execution
     */
    public void setError(Exception error) {
        status.setError(error);
        notifyStatusChange();
    }

    /**
     * Resets the status to initial state
     */
    public void reset() {
        status.reset();
        notifyStatusChange();
    }

    /**
     * Notifies any registered consumers of a status change
     */
    private void notifyStatusChange() {
        if (statusConsumer != null) {
            statusConsumer.accept(status.copy());
        }
    }
}