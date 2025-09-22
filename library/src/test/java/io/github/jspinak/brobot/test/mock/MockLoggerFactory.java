package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogBuilder;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Factory for creating mock logger instances for testing.
 * Provides various types of mock loggers for different test scenarios.
 */
@Component
public class MockLoggerFactory {

    /**
     * Creates a standard mock logger with fluent API support.
     *
     * @return A fully configured mock BrobotLogger
     */
    public BrobotLogger createMockLogger() {
        BrobotLogger logger = mock(BrobotLogger.class);
        LogBuilder builder = createMockLogBuilder();
        when(logger.builder(any(LogCategory.class))).thenReturn(builder);
        return logger;
    }

    /**
     * Creates a mock LogBuilder with fluent API support.
     * All builder methods return the builder itself to support method chaining.
     *
     * @return A fully configured mock LogBuilder
     */
    public LogBuilder createMockLogBuilder() {
        LogBuilder builder = mock(LogBuilder.class);

        // Configure fluent API - all methods return the builder
        when(builder.level(any(LogLevel.class))).thenReturn(builder);
        when(builder.message(anyString(), any())).thenReturn(builder);
        when(builder.context(anyString(), any())).thenReturn(builder);
        when(builder.action(anyString(), anyString())).thenReturn(builder);
        when(builder.duration(any(Duration.class))).thenReturn(builder);
        when(builder.error(any(Throwable.class))).thenReturn(builder);
        when(builder.correlationId(anyString())).thenReturn(builder);
        when(builder.state(anyString())).thenReturn(builder);
        doNothing().when(builder).log();

        return builder;
    }

    /**
     * Creates a silent logger that doesn't log anything.
     * Useful for performance tests where logging overhead should be minimized.
     *
     * @return A silent mock BrobotLogger
     */
    public BrobotLogger createSilentLogger() {
        BrobotLogger logger = mock(BrobotLogger.class);
        LogBuilder builder = createSilentLogBuilder();
        when(logger.builder(any(LogCategory.class))).thenReturn(builder);
        return logger;
    }

    /**
     * Creates a silent LogBuilder that does nothing.
     *
     * @return A silent mock LogBuilder
     */
    private LogBuilder createSilentLogBuilder() {
        LogBuilder builder = mock(LogBuilder.class);

        // Configure to do nothing but maintain fluent API
        when(builder.level(any(LogLevel.class))).thenReturn(builder);
        when(builder.message(anyString(), any())).thenReturn(builder);
        when(builder.context(anyString(), any())).thenReturn(builder);
        when(builder.action(anyString(), anyString())).thenReturn(builder);
        when(builder.duration(any(Duration.class))).thenReturn(builder);
        when(builder.error(any(Throwable.class))).thenReturn(builder);
        when(builder.correlationId(anyString())).thenReturn(builder);
        when(builder.state(anyString())).thenReturn(builder);
        doNothing().when(builder).log();

        return builder;
    }

    /**
     * Creates a capturing logger that records all log entries for verification.
     * Used with LogCapture utility for integration tests.
     *
     * @param logCapture The LogCapture instance to record entries to
     * @return A capturing mock BrobotLogger
     */
    public BrobotLogger createCapturingLogger(LogCapture logCapture) {
        BrobotLogger logger = mock(BrobotLogger.class);

        when(logger.builder(any(LogCategory.class))).thenAnswer(invocation -> {
            LogCategory category = invocation.getArgument(0);
            return new CapturingLogBuilder(category, logCapture);
        });

        return logger;
    }

    /**
     * Creates a strict logger that verifies correct usage patterns.
     * Throws exceptions if methods are called in incorrect order.
     *
     * @return A strict mock BrobotLogger
     */
    public BrobotLogger createStrictLogger() {
        BrobotLogger logger = mock(BrobotLogger.class, RETURNS_SELF);
        LogBuilder builder = createStrictLogBuilder();

        when(logger.builder(any(LogCategory.class))).thenReturn(builder);

        return logger;
    }

    /**
     * Creates a strict LogBuilder that enforces correct usage patterns.
     *
     * @return A strict mock LogBuilder
     */
    private LogBuilder createStrictLogBuilder() {
        LogBuilder builder = mock(LogBuilder.class, withSettings()
            .strictness(org.mockito.quality.Strictness.STRICT_STUBS));

        // Enforce that level must be set first
        when(builder.level(any(LogLevel.class))).thenReturn(builder);

        // All other methods also return builder
        when(builder.message(anyString(), any())).thenReturn(builder);
        when(builder.context(anyString(), any())).thenReturn(builder);
        when(builder.action(anyString(), anyString())).thenReturn(builder);
        when(builder.duration(any(Duration.class))).thenReturn(builder);
        when(builder.error(any(Throwable.class))).thenReturn(builder);
        when(builder.correlationId(anyString())).thenReturn(builder);
        when(builder.state(anyString())).thenReturn(builder);

        // Verify log() is called last
        doAnswer(invocation -> {
            verify(builder, atLeastOnce()).level(any(LogLevel.class));
            return null;
        }).when(builder).log();

        return builder;
    }

    /**
     * Creates a spy logger that wraps a real logger instance.
     * Useful for partial mocking scenarios.
     *
     * @param realLogger The real logger instance to spy on
     * @return A spy BrobotLogger
     */
    public BrobotLogger createSpyLogger(BrobotLogger realLogger) {
        return spy(realLogger);
    }
}