/**
 * Defines Service Provider Interfaces (SPIs) for extensible logging backends.
 *
 * <p>This package contains the contracts that allow the Brobot logging framework to work with
 * different storage and processing backends. By implementing these interfaces, developers can
 * integrate the logging system with various persistence mechanisms, monitoring systems, or
 * analytics platforms without modifying the core logging framework.
 *
 * <h2>Core Interfaces</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.spi.LogSink} - The primary interface for
 *       persisting log data to any storage backend
 *   <li>{@link io.github.jspinak.brobot.tools.logging.spi.NoOpLogSink} - A no-operation
 *       implementation useful for testing or when logging should be disabled
 * </ul>
 *
 * <h2>Architecture Benefits</h2>
 *
 * <ul>
 *   <li><strong>Pluggability</strong>: New storage backends can be added without changing the
 *       logging framework
 *   <li><strong>Testability</strong>: Mock implementations can be easily created for unit testing
 *   <li><strong>Performance</strong>: Different sinks can be optimized for specific use cases
 *       (buffering, async, etc.)
 *   <li><strong>Flexibility</strong>: Multiple sinks can be combined using composite patterns
 * </ul>
 *
 * <h2>Implementation Examples</h2>
 *
 * <pre>{@code
 * // File-based sink
 * public class FileLogSink implements LogSink {
 *     private final Path logFile;
 *
 *     @Override
 *     public void accept(LogData logData) {
 *         String json = serialize(logData);
 *         Files.write(logFile, json.getBytes(), APPEND);
 *     }
 * }
 *
 * // Database sink
 * public class DatabaseLogSink implements LogSink {
 *     private final DataSource dataSource;
 *
 *     @Override
 *     public void accept(LogData logData) {
 *         try (Connection conn = dataSource.getConnection()) {
 *             insertLogEntry(conn, logData);
 *         }
 *     }
 * }
 *
 * // Composite sink for multiple destinations
 * public class CompositeLogSink implements LogSink {
 *     private final List<LogSink> sinks;
 *
 *     @Override
 *     public void accept(LogData logData) {
 *         sinks.forEach(sink -> sink.accept(logData));
 *     }
 * }
 * }</pre>
 *
 * <h2>Built-in Implementations</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.spi.NoOpLogSink} - Discards all log data
 *       (useful for testing or disabling logging)
 * </ul>
 *
 * <h2>Creating Custom Sinks</h2>
 *
 * <p>To create a custom LogSink:
 *
 * <ol>
 *   <li>Implement the {@link io.github.jspinak.brobot.tools.logging.spi.LogSink} interface
 *   <li>Handle the {@link io.github.jspinak.brobot.tools.logging.model.LogData} appropriately for
 *       your backend
 *   <li>Consider thread safety if the sink will be used concurrently
 *   <li>Implement error handling to prevent logging failures from affecting the main application
 *   <li>Optionally implement buffering or async processing for performance
 * </ol>
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * // Using a custom sink
 * LogSink customSink = new ElasticsearchLogSink(esClient);
 * ActionLogger logger = new ActionLoggerImpl(customSink);
 *
 * // Using multiple sinks
 * LogSink compositeSink = new CompositeLogSink(
 *     new FileLogSink("app.log"),
 *     new ConsoleLogSink(),
 *     new MetricsLogSink(metricsRegistry)
 * );
 * ActionLogger logger = new ActionLoggerImpl(compositeSink);
 * }</pre>
 *
 * @see io.github.jspinak.brobot.tools.logging.ActionLogger
 * @see io.github.jspinak.brobot.tools.logging.model.LogData
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.logging.spi;
