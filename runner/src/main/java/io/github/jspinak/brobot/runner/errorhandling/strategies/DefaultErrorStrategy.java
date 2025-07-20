package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Default error strategy for unhandled exceptions.
 * Provides a generic error response when no specific strategy is available.
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