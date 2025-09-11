package io.github.jspinak.brobot.runner.execution.control;

/**
 * Interface for controlling execution flow.
 *
 * <p>Provides methods for pausing, resuming, and stopping executions.
 *
 * @since 1.0.0
 */
public interface ExecutionControl {

    /** Pauses the execution. */
    void pause();

    /** Resumes a paused execution. */
    void resume();

    /** Stops the execution. */
    void stop();

    /**
     * Checks if the execution is paused.
     *
     * @return true if paused
     */
    boolean isPaused();

    /**
     * Checks if stop has been requested.
     *
     * @return true if stop requested
     */
    boolean isStopRequested();

    /**
     * Checks the pause state and waits if paused.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    void checkPaused() throws InterruptedException;
}
