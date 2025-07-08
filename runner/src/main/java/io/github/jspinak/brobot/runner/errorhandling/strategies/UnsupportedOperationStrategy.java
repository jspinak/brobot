package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling unsupported operation exceptions.
 * These indicate attempts to use features that are not implemented.
 */
@Slf4j
public class UnsupportedOperationStrategy implements IErrorStrategy {
    
    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.warn("Unsupported operation attempted in {}: {}", 
                context.getComponent(), error.getMessage());
        
        return ErrorResult.builder()
            .errorId(context.getErrorId())
            .success(false)
            .recoverable(false)
            .userMessage("This operation is not supported.")
            .technicalDetails(String.format(
                "UnsupportedOperationException: %s\nOperation: %s\nComponent: %s",
                error.getMessage() != null ? error.getMessage() : "No details provided",
                context.getOperation(),
                context.getComponent()
            ))
            .build();
    }
}