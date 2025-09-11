package io.github.jspinak.brobot.runner.errorhandling.strategy.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/** Strategy for handling I/O exceptions. */
@Slf4j
public class IOExceptionStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.error("IOException in operation: {}", context.getOperation(), error);

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("File operation failed: " + error.getMessage())
                .technicalDetails(error.toString())
                .recoveryAction(() -> log.info("Retrying file operation..."))
                .build();
    }
}
