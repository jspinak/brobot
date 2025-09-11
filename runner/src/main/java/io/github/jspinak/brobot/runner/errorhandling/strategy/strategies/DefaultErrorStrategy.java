package io.github.jspinak.brobot.runner.errorhandling.strategy.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Default error strategy for unhandled exceptions.
 *
 * <p>This strategy is used when no specific strategy is registered for an error type. It provides a
 * safe, generic error response.
 */
@Slf4j
public class DefaultErrorStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.debug("Handling error with default strategy: {}", error.getClass().getSimpleName());

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("An unexpected error occurred: " + error.getMessage())
                .technicalDetails(error.toString())
                .build();
    }
}
