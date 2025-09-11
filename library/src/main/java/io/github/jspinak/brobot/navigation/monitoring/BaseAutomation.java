package io.github.jspinak.brobot.navigation.monitoring;

import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ExecutionState;
import io.github.jspinak.brobot.control.ExecutionStoppedException;
import io.github.jspinak.brobot.control.ThreadSafeExecutionController;

/**
 * Abstract base class for building GUI automation applications in the Brobot framework.
 *
 * <p>BaseAutomation provides the foundational structure for creating automation scripts that
 * leverage Brobot's model-based approach. It implements the AutomationScript interface and provides
 * common functionality needed by all automation applications, including lifecycle management,
 * execution control, and state handling integration.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Lifecycle Management</b>: Full execution control with pause/resume/stop
 *   <li><b>State Handler Integration</b>: Direct access to state-based automation patterns
 *   <li><b>Thread Safety</b>: Thread-safe execution control for multi-threaded operation
 *   <li><b>Pause Points</b>: Built-in support for pause/resume functionality
 *   <li><b>Extensibility</b>: Abstract design allows custom automation implementations
 * </ul>
 *
 * <p>Subclasses should implement:
 *
 * <ul>
 *   <li>The {@code runScript()} method to define automation logic
 *   <li>Regular calls to {@code checkPausePoint()} for pause/stop functionality
 *   <li>Any initialization logic needed for their specific use case
 *   <li>Custom error handling and recovery strategies
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * public class MyAutomation extends BaseAutomation {
 *     protected void runScript() {
 *         while (isRunning()) {
 *             try {
 *                 checkPausePoint(); // Enable pause/resume
 *                 // Perform automation tasks
 *             } catch (ExecutionStoppedException e) {
 *                 break; // Exit gracefully
 *             }
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>In the model-based approach, BaseAutomation serves as the bridge between the framework's state
 * management capabilities and user-defined automation logic. It enables developers to focus on
 * business logic while the framework handles the complexities of GUI interaction and state
 * navigation.
 *
 * @since 1.0
 * @see AutomationScript
 * @see StateHandler
 * @see MonitoringService
 * @see ExecutionController
 */
public abstract class BaseAutomation implements AutomationScript {

    /**
     * State handler for managing GUI state navigation.
     *
     * <p>Provides access to state-based automation patterns and navigation capabilities. Final to
     * ensure consistent state handling throughout the automation lifecycle.
     */
    protected final StateHandler stateHandler;

    /**
     * Execution controller for managing pause/resume/stop functionality.
     *
     * <p>Provides thread-safe execution control with support for pausing, resuming, and stopping
     * automation execution.
     */
    protected final ExecutionController executionController;

    /**
     * Constructs a BaseAutomation with the specified state handler.
     *
     * <p>The state handler provides access to Brobot's state management capabilities, enabling
     * state-based navigation and automation patterns. A default ThreadSafeExecutionController is
     * created for execution control.
     *
     * @param stateHandler Handler for managing GUI state transitions
     */
    protected BaseAutomation(StateHandler stateHandler) {
        this.stateHandler = stateHandler;
        this.executionController = new ThreadSafeExecutionController();
    }

    /**
     * Constructs a BaseAutomation with the specified state handler and execution controller.
     *
     * <p>This constructor allows for dependency injection of a custom execution controller, useful
     * for testing or when sharing execution control across components.
     *
     * @param stateHandler Handler for managing GUI state transitions
     * @param executionController Controller for managing execution flow
     */
    protected BaseAutomation(StateHandler stateHandler, ExecutionController executionController) {
        this.stateHandler = stateHandler;
        this.executionController = executionController;
    }

    /**
     * Starts the automation execution.
     *
     * <p>Sets the execution state to RUNNING and updates the deprecated running flag for backward
     * compatibility.
     *
     * @throws IllegalStateException if the execution cannot be started from current state
     */
    @Override
    public void start() {
        executionController.start();
    }

    /**
     * Pauses the automation at the next checkpoint.
     *
     * <p>The automation will pause when it next calls checkPausePoint(). Can be resumed by calling
     * resume().
     *
     * @throws IllegalStateException if the execution cannot be paused from current state
     */
    @Override
    public void pause() {
        executionController.pause();
    }

    /**
     * Resumes a paused automation.
     *
     * <p>Continues execution from where it was paused.
     *
     * @throws IllegalStateException if the execution is not currently paused
     */
    @Override
    public void resume() {
        executionController.resume();
    }

    /**
     * Stops the automation gracefully.
     *
     * <p>This method signals the automation to stop but does not wait for completion. Subclasses
     * should check pause points in their automation loops and exit gracefully when stopped.
     *
     * @throws IllegalStateException if the execution cannot be stopped from current state
     */
    @Override
    public void stop() {
        executionController.stop();
    }

    /**
     * Returns the current running state of the automation.
     *
     * <p>Thread-safe check of the execution state.
     *
     * @return true if automation is running, false otherwise
     */
    @Override
    public boolean isRunning() {
        return executionController.isRunning();
    }

    /**
     * Checks if the automation is currently paused.
     *
     * @return true if the execution state is PAUSED
     */
    @Override
    public boolean isPaused() {
        return executionController.isPaused();
    }

    /**
     * Checks if the automation has been stopped.
     *
     * @return true if the execution state is STOPPED or STOPPING
     */
    @Override
    public boolean isStopped() {
        return executionController.isStopped();
    }

    /**
     * Gets the current execution state.
     *
     * @return the current ExecutionState
     */
    @Override
    public ExecutionState getState() {
        return executionController.getState();
    }

    /**
     * Checks for pause or stop conditions and blocks if paused.
     *
     * <p>Subclasses should call this method regularly in their automation loops to enable
     * pause/resume functionality.
     *
     * @throws ExecutionStoppedException if the execution has been stopped
     * @throws InterruptedException if the thread is interrupted while paused
     */
    @Override
    public void checkPausePoint() throws ExecutionStoppedException, InterruptedException {
        executionController.checkPausePoint();
    }

    /**
     * Resets the controller to IDLE state.
     *
     * <p>Should be called after execution completes or when preparing for a new execution cycle.
     */
    @Override
    public void reset() {
        executionController.reset();
    }
}
