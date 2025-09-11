package io.github.jspinak.brobot.config.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.jspinak.brobot.tools.logging.spi.LogSink;
import io.github.jspinak.brobot.tools.logging.spi.NoOpLogSink;

/**
 * Spring configuration for the Brobot logging subsystem.
 *
 * <p>LoggingConfiguration provides a flexible logging infrastructure that allows different
 * implementations of {@link LogSink} to be plugged in based on the deployment context. It
 * establishes a no-operation default to ensure the framework functions even when no specific
 * logging implementation is provided.
 *
 * <p><strong>Design principles:</strong>
 *
 * <ul>
 *   <li>Extensibility: Applications can provide custom LogSink implementations
 *   <li>Safe defaults: NoOpLogSink ensures framework stability without logging
 *   <li>Spring integration: Uses conditional beans for flexible configuration
 * </ul>
 *
 * <p><strong>Common logging implementations:</strong>
 *
 * <ul>
 *   <li>{@link NoOpLogSink} - Default, discards all log entries (no side effects)
 *   <li>DatabaseLogSink - Persists logs to a database (provided by runner module)
 *   <li>FileLogSink - Writes logs to files (custom implementation)
 *   <li>RemoteLogSink - Sends logs to external systems (custom implementation)
 * </ul>
 *
 * <p><strong>Usage example:</strong>
 *
 * <pre>{@code
 * // To provide a custom LogSink, define a bean in your configuration:
 * @Configuration
 * public class MyAppConfig {
 *     @Bean
 *     public LogSink customLogSink() {
 *         return new MyCustomLogSink();
 *     }
 * }
 *
 * // The framework will automatically use your custom sink instead of NoOpLogSink
 * }</pre>
 *
 * <p>In the model-based approach, centralized logging is crucial for understanding action execution
 * patterns, debugging automation failures, and building datasets for machine learning. This
 * configuration ensures logging is always available while allowing deployment-specific
 * customization.
 *
 * @see LogSink
 * @see NoOpLogSink
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 */
@Configuration
public class LoggingConfiguration {

    /**
     * Provides a default no-operation log sink when no other implementation exists.
     *
     * <p>This bean is only created if no other {@link LogSink} bean is present in the Spring
     * context, thanks to the {@code @ConditionalOnMissingBean} annotation. This ensures that:
     *
     * <ul>
     *   <li>The framework always has a LogSink available
     *   <li>Applications can override with their own LogSink implementations
     *   <li>No logging overhead occurs in minimal deployments
     * </ul>
     *
     * <p>The NoOpLogSink implementation discards all log entries without processing, making it
     * ideal for testing or lightweight deployments where logging is not required.
     *
     * <p><strong>Override behavior:</strong>
     *
     * <p>When another module (like brobot-runner) provides a LogSink bean such as DatabaseLogSink,
     * this default bean will not be created, and the provided implementation will be used
     * throughout the framework.
     *
     * @return A NoOpLogSink instance that silently discards all log entries
     * @see NoOpLogSink
     * @see ConditionalOnMissingBean
     */
    @Bean
    @ConditionalOnMissingBean(LogSink.class)
    public LogSink defaultLogSink() {
        // This bean will only be created if no other LogSink bean
        // (like the runner's DatabaseLogSink) is present in the context.
        return new NoOpLogSink();
    }
}
