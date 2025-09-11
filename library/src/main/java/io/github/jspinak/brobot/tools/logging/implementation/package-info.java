/**
 * Provides concrete implementations of the logging framework interfaces.
 *
 * <p>This package contains the default implementations of the core logging interfaces defined in
 * the parent package. These implementations serve as the primary logging mechanism for the Brobot
 * framework, bridging the high-level logging API with the underlying storage and processing layers.
 *
 * <h2>Key Implementations</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.implementation.ActionLoggerImpl} - The main
 *       implementation of {@link io.github.jspinak.brobot.tools.logging.ActionLogger} that creates
 *       structured log entries and delegates persistence to LogSink
 *   <li>{@link io.github.jspinak.brobot.tools.logging.implementation.SessionLifecycleLoggerImpl} -
 *       Manages logging for automation session lifecycle events
 * </ul>
 *
 * <h2>Architecture</h2>
 *
 * <p>The implementations follow these design principles:
 *
 * <ul>
 *   <li><strong>Separation of Concerns</strong>: Logging logic is separated from persistence,
 *       allowing flexible storage backends
 *   <li><strong>Dependency Injection</strong>: Implementations accept LogSink instances, enabling
 *       easy testing and configuration
 *   <li><strong>Thread Safety</strong>: Implementations are designed to be thread-safe for
 *       concurrent logging operations
 *   <li><strong>Performance</strong>: Minimal overhead is added to the automation execution flow
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Creating a logger with a specific sink
 * LogSink sink = new FileLogSink("automation.log");
 * ActionLogger logger = new ActionLoggerImpl(sink);
 *
 * // Using the logger
 * logger.logAction("type", "username", "testuser");
 * logger.logStateTransition("LoginPage", "HomePage");
 * logger.logError("Validation failed", exception);
 *
 * // Session lifecycle logging
 * SessionLifecycleLogger lifecycleLogger = new SessionLifecycleLoggerImpl(sink);
 * lifecycleLogger.logSessionStart("TestSuite1");
 * // ... test execution ...
 * lifecycleLogger.logSessionEnd("TestSuite1", metrics);
 * }</pre>
 *
 * <h2>Extension Points</h2>
 *
 * <p>While these are the default implementations, the framework allows for custom implementations
 * of the logging interfaces. Custom implementations can be created to:
 *
 * <ul>
 *   <li>Add specialized formatting or filtering
 *   <li>Integrate with specific monitoring systems
 *   <li>Implement custom buffering or batching strategies
 *   <li>Add encryption or compression to log data
 * </ul>
 *
 * @see io.github.jspinak.brobot.tools.logging.ActionLogger
 * @see io.github.jspinak.brobot.tools.logging.spi.LogSink
 * @see io.github.jspinak.brobot.tools.logging.model.LogData
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.logging.implementation;
