/**
 * Reactive and continuous automation monitoring.
 *
 * <p>This package provides components for event-driven and continuous automation patterns. Unlike
 * traditional sequential automation, the monitoring approach enables systems that continuously
 * observe the GUI state and react to changes, making it ideal for handling dynamic applications,
 * asynchronous events, and long-running processes.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.navigation.monitoring.ReactiveAutomator} - Event-loop based
 *       automation that continuously monitors and responds to states
 *   <li>{@link io.github.jspinak.brobot.navigation.monitoring.MonitoringService} - Coordinates
 *       monitoring activities and state observations
 *   <li>{@link io.github.jspinak.brobot.navigation.monitoring.BaseAutomation} - Abstract base for
 *       custom automation implementations
 *   <li>{@link io.github.jspinak.brobot.navigation.monitoring.AutomationScript} - Script-based
 *       automation execution
 * </ul>
 *
 * <h2>State Handling</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.navigation.monitoring.StateHandler} - Interface for
 *       defining state-specific behavior
 *   <li>{@link io.github.jspinak.brobot.navigation.monitoring.DefaultStateHandler} - Standard
 *       implementation executing state transitions
 * </ul>
 *
 * <h2>Reactive Automation Model</h2>
 *
 * <p>ReactiveAutomator implements an event-loop pattern:
 *
 * <ol>
 *   <li><b>Scan</b> - Periodically search for active states
 *   <li><b>Detect</b> - Identify which states are currently active
 *   <li><b>React</b> - Execute appropriate handlers for active states
 *   <li><b>Loop</b> - Continue monitoring for changes
 * </ol>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Reactive Automation</h3>
 *
 * <pre>{@code
 * // Create and configure reactive automator
 * ReactiveAutomator automator = context.getBean(ReactiveAutomator.class);
 *
 * // Set search interval (milliseconds)
 * automator.setSearchInterval(2000); // Check every 2 seconds
 *
 * // Start monitoring
 * automator.start();
 *
 * // System now continuously:
 * // - Searches for active states
 * // - Executes transitions for found states
 * // - Handles unexpected popups/dialogs
 * // - Maintains desired application state
 *
 * // Stop when done
 * automator.stop();
 * }</pre>
 *
 * <h3>Custom State Handlers</h3>
 *
 * <pre>{@code
 * public class CustomStateHandler implements StateHandler {
 *     @Override
 *     public boolean handle(State state) {
 *         switch (state.getName()) {
 *             case "ErrorDialog":
 *                 // Handle error condition
 *                 click.perform(dismissButton);
 *                 logError(state);
 *                 return true;
 *
 *             case "UpdateNotification":
 *                 // Handle update prompt
 *                 click.perform(laterButton);
 *                 return true;
 *
 *             default:
 *                 // Use default transition handling
 *                 return defaultHandler.handle(state);
 *         }
 *     }
 * }
 *
 * // Configure automator with custom handler
 * automator.setStateHandler(new CustomStateHandler());
 * }</pre>
 *
 * <h3>Monitoring Service Usage</h3>
 *
 * <pre>{@code
 * MonitoringService monitor = context.getBean(MonitoringService.class);
 *
 * // Check current state
 * Set<State> activeStates = monitor.getCurrentStates();
 *
 * // Wait for specific state
 * boolean found = monitor.waitForState("SuccessDialog",
 *     Duration.ofSeconds(30));
 *
 * // Monitor state changes
 * monitor.onStateChange(states -> {
 *     System.out.println("Active states: " + states);
 * });
 * }</pre>
 *
 * <h2>Automation Patterns</h2>
 *
 * <h3>Watchdog Pattern</h3>
 *
 * <pre>{@code
 * // Monitor for error conditions
 * public class ErrorWatchdog extends BaseAutomation {
 *     @Override
 *     protected void executeAutomation() {
 *         // Continuously check for error states
 *         if (isStateActive("CriticalError")) {
 *             handleCriticalError();
 *             notifyAdministrator();
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>Maintenance Pattern</h3>
 *
 * <pre>{@code
 * // Keep application in desired state
 * public class MaintenanceAutomation extends BaseAutomation {
 *     @Override
 *     protected void executeAutomation() {
 *         // Ensure we stay logged in
 *         if (!isStateActive("Dashboard")) {
 *             navigateToState("Dashboard");
 *         }
 *
 *         // Handle session timeouts
 *         if (isStateActive("SessionExpired")) {
 *             relogin();
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>Event Response Pattern</h3>
 *
 * <pre>{@code
 * // Respond to asynchronous events
 * StateHandler eventHandler = state -> {
 *     if (state.getName().contains("Notification")) {
 *         // Process notification
 *         String message = extractMessage(state);
 *         processNotification(message);
 *         dismissNotification(state);
 *         return true;
 *     }
 *     return false;
 * };
 * }</pre>
 *
 * <h2>Configuration Options</h2>
 *
 * <h3>Search Intervals</h3>
 *
 * <ul>
 *   <li><b>Fast (100-500ms)</b> - Responsive but CPU intensive
 *   <li><b>Normal (1-2s)</b> - Good balance for most applications
 *   <li><b>Slow (5-10s)</b> - Low overhead for stable applications
 * </ul>
 *
 * <h3>State Handler Strategies</h3>
 *
 * <ul>
 *   <li><b>Transition-based</b> - Execute defined transitions (default)
 *   <li><b>Custom logic</b> - Application-specific handling
 *   <li><b>Hybrid</b> - Mix of transitions and custom code
 *   <li><b>Priority-based</b> - Handle critical states first
 * </ul>
 *
 * <h2>Use Cases</h2>
 *
 * <ol>
 *   <li><b>Popup Handling</b> - Dismiss unexpected dialogs automatically
 *   <li><b>Session Management</b> - Maintain login state
 *   <li><b>Error Recovery</b> - Detect and recover from error conditions
 *   <li><b>Process Monitoring</b> - Track long-running operations
 *   <li><b>Event Processing</b> - Handle asynchronous notifications
 *   <li><b>State Maintenance</b> - Keep application in desired configuration
 * </ol>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Choose appropriate search intervals for your use case
 *   <li>Implement proper error handling in state handlers
 *   <li>Use rest states to reduce CPU usage when idle
 *   <li>Log state changes for debugging and analysis
 *   <li>Clean shutdown to avoid resource leaks
 *   <li>Consider priority handling for critical states
 * </ol>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>Monitoring components use single-threaded executors to ensure:
 *
 * <ul>
 *   <li>Sequential state handling (no race conditions)
 *   <li>Predictable execution order
 *   <li>Safe shutdown and cleanup
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.statemanagement
 * @see io.github.jspinak.brobot.navigation.transition
 * @see io.github.jspinak.brobot.model.state
 */
package io.github.jspinak.brobot.navigation.monitoring;
