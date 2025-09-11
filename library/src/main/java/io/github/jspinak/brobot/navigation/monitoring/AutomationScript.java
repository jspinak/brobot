package io.github.jspinak.brobot.navigation.monitoring;

import io.github.jspinak.brobot.control.ExecutionController;

/**
 * Core interface for automation script lifecycle management in the Brobot framework.
 *
 * <p>AutomationScript defines the fundamental contract that all automation implementations must
 * follow, establishing a consistent lifecycle pattern for starting, stopping, and monitoring
 * automation execution. This interface enables the framework to manage diverse automation scripts
 * uniformly, regardless of their complexity or implementation details.
 *
 * <p>Lifecycle operations:
 *
 * <ul>
 *   <li><b>start()</b>: Initiates automation execution, typically launching the main automation
 *       logic in a controlled manner
 *   <li><b>stop()</b>: Gracefully terminates automation execution, allowing for cleanup and
 *       resource release
 *   <li><b>isRunning()</b>: Provides current execution status for monitoring and control
 * </ul>
 *
 * <p>Implementation requirements:
 *
 * <ul>
 *   <li>Thread-safe state management for concurrent access to running status
 *   <li>Graceful shutdown handling to avoid leaving the GUI in an inconsistent state
 *   <li>Proper resource cleanup in stop() method
 *   <li>Accurate status reporting through isRunning()
 * </ul>
 *
 * <p>Common implementation patterns:
 *
 * <ul>
 *   <li><b>BaseAutomation</b>: Abstract base class providing common lifecycle logic
 *   <li><b>ContinuousAutomation</b>: For scripts that run indefinitely until stopped
 *   <li><b>BatchAutomation</b>: For scripts that process a fixed set of tasks
 *   <li><b>ScheduledAutomation</b>: For scripts that run at specific intervals
 * </ul>
 *
 * <p>In the model-based approach, AutomationScript provides the bridge between the framework's
 * state management capabilities and user-defined automation logic. It ensures that all automation
 * scripts can be controlled uniformly while allowing complete freedom in how the automation logic
 * is implemented.
 *
 * <p>This interface is intentionally minimal to provide maximum flexibility while ensuring
 * essential lifecycle management capabilities. Implementations typically extend BaseAutomation for
 * additional functionality like state handling and error management.
 *
 * <p>As of version 2.0, AutomationScript extends ExecutionController to provide pause/resume
 * functionality in addition to the basic start/stop lifecycle. This enhancement enables more
 * sophisticated execution control for interactive debugging and user-controlled automation flows.
 *
 * @since 1.0
 * @see BaseAutomation
 * @see MonitoringService
 * @see StateHandler
 * @see ExecutionController
 */
public interface AutomationScript extends ExecutionController {
    // Methods inherited from ExecutionController:
    // - start() - Starts the automation script execution
    // - stop() - Stops the automation script execution
    // - pause() - Pauses execution at the next checkpoint
    // - resume() - Resumes a paused execution
    // - isRunning() - Checks if the automation is currently running
    // - isPaused() - Checks if the automation is currently paused
    // - isStopped() - Checks if the automation has been stopped
    // - getState() - Gets the current execution state
    // - checkPausePoint() - Checks for pause/stop conditions
    // - reset() - Resets the controller to IDLE state

    // AutomationScript can add additional methods specific to automation
    // scripts if needed in the future
}
