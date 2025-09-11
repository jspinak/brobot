package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling null pointer exceptions. Provides user-friendly messages for null reference
 * errors.
 */
@Slf4j
public class NullPointerStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        log.debug("Handling NullPointerException in operation: {}", context.getOperation());

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("A required value was missing.")
                .technicalDetails(
                        "NullPointerException in "
                                + context.getOperation()
                                + "\nStack trace: "
                                + getRelevantStackTrace(error))
                .build();
    }

    private String getRelevantStackTrace(Throwable error) {
        StackTraceElement[] stackTrace = error.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement element = stackTrace[0];
            return String.format(
                    "at %s.%s(%s:%d)",
                    element.getClassName(),
                    element.getMethodName(),
                    element.getFileName(),
                    element.getLineNumber());
        }
        return "No stack trace available";
    }
}
