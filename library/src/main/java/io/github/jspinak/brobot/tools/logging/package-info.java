/**
 * Provides comprehensive logging capabilities for the Brobot automation framework.
 * 
 * <p>This package forms the foundation of Brobot's logging infrastructure, offering
 * a flexible and extensible system for capturing, formatting, and persisting
 * automation events, state transitions, and performance metrics.
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.ActionLogger} - The main logging interface
 *       that defines the contract for all logging operations</li>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.ConsoleReporter} - Generates formatted
 *       console reports with visual hierarchy and metrics</li>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.LogEventDispatcher} - Manages event
 *       distribution to multiple log sinks</li>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.StateHierarchyVisualizer} - Creates
 *       visual representations of state transitions</li>
 * </ul>
 * 
 * <h2>Architecture</h2>
 * <p>The logging framework follows a clean architecture pattern with clear separation
 * of concerns:
 * <ul>
 *   <li>Public API interfaces in the root package</li>
 *   <li>Domain models in the {@code model} subpackage</li>
 *   <li>Data transfer objects in the {@code dto} subpackage</li>
 *   <li>Implementation classes in the {@code implementation} subpackage</li>
 *   <li>Service provider interfaces in the {@code spi} subpackage</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Structured logging with type-safe event categories</li>
 *   <li>Performance metrics tracking and reporting</li>
 *   <li>Visual state hierarchy representation</li>
 *   <li>Pluggable storage backends through SPI</li>
 *   <li>ANSI color support for enhanced console output</li>
 *   <li>Video recording integration for visual debugging</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ActionLogger logger = new ActionLoggerImpl(logSink);
 * logger.logAction("click", "Submit Button");
 * logger.logStateTransition("LoginPage", "DashboardPage");
 * logger.logPerformance("Page Load", 1500L);
 * }</pre>
 * 
 * @see io.github.jspinak.brobot.tools.logging.spi.LogSink
 * @see io.github.jspinak.brobot.tools.logging.model.LogData
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.logging;