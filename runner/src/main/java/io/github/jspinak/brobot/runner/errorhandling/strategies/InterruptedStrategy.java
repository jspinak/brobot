package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling interrupted exceptions.
 * Properly restores the interrupted status of the thread.
 */
@Slf4j
public class InterruptedStrategy implements IErrorStrategy {
    
    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        // IMPORTANT: Restore interrupted status
        Thread.currentThread().interrupt();
        
        log.info("Thread interrupted during {}", context.getOperation());
        
        return ErrorResult.builder()
            .errorId(context.getErrorId())
            .success(false)
            .recoverable(false)
            .userMessage("Operation was cancelled.")
            .technicalDetails(String.format(
                "InterruptedException during %s\nComponent: %s\nThread: %s\nNote: Thread interrupted status has been restored",
                context.getOperation(),
                context.getComponent(),
                Thread.currentThread().getName()
            ))
            .build();
    }
}