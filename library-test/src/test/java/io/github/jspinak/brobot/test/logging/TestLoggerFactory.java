package io.github.jspinak.brobot.test.logging;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.logging.unified.*;
import io.github.jspinak.brobot.logging.unified.console.ConsoleFormatter;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.ConsoleReporterInitializer;
import io.github.jspinak.brobot.tools.logging.spi.LogSink;
import io.github.jspinak.brobot.tools.logging.spi.NoOpLogSink;

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
            ActionLogger actionLogger, LoggingVerbosityConfig verbosityConfig) {

        // Step 1: Create base components
        LoggingContext context = new LoggingContext();
        LogSink logSink = new NoOpLogSink();
        ConsoleFormatter formatter = new ConsoleFormatter(verbosityConfig);

        // Step 2: Create message router with its dependencies
        MessageRouter router = new MessageRouter(actionLogger, verbosityConfig, formatter);

        // Step 3: Create the main logger
        BrobotLogger logger = new BrobotLogger(context, router);

        // Step 4: Initialize console reporter
        ConsoleReporterInitializer reporterInit =
                new ConsoleReporterInitializer(logger, verbosityConfig);

        return new LoggingSystem(context, logSink, formatter, router, logger, reporterInit);
    }

    /**
     * Container for all logging components. This ensures proper lifecycle management and access to
     * all components.
     */
    public static class LoggingSystem {
        private final LoggingContext context;
        private final LogSink logSink;
        private final ConsoleFormatter formatter;
        private final MessageRouter router;
        private final BrobotLogger logger;
        private final ConsoleReporterInitializer reporterInit;

        public LoggingSystem(
                LoggingContext context,
                LogSink logSink,
                ConsoleFormatter formatter,
                MessageRouter router,
                BrobotLogger logger,
                ConsoleReporterInitializer reporterInit) {
            this.context = context;
            this.logSink = logSink;
            this.formatter = formatter;
            this.router = router;
            this.logger = logger;
            this.reporterInit = reporterInit;
        }

        public LoggingContext getContext() {
            return context;
        }

        public LogSink getLogSink() {
            return logSink;
        }

        public ConsoleFormatter getFormatter() {
            return formatter;
        }

        public MessageRouter getRouter() {
            return router;
        }

        public BrobotLogger getLogger() {
            return logger;
        }

        public ConsoleReporterInitializer getReporterInit() {
            return reporterInit;
        }
    }
}
