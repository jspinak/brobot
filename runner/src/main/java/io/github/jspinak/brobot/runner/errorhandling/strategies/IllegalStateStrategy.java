package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling illegal state exceptions. These errors indicate operations attempted in
 * invalid states.
 */
@Slf4j
public class IllegalStateStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.warn(
                "Handling IllegalStateException in {}: {}",
                context.getComponent(),
                error.getMessage());

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("Operation cannot be performed in current state.")
                .technicalDetails(
                        String.format(
                                "IllegalStateException: %s\nComponent: %s\nOperation: %s",
                                error.getMessage(), context.getComponent(), context.getOperation()))
                .recoveryAction(
                        () -> {
                            log.info("Attempting to reset to valid state...");
                            // In a real implementation, this would trigger state reset logic
                        })
                .build();
    }
}
