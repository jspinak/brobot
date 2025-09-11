package io.github.jspinak.brobot.runner.errorhandling.strategy.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/** Strategy for handling timeout exceptions. */
@Slf4j
public class TimeoutStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.warn("Operation timed out: {}", context.getOperation());

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("Operation timed out. Please try again.")
                .technicalDetails("Timeout after waiting period")
                .recoveryAction(() -> log.info("Retrying with extended timeout..."))
                .build();
    }
}
