package io.github.jspinak.brobot.control;

/**
 * Interface for controlling the execution flow of automation tasks. Provides methods to pause,
 * resume, and stop execution, as well as query the current execution state.
 *
 * <p>This interface is the core contract for implementing execution control throughout the Brobot
 * framework. It enables fine-grained control over automation execution, allowing for interactive
 * debugging, user intervention, and graceful shutdown.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Thread-safe pause/resume mechanism
 *   <li>Graceful stop with cleanup support
 *   <li>State query methods for UI integration
 *   <li>Checkpoint support for pause points
 * </ul>
 *
 * <p>Implementation requirements:
 *
 * <ul>
 *   <li>All methods must be thread-safe
 *   <li>State transitions must be atomic
 *   <li>Pause should block execution until resumed
 *   <li>Stop should allow for cleanup operations
 * </ul>
 */
public interface ExecutionController {

    /**
     * Pauses the execution at the next checkpoint.
     *
     * <p>This method sets the execution state to PAUSED, causing any threads checking pause points
     * to block until resume() is called. The pause takes effect at the next checkpoint, not
     * immediately.
     *
     * @throws IllegalStateException if the execution cannot be paused from current state
     */
    void pause();

    /**
     * Resumes a paused execution.
     *
     * <p>This method changes the state from PAUSED to RUNNING and notifies all waiting threads to
     * continue execution.
     *
     * @throws IllegalStateException if the execution is not currently paused
     */
    void resume();

    /**
     * Stops the execution gracefully.
     *
     * <p>This method signals all executing threads to stop at the next checkpoint. Unlike pause(),
     * stopped executions cannot be resumed.
     *
     * @throws IllegalStateException if the execution cannot be stopped from current state
     */
    void stop();

    /**
     * Starts or restarts the execution.
     *
     * <p>This method changes the state from IDLE or STOPPED to RUNNING.
     *
     * @throws IllegalStateException if the execution cannot be started from current state
     */
    void start();

    /**
     * Checks if the execution is currently paused.
     *
     * @return true if the execution state is PAUSED
     */
    boolean isPaused();

    /**
     * Checks if the execution has been stopped.
     *
     * @return true if the execution state is STOPPED or STOPPING
     */
    boolean isStopped();

    /**
     * Checks if the execution is currently running.
     *
     * @return true if the execution state is RUNNING
     */
    boolean isRunning();

    /**
     * Gets the current execution state.
     *
     * @return the current ExecutionState
     */
    ExecutionState getState();

    /**
     * Checks for pause or stop conditions and blocks if paused.
     *
     * <p>This method should be called at regular intervals during execution to enable pause/resume
     * functionality. If the execution is paused, this method blocks until resumed. If the execution
     * is stopped, this method throws ExecutionStoppedException.
     *
     * @throws ExecutionStoppedException if the execution has been stopped
     * @throws InterruptedException if the thread is interrupted while paused
     */
    void checkPausePoint() throws ExecutionStoppedException, InterruptedException;

    /**
     * Resets the controller to IDLE state.
     *
     * <p>This method should be called after execution completes or when preparing for a new
     * execution cycle.
     */
    void reset();
}
