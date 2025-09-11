package io.github.jspinak.brobot.runner.errorhandling;

/**
 * Interface for error handling strategies. Different strategies can be implemented for different
 * types of errors.
 */
public interface IErrorStrategy {

    /**
     * Handle an error and return a result indicating the outcome.
     *
     * @param error The error to handle
     * @param context The error context
     * @return The result of handling the error
     */
    ErrorResult handle(Throwable error, ErrorContext context);
}
