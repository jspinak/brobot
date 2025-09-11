package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling illegal argument exceptions. These errors are typically recoverable with
 * correct input.
 */
@Slf4j
public class IllegalArgumentStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.debug("Handling IllegalArgumentException: {}", error.getMessage());

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("Invalid input provided: " + sanitizeMessage(error.getMessage()))
                .technicalDetails(error.toString())
                .recoveryAction(() -> log.info("Please provide valid input and retry"))
                .build();
    }

    private String sanitizeMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "Invalid argument";
        }
        // Limit message length and remove any sensitive information
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }
}
