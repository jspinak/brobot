package io.github.jspinak.brobot.navigation.monitoring;

/**
 * Core interface for automation script lifecycle management in the Brobot framework.
 * 
 * <p>AutomationScript defines the fundamental contract that all automation implementations 
 * must follow, establishing a consistent lifecycle pattern for starting, stopping, and 
 * monitoring automation execution. This interface enables the framework to manage diverse 
 * automation scripts uniformly, regardless of their complexity or implementation details.</p>
 * 
 * <p>Lifecycle operations:
 * <ul>
 *   <li><b>start()</b>: Initiates automation execution, typically launching the main 
 *       automation logic in a controlled manner</li>
 *   <li><b>stop()</b>: Gracefully terminates automation execution, allowing for cleanup 
 *       and resource release</li>
 *   <li><b>isRunning()</b>: Provides current execution status for monitoring and control</li>
 * </ul>
 * </p>
 * 
 * <p>Implementation requirements:
 * <ul>
 *   <li>Thread-safe state management for concurrent access to running status</li>
 *   <li>Graceful shutdown handling to avoid leaving the GUI in an inconsistent state</li>
 *   <li>Proper resource cleanup in stop() method</li>
 *   <li>Accurate status reporting through isRunning()</li>
 * </ul>
 * </p>
 * 
 * <p>Common implementation patterns:
 * <ul>
 *   <li><b>BaseAutomation</b>: Abstract base class providing common lifecycle logic</li>
 *   <li><b>ContinuousAutomation</b>: For scripts that run indefinitely until stopped</li>
 *   <li><b>BatchAutomation</b>: For scripts that process a fixed set of tasks</li>
 *   <li><b>ScheduledAutomation</b>: For scripts that run at specific intervals</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, AutomationScript provides the bridge between the 
 * framework's state management capabilities and user-defined automation logic. It ensures 
 * that all automation scripts can be controlled uniformly while allowing complete freedom 
 * in how the automation logic is implemented.</p>
 * 
 * <p>This interface is intentionally minimal to provide maximum flexibility while ensuring 
 * essential lifecycle management capabilities. Implementations typically extend BaseAutomation 
 * for additional functionality like state handling and error management.</p>
 * 
 * @since 1.0
 * @see BaseAutomation
 * @see MonitoringService
 * @see StateHandler
 */
public interface AutomationScript {
    /**
     * Starts the automation script execution.
     * <p>
     * Initiates the automation logic, typically launching background threads
     * or scheduled tasks. Should be idempotent - calling start() on an already
     * running script should have no effect.
     * <p>
     * Implementation notes:
     * <ul>
     *   <li>Should return quickly, not block the calling thread</li>
     *   <li>Must update internal state to reflect running status</li>
     *   <li>Should handle initialization errors gracefully</li>
     * </ul>
     */
    void start();
    
    /**
     * Stops the automation script execution.
     * <p>
     * Gracefully terminates the automation, allowing for proper cleanup of
     * resources and GUI state. Should handle being called multiple times
     * or when the script is not running.
     * <p>
     * Implementation notes:
     * <ul>
     *   <li>Should interrupt or signal running threads to stop</li>
     *   <li>Must wait for clean shutdown before returning</li>
     *   <li>Should reset internal state to not-running</li>
     * </ul>
     */
    void stop();
    
    /**
     * Checks if the automation script is currently running.
     * <p>
     * Provides real-time status of the automation execution. Used by
     * monitoring services and UI components to display current state.
     *
     * @return true if the automation is actively running, false otherwise
     */
    boolean isRunning();
}