package io.github.jspinak.brobot.navigation.monitoring;

/**
 * Abstract base class for building GUI automation applications in the Brobot framework.
 * 
 * <p>BaseAutomation provides the foundational structure for creating automation scripts 
 * that leverage Brobot's model-based approach. It implements the AutomationScript interface 
 * and provides common functionality needed by all automation applications, including 
 * lifecycle management and state handling integration.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Lifecycle Management</b>: Built-in running state tracking and stop mechanism</li>
 *   <li><b>State Handler Integration</b>: Direct access to state-based automation patterns</li>
 *   <li><b>Thread Safety</b>: Volatile running flag for safe multi-threaded operation</li>
 *   <li><b>Extensibility</b>: Abstract design allows custom automation implementations</li>
 * </ul>
 * </p>
 * 
 * <p>Subclasses should implement:
 * <ul>
 *   <li>The {@code runScript()} method to define automation logic</li>
 *   <li>Any initialization logic needed for their specific use case</li>
 *   <li>Custom error handling and recovery strategies</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, BaseAutomation serves as the bridge between the 
 * framework's state management capabilities and user-defined automation logic. It 
 * enables developers to focus on business logic while the framework handles the 
 * complexities of GUI interaction and state navigation.</p>
 * 
 * @since 1.0
 * @see AutomationScript
 * @see StateHandler
 * @see MonitoringService
 */
public abstract class BaseAutomation implements AutomationScript {

    /**
     * Thread-safe flag tracking the automation's running state.
     * <p>
     * Uses volatile keyword to ensure visibility across threads.
     * Protected access allows subclasses to check state in their
     * automation loops.
     */
    protected volatile boolean running = false;
    
    /**
     * State handler for managing GUI state navigation.
     * <p>
     * Provides access to state-based automation patterns and
     * navigation capabilities. Final to ensure consistent state
     * handling throughout the automation lifecycle.
     */
    protected final StateHandler stateHandler;

    /**
     * Constructs a BaseAutomation with the specified state handler.
     * <p>
     * The state handler provides access to Brobot's state management
     * capabilities, enabling state-based navigation and automation patterns.
     *
     * @param stateHandler Handler for managing GUI state transitions
     */
    protected BaseAutomation(StateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    /**
     * Returns the current running state of the automation.
     * <p>
     * Thread-safe read of the volatile running flag. Can be called
     * from any thread to check if the automation is active.
     *
     * @return true if automation is running, false otherwise
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Stops the automation by setting the running flag to false.
     * <p>
     * This method signals the automation to stop but does not wait for
     * completion. Subclasses should check the running flag in their
     * automation loops and exit gracefully when it becomes false.
     * <p>
     * Thread-safe due to volatile flag. Can be called from any thread
     * to initiate shutdown.
     */
    @Override
    public void stop() {
        running = false;
    }
}