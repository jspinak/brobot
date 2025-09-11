package io.github.jspinak.brobot.runner.errorhandling.strategy.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/** Strategy for handling interrupted exceptions. */
@Slf4j
public class InterruptedStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.warn("Thread interrupted during: {}", context.getOperation());

        // Restore interrupted status
        Thread.currentThread().interrupt();

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("Operation was cancelled.")
                .technicalDetails("Thread interrupted during " + context.getOperation())
                .build();
    }
}
