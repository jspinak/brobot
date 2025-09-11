package io.github.jspinak.brobot.runner.errorhandling;

/**
 * Interface for error processors that can handle errors in various ways (logging, notifications,
 * metrics, etc.).
 */
public interface IErrorProcessor {

    /**
     * Process an error with its context.
     *
     * @param error The error that occurred
     * @param context The error context with additional information
     */
    void process(Throwable error, ErrorContext context);
}
