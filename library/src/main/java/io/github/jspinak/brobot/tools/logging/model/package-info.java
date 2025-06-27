/**
 * Contains core domain models representing log data structures.
 * 
 * <p>This package defines the fundamental data models used throughout the Brobot
 * logging framework. These models capture comprehensive information about automation
 * events, state transitions, performance metrics, and execution context. The models
 * are designed to be flexible, extensible, and suitable for various storage backends.
 * 
 * <h2>Core Models</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.model.LogData} - The primary
 *       log entry model that captures all aspects of an automation event</li>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.model.LogEventType} - Enumeration
 *       defining the categories of events that can be logged</li>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics} - 
 *       Aggregated performance and execution statistics</li>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.model.StateImageLogData} - 
 *       Specialized model for capturing visual state information</li>
 * </ul>
 * 
 * <h2>Model Design Principles</h2>
 * <ul>
 *   <li><strong>Comprehensiveness</strong>: Models capture all relevant information
 *       needed for debugging and analysis</li>
 *   <li><strong>Flexibility</strong>: Optional fields allow capturing varying levels
 *       of detail based on context</li>
 *   <li><strong>Type Safety</strong>: Strong typing with enums and specific field
 *       types prevent data corruption</li>
 *   <li><strong>Serialization Ready</strong>: Models are designed to work with
 *       standard Java serialization frameworks</li>
 * </ul>
 * 
 * <h2>LogData Structure</h2>
 * <p>The {@link io.github.jspinak.brobot.tools.logging.model.LogData} model includes:
 * <ul>
 *   <li>Event metadata (timestamp, type, session ID)</li>
 *   <li>Action details (action type, target, parameters)</li>
 *   <li>State information (current state, transitions)</li>
 *   <li>Performance metrics (duration, resource usage)</li>
 *   <li>Error information (exception details, stack traces)</li>
 *   <li>Visual debugging data (screenshots, recordings)</li>
 * </ul>
 * 
 * <h2>Event Types</h2>
 * <p>The {@link io.github.jspinak.brobot.tools.logging.model.LogEventType} enum
 * categorizes events into:
 * <ul>
 *   <li>ACTION - User interface interactions</li>
 *   <li>STATE_TRANSITION - Navigation between application states</li>
 *   <li>PERFORMANCE - Timing and resource measurements</li>
 *   <li>ERROR - Exception and failure events</li>
 *   <li>DEBUG - Detailed debugging information</li>
 *   <li>INFO - General informational messages</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Creating a comprehensive log entry
 * LogData logData = new LogData();
 * logData.setTimestamp(Instant.now());
 * logData.setType(LogEventType.ACTION);
 * logData.setSessionId("test-session-123");
 * logData.setAction("click");
 * logData.setTarget("loginButton");
 * logData.setCurrentState("LoginPage");
 * logData.setDuration(150L);
 * logData.setScreenshotPath("/logs/screenshots/login-click.png");
 * 
 * // Creating performance metrics
 * ExecutionMetrics metrics = new ExecutionMetrics();
 * metrics.setTotalActions(25);
 * metrics.setSuccessfulActions(24);
 * metrics.setAverageDuration(200L);
 * metrics.setTotalDuration(5000L);
 * }</pre>
 * 
 * @see io.github.jspinak.brobot.tools.logging.ActionLogger
 * @see io.github.jspinak.brobot.tools.logging.dto
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.logging.model;