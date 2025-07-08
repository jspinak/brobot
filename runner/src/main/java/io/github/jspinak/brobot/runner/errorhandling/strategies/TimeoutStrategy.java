package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeoutException;

/**
 * Strategy for handling timeout exceptions.
 * These errors are often recoverable with extended timeout or retry.
 */
@Slf4j
public class TimeoutStrategy implements IErrorStrategy {
    
    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        TimeoutException timeoutException = (TimeoutException) error;
        
        log.warn("Operation timed out in {}: {}", 
                context.getOperation(), timeoutException.getMessage());
        
        return ErrorResult.builder()
            .errorId(context.getErrorId())
            .success(false)
            .recoverable(true)
            .userMessage("Operation timed out. Please try again.")
            .technicalDetails(String.format(
                "TimeoutException in %s\nComponent: %s\nDetails: %s\nSuggestion: Consider increasing timeout or optimizing the operation",
                context.getOperation(),
                context.getComponent(),
                timeoutException.getMessage() != null ? timeoutException.getMessage() : "No additional details"
            ))
            .recoveryAction(() -> {
                log.info("Retrying operation {} with extended timeout...", context.getOperation());
                // In a real implementation, this would retry with a longer timeout
            })
            .build();
    }
}