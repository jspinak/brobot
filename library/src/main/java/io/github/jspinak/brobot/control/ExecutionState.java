package io.github.jspinak.brobot.control;

/**
 * Represents the possible states of an automation execution. This enum is used to track and control
 * the execution flow, enabling pause, resume, and stop functionality.
 */
public enum ExecutionState {
    /** Execution has not started or has been reset */
    IDLE("Idle - ready to start"),

    /** Execution is actively running */
    RUNNING("Running"),

    /** Execution has been paused and can be resumed */
    PAUSED("Paused - can be resumed"),

    /** Execution is in the process of stopping */
    STOPPING("Stopping - cleanup in progress"),

    /** Execution has been stopped and cannot be resumed */
    STOPPED("Stopped");

    private final String description;

    ExecutionState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if the execution is in an active state (running or paused)
     *
     * @return true if the state is RUNNING or PAUSED
     */
    public boolean isActive() {
        return this == RUNNING || this == PAUSED;
    }

    /**
     * Checks if the execution has terminated (stopped or stopping)
     *
     * @return true if the state is STOPPING or STOPPED
     */
    public boolean isTerminated() {
        return this == STOPPING || this == STOPPED;
    }

    /**
     * Checks if the execution can be started
     *
     * @return true if the state is IDLE or STOPPED
     */
    public boolean canStart() {
        return this == IDLE || this == STOPPED;
    }

    /**
     * Checks if the execution can be paused
     *
     * @return true if the state is RUNNING
     */
    public boolean canPause() {
        return this == RUNNING;
    }

    /**
     * Checks if the execution can be resumed
     *
     * @return true if the state is PAUSED
     */
    public boolean canResume() {
        return this == PAUSED;
    }

    /**
     * Checks if the execution can be stopped
     *
     * @return true if the state is RUNNING or PAUSED
     */
    public boolean canStop() {
        return this == RUNNING || this == PAUSED;
    }
}
