package io.github.jspinak.brobot.runner.errorhandling.strategy.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling illegal state exceptions.
 */
@Slf4j
public class IllegalStateStrategy implements IErrorStrategy {
    
    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.warn("IllegalStateException: {}", error.getMessage());
        
        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("Operation cannot be performed in current state.")
                .technicalDetails(error.getMessage())
                .recoveryAction(() -> log.info("Resetting to valid state..."))
                .build();
    }
}