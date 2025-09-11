package io.github.jspinak.brobot.runner.errorhandling.strategy.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/** Strategy for handling file not found exceptions. */
@Slf4j
public class FileNotFoundStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.error("File not found: {}", error.getMessage());

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("File not found: " + error.getMessage())
                .technicalDetails("Check file path and permissions")
                .build();
    }
}
