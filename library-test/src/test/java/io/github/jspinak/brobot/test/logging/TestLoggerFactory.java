package io.github.jspinak.brobot.test.logging;

import org.mockito.Mockito;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogBuilder;

/**
 * Factory for creating logger components in tests. This factory ensures proper initialization order
 * and follows SRP by separating logger creation from bean wiring.
 *
 * <p>Single Responsibility: Create and wire logger components for tests.
 */
@Component
public class TestLoggerFactory {

    /**
     * Creates a complete logging system for tests. This method ensures all components are created
     * in the correct order.
     */
    public LoggingSystem createTestLoggingSystem(
            Object actionLogger, LoggingVerbosityConfig verbosityConfig) {

        // Create a mock BrobotLogger that returns a working LogBuilder
        BrobotLogger logger = Mockito.mock(BrobotLogger.class);
        LogBuilder logBuilder = Mockito.mock(LogBuilder.class);

        // Setup the mock to return the LogBuilder for chaining
        Mockito.when(logger.builder()).thenReturn(logBuilder);
        Mockito.when(logBuilder.prefix(Mockito.anyString())).thenReturn(logBuilder);
        Mockito.when(logBuilder.message(Mockito.anyString())).thenReturn(logBuilder);
        Mockito.when(logBuilder.data(Mockito.anyString(), Mockito.any())).thenReturn(logBuilder);
        Mockito.when(logBuilder.error(Mockito.any(Throwable.class))).thenReturn(logBuilder);

        return new LoggingSystem(logger);
    }

    /**
     * Container for all logging components. This ensures proper lifecycle management and access to
     * all components.
     */
    public static class LoggingSystem {
        private final BrobotLogger logger;

        public LoggingSystem(BrobotLogger logger) {
            this.logger = logger;
        }

        public BrobotLogger getLogger() {
            return logger;
        }

        // Return null for removed components - they're no longer needed
        public Object getContext() {
            return null;
        }

        public Object getLogSink() {
            return null;
        }

        public Object getFormatter() {
            return null;
        }

        public Object getRouter() {
            return null;
        }

        public Object getReporterInit() {
            return null;
        }
    }
}
